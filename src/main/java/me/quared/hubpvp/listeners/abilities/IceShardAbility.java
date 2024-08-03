package me.quared.hubpvp.listeners.abilities;

import me.quared.hubpvp.HubPvP;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IceShardAbility implements Ability {

    @Override
    public void cast(PlayerInteractEvent event) {
        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            World world = player.getWorld();
            Location eyeLocation = player.getEyeLocation();
            Vector direction = eyeLocation.getDirection();

            // Define parameters for the Ice Shard ability
            double range = 20.0; // Maximum range of the ice shard
            double damage = 8.0; // Amount of damage dealt by the ice shard
            int freezeTicks = 60; // Duration of the freezing effect in ticks (3 seconds)
            double speed = 1.5; // Speed of the ice shard projectile

            // Create a new BukkitRunnable to handle the ice shard effect
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location shardLocation = eyeLocation.clone();
                    Vector shardVelocity = direction.clone().multiply(speed);

                    // Loop to simulate the ice shard projectile
                    while (shardLocation.distance(eyeLocation) < range) {
                        shardLocation.add(shardVelocity);

                        // Check for entities in the path of the ice shard
                        for (Entity entity : world.getNearbyEntities(shardLocation, 1.0, 1.0, 1.0)) {
                            if (entity instanceof LivingEntity && !(entity.equals(player))) {
                                LivingEntity target = (LivingEntity) entity;

                                if (target instanceof Player) {
                                    Player targetPlayer = (Player) target;
                                    if (!HubPvP.instance().pvpManager().isInPvP(targetPlayer)) {
                                        return;
                                    }
                                }

                                // Apply damage and freezing effect
                                target.damage(damage, player);
                                target.setFreezeTicks(60);

                                // Spawn ice particles
                                world.spawnParticle(Particle.SNOW_SHOVEL, shardLocation, 10, 0.5, 0.5, 0.5, 0.1);
                                world.playSound(shardLocation, Sound.BLOCK_GLASS_BREAK, 1.0F, 1.0F);

                                // End the effect once the target is hit
                                return;
                            }
                        }

                        // Optional: Spawn visual effect for the ice shard
                        world.spawnParticle(Particle.SNOWBALL, shardLocation, 5, 0.1, 0.1, 0.1, 0.1);
                    }
                }
            }.runTaskLater(HubPvP.instance(), 0L); // Run immediately

            // Play a sound effect for the Ice Shard ability
            Objects.requireNonNull(player.getLocation().getWorld()).playSound(player.getLocation(), Sound.ENTITY_SNOW_GOLEM_SHOOT, 1.0F, 1.0F);
        }
    }

    @Override
    public int getCooldown() {
        return 10*20; // Cooldown in seconds
    }

    @Override
    public ItemStack getIcon() {
        ItemStack icon = new ItemStack(Material.SNOWBALL); // You can use a different material if you prefer
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&lIce Shard"));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Shoots a shard of ice"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Deals damage and slows enemies"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7in its path"));
            lore.add(ChatColor.translateAlternateColorCodes('&', ""));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Cooldown: &b&l10s"));
            meta.setLore(lore);
            icon.setItemMeta(meta);
        }
        return icon;
    }
}
