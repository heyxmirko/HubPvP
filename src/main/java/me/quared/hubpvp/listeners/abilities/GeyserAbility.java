package me.quared.hubpvp.listeners.abilities;

import me.quared.hubpvp.HubPvP;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class GeyserAbility implements Ability {

    @Override
    public void cast(PlayerInteractEvent event) {
        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            World world = player.getWorld();
            Location eyeLocation = player.getEyeLocation();
            Vector direction = eyeLocation.getDirection();

            // Define parameters for the Geyser ability
            double range = 20.0; // Maximum range to detect targets
            double radius = 5.0; // Radius to search for targets
            double height = 10.0; // Height to propel players into the air
            double delay = 20L; // Delay before the geyser effect activates (in ticks, 1 tick = 1/20 second)
            double glowDuration = 100; // Duration of the blue glow effect in ticks (5 seconds)

            // Play a sound effect for the Geyser ability
            player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0F, 1.0F);

            // Detect entities within the specified radius
            for (Entity entity : player.getNearbyEntities(range, range, range)) {
                if (entity instanceof LivingEntity && !(entity.equals(player))) {
                    LivingEntity target = (LivingEntity) entity;

                    // Check if the entity is within the defined radius
                    if (target.getLocation().distance(player.getLocation()) <= radius) {
                        // Apply a blue glow effect to the player
                        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, (int) glowDuration, 1, false, false, false));

                        // Create a new BukkitRunnable to handle the geyser effect
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Location targetLocation = target.getLocation().clone().add(0, 1, 0); // Spawn the geyser slightly above the target

                                // Create the geyser effect
                                for (double d = 0; d < height; d += 0.5) {
                                    Location geyserLocation = targetLocation.clone().add(0, d, 0);
                                    world.spawnParticle(Particle.WATER_BUBBLE, geyserLocation, 10, 0.5, 0.5, 0.5, 0.1);
                                    world.spawnParticle(Particle.WATER_SPLASH, geyserLocation, 10, 0.5, 0.5, 0.5, 0.1);
                                    world.playSound(geyserLocation, Sound.ENTITY_GENERIC_SPLASH, 1.0F, 1.0F);
                                }

                                // Propel the player into the air
                                target.setVelocity(new Vector(0, 1, 0).multiply(height));

                                // Optional: Create additional visual effects if needed
                            }
                        }.runTaskLater((Plugin) HubPvP.instance(), (long) delay);
                    }
                }
            }
        }
    }

    @Override
    public int getCooldown() {
        return 30; // Cooldown in seconds
    }

    @Override
    public ItemStack getIcon() {
        ItemStack icon = new ItemStack(Material.WATER_BUCKET); // Use a water-related item for the icon
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&lGeyser"));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Spawns a geyser beneath target players"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Propels them into the air"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7and applies a blue glow effect"));
            lore.add(ChatColor.translateAlternateColorCodes('&', ""));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Cooldown: &b&l30s"));
            meta.setLore(lore);
            icon.setItemMeta(meta);
        }
        return icon;
    }
}
