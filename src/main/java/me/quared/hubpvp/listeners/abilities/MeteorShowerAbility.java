package me.quared.hubpvp.listeners.abilities;

import me.quared.hubpvp.HubPvP;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import java.util.ArrayList;
import java.util.List;

public class MeteorShowerAbility implements Ability {

    @Override
    public void cast(PlayerInteractEvent event) {
        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            World world = player.getWorld();
            Location targetLocation = player.getTargetBlock(null, 100).getLocation(); // Target location
            double radius = 5.0; // Radius of the meteor circle
            int numberOfMeteors = 5; // Number of meteors to fall (1 per second)
            double damage = 10.0; // Damage dealt by each meteor
            int fireTicks = 60; // Duration of fire effect in ticks (3 seconds)
            int durationTicks = 100; // Duration of the entire event (5 seconds)

            // Play evacuation sound at the start
            world.playSound(targetLocation, Sound.ENTITY_ILLUSIONER_PREPARE_BLINDNESS, 5.0F, 0F);

            // Create a list to store the locations where meteors will fall
            List<Location> meteorLocations = new ArrayList<>();
            for (int i = 0; i < numberOfMeteors; i++) {
                double angle = 360.0 / numberOfMeteors * i;
                double rad = Math.toRadians(angle);
                double x = radius * Math.cos(rad);
                double z = radius * Math.sin(rad);
                meteorLocations.add(targetLocation.clone().add(x, 1.0, z)); // Start just above the target location
            }

            // Create and schedule a runnable for the particle effect
            new BukkitRunnable() {
                int tickCount = 0;
                Particle.DustOptions dustOptions = new Particle.DustOptions(org.bukkit.Color.RED, 1.0F);

                @Override
                public void run() {
                    if (tickCount >= durationTicks) {
                        this.cancel();
                        return;
                    }

                    // Highlight the area with Redstone particles using DustOptions
                    for (double angle = 0; angle < 360; angle += 1) {
                        double rad = Math.toRadians(angle);
                        double x = radius * Math.cos(rad);
                        double z = radius * Math.sin(rad);
                        Location particleLocation = targetLocation.clone().add(x, 1, z); // Ensure particles are visible above ground
                        world.spawnParticle(Particle.REDSTONE, particleLocation, 3, dustOptions);
                    }

                    tickCount += 5;
                }
            }.runTaskTimer(HubPvP.instance(), 0, 5); // Run every 5 ticks (0.25 seconds)

            // Summon meteors and apply effects
            new BukkitRunnable() {
                int meteorIndex = 0;

                @Override
                public void run() {
                    if (meteorIndex < meteorLocations.size()) {
                        Location meteorLocation = meteorLocations.get(meteorIndex);
                        Location spawnLocation = meteorLocation.clone().add(0, 50, 0); // Spawn point in the sky

                        // Spawn a fireball to simulate the meteor
                        Fireball fireball = (Fireball) world.spawnEntity(spawnLocation, org.bukkit.entity.EntityType.FIREBALL);
                        fireball.setDirection(new Vector(0, -0.2, 0)); // Fall slowly downwards
                        fireball.setIsIncendiary(false); // Do not cause fire
                        fireball.setYield(0); // Do not cause explosion damage

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (fireball.isValid()) {
                                    Location impactLocation = fireball.getLocation();
                                    fireball.remove();

                                    // Simulate explosion with particles and sound
                                    world.spawnParticle(Particle.LAVA, impactLocation, 10);
                                    world.playSound(impactLocation, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.0f, 0.5f);

                                    // Apply effects to entities in the area
                                    for (Entity entity : world.getNearbyEntities(impactLocation, 3, 3, 3)) {
                                        if (entity instanceof Player && !(entity.equals(player))) {
                                            Player target = (Player) entity;
                                            if (HubPvP.instance().pvpManager().isInPvP(target)) {
                                                target.damage(damage, player);
                                                target.setFireTicks(fireTicks);
                                            }
                                        }
                                    }
                                }
                            }
                        }.runTaskLater(HubPvP.instance(), 40); // Fireball impacts after 1 second
                        meteorIndex++;
                    } else {
                        this.cancel(); // End the meteor shower after all meteors have fallen
                    }
                }
            }.runTaskTimer(HubPvP.instance(), 0, 40); // One meteor per second (20 ticks)
        }
    }

    @Override
    public int getCooldown() {
        return 60*20; // Cooldown in seconds
    }

    @Override
    public ItemStack getIcon() {
        ItemStack icon = new ItemStack(Material.FIRE_CHARGE);
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&7&l[&c&lMeteor Shower&7&l]"));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Calls down meteors in a targeted area"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Deals damage and sets enemies on fire"));
            lore.add(ChatColor.translateAlternateColorCodes('&', ""));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Cooldown: &c&l60s"));
            meta.setLore(lore);
            icon.setItemMeta(meta);
        }
        return icon;
    }
}
