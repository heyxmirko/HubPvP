package me.quared.hubpvp.listeners.abilities;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.core.PvPManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PurpleStrikeAbility implements Ability {

    private final JavaPlugin plugin = JavaPlugin.getPlugin(HubPvP.class);

    List<Material> noCollisionBlocks = new ArrayList<>();

    public PurpleStrikeAbility() {
        noCollisionBlocks.add(Material.AIR);
        noCollisionBlocks.add(Material.WATER);
        noCollisionBlocks.add(Material.LIGHT);
    }

    @Override
    public void cast(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Action action = e.getAction();
        if (action == Action.RIGHT_CLICK_AIR) {
            handleAirRightClick(player);
        } else if (action == Action.RIGHT_CLICK_BLOCK && isCorrectBlockClicked(e.getClickedBlock(), player)) {
            handleBlockRightClick(player, e.getClickedBlock());
        }
    }

    private boolean isPlayerInPvPAndHoldingCorrectWeapon(Player player, ItemStack item) {
        HubPvP instance = HubPvP.instance();
        PvPManager pvpManager = instance.pvpManager();
        if (!pvpManager.isInPvP(player)) {
            return false;
        }

        ItemStack weapon = pvpManager.getWeapon();
        return item != null && item.getType() == weapon.getType() &&
                item.hasItemMeta() && item.getItemMeta().hasCustomModelData() &&
                item.getItemMeta().getCustomModelData() == Objects.requireNonNull(weapon.getItemMeta()).getCustomModelData();
    }

    private void handleAirRightClick(Player player) {
        if (player.getCooldown(Material.DIAMOND_SWORD) > 0) {
            player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 0.1F, 1.0F);
            return;
        }
        castSpell(player);
    }

    private boolean isCorrectBlockClicked(Block clickedBlock, Player player) {
        return clickedBlock != null && isWithinRadius(player.getLocation(), clickedBlock.getLocation(), 3);
    }

    private void handleBlockRightClick(Player player, Block clickedBlock) {
        if (player.getCooldown(Material.DIAMOND_SWORD) > 0) {
            player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 0.1F, 1.0F);
            return;
        }
        launchPlayerInAir(player);
    }

    public void castSpell(Player player) {
        final Location startLocation = player.getEyeLocation();
        final Vector direction = startLocation.getDirection().normalize();
        final double speedPerTick = 30.0 / 20.0; // 15 blocks per second, converted to blocks per tick
        final double maxDistance = 20.0;
        final Color purpleColor = Color.fromRGB(180, 0, 250); // Corrected RGB values for purple
        final Color darkGrayColor = Color.fromRGB(40, 40, 40); // Corrected RGB values for purple
        final Particle.DustOptions dustOptionPurple= new Particle.DustOptions(purpleColor, 1.0F);// Options for particle color and size
        final Particle.DustOptions dustOptionDarkGray = new Particle.DustOptions(darkGrayColor, 1.0F);

        // Play initial spell casting sound
        player.getWorld().playSound(startLocation, Sound.ENTITY_EVOKER_CAST_SPELL, SoundCategory.PLAYERS, 1.0f, 1.0f);

        new BukkitRunnable() {
            private double distanceTravelled = 0.0;
            private int ticks = 0;

            @Override
            public void run() {
                Location currentLocation = startLocation.clone().add(direction.clone().multiply(distanceTravelled));
                playEffectAndSound(currentLocation);

                if (checkCollision(currentLocation, player)) {
                    cancel();
                    return;
                }

                distanceTravelled += speedPerTick;
                ticks++;

                if (distanceTravelled >= maxDistance) {
                    cancel();
                }
            }

            private void playEffectAndSound(Location currentLocation) {
                if (ticks % 5 == 0) { // Every quarter second, considering 20 ticks per second
                    player.getWorld().playSound(currentLocation, Sound.ENTITY_WITHER_SHOOT, SoundCategory.PLAYERS, 0.02f, 2.0f);
                }

                int particlesPerTick = 6 + ticks / 2; // Increase particle density over time
                for (int i = 0; i < particlesPerTick; i++) {
                    double angle = 2 * Math.PI * i / particlesPerTick + ticks * 0.2;
                    Vector offset = new Vector(Math.cos(angle) * 0.35, 0, Math.sin(angle) * 0.35);
                    offset = rotateVector(offset, direction);

                    Location particleLocation = currentLocation.clone().add(offset);
                    player.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 1, 0, 0, 0, 0, dustOptionDarkGray);
                }
                player.getWorld().spawnParticle(Particle.REDSTONE, currentLocation, 1, 0, 0, 0, 0, dustOptionPurple);
            }

            private boolean checkCollision(Location location, Player caster) {
                // Check for block collisions
                Block block = location.getBlock();
                if (!noCollisionBlocks.contains(block.getType())) {
                    Objects.requireNonNull(location.getWorld()).spawnParticle(Particle.LAVA, location, 10);
                    location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.0f, 0.5f);
                    return true;
                }

                // Check for entity collisions
                for (Entity entity : Objects.requireNonNull(location.getWorld()).getNearbyEntities(location, 0.5, 0.5, 0.5)) {
                    if ((entity instanceof Player || entity instanceof Mob) && entity != caster) {
                        handleEntityCollision(entity, location, caster);
                        return true;
                    }
                }

                return false;
            }

            private void handleEntityCollision(Entity entity, Location location, Player caster) {
                if (entity instanceof Player) {
                    PvPManager pvpManager = HubPvP.instance().pvpManager();
                    if (!pvpManager.isInPvP((Player) entity)) {
                        return;
                    }
                }

                // Apply effects to the target entity
                ((LivingEntity) entity).damage(6.0);
                Vector knockbackDirection = entity.getLocation().toVector().subtract(caster.getLocation().toVector()).normalize().setY(0.5);
                entity.setVelocity(knockbackDirection.multiply(3.0));

                // Play sound and particle effects
                Objects.requireNonNull(location.getWorld()).spawnParticle(Particle.LAVA, location, 10);
                location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.0f, 0.5f);
                location.getWorld().playSound(location, Sound.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 1.0f, 1.0f);
                location.getWorld().playSound(location, Sound.ENTITY_PLAYER_HURT, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }

            private Vector rotateVector(Vector vector, Vector direction) {
                Vector axis = new Vector(0, 1, 0).crossProduct(direction).normalize();
                double angle = Math.acos(new Vector(0, 1, 0).dot(direction));
                return vector.rotateAroundAxis(axis, angle);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }


    public void launchPlayerInAir(Player player) {
        Vector launchVelocity = player.getVelocity().add(new Vector(0, 1, 0));
        player.setVelocity(launchVelocity);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.5F, 1.0F);
    }

    private boolean isWithinRadius(Location playerLocation, Location blockLocation, int radius) {
        if (playerLocation.getWorld() != blockLocation.getWorld()) {
            return false; // Different worlds
        }

        double deltaX = Math.abs(playerLocation.getX() - blockLocation.getX());
        double deltaZ = Math.abs(playerLocation.getZ() - blockLocation.getZ());
        double deltaY = playerLocation.getY() - blockLocation.getY(); // Check if block is directly below

        return deltaX <= radius && deltaZ <= radius && deltaY > 0 && deltaY <= 3; // Within radius and directly below within 3 blocks
    }

    @Override
    public int getCooldown() {
        return 200; // Example cooldown, adjust as needed
    }

    @Override
    public ItemStack getIcon() {
        ItemStack icon = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&5&lPurple Strike"));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Casts a beam that damages and"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7knocks back enemies in it's path."));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Can also launch you into the air"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7when clicking beneath yourself."));
            lore.add(ChatColor.translateAlternateColorCodes('&', ""));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Cooldown: &5&l20s"));
            meta.setLore(lore);
            icon.setItemMeta(meta);
        }
        return icon;
    }
}
