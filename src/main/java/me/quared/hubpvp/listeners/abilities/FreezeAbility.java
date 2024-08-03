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
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FreezeAbility implements Ability {

    @Override
    public void cast(PlayerInteractEvent event) {
        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            World world = player.getWorld();
            Location eyeLocation = player.getEyeLocation();
            Vector direction = eyeLocation.getDirection();

            // Define the range and width of the freeze effect
            double range = 15.0;
            double width = 3.0; // Width of the effect
            int freeze_ticks = 60; // Duration of freeze effect in ticks

            // Create a location object to track the position of the freeze
            Location currentLocation = eyeLocation.clone();

            for (double d = 0; d < range; d += 0.5) {
                currentLocation.add(direction.clone().multiply(0.5));
                // Spawn ice particles to represent the freeze effect
                world.spawnParticle(Particle.SNOWFLAKE, currentLocation, 10, width, width, width, 0.1);
                world.spawnParticle(Particle.SNOWBALL, currentLocation, 5, width, width, width, 0.1);

                // Check for entities in the area of effect
                for (Entity entity : currentLocation.getWorld().getNearbyEntities(currentLocation, width, width, width)) {
                    if (entity instanceof Player && !entity.equals(player)) {
                        Player target = (Player) entity;

                        if (HubPvP.instance().pvpManager().isInPvP(target)) {
                            // Apply the freeze effect
                            target.setFreezeTicks(freeze_ticks); // Freeze the entity
                        }
                    }
                }
            }

            // Play a sound effect for the freeze ability
            Objects.requireNonNull(player.getLocation().getWorld()).playSound(player.getLocation(), Sound.BLOCK_SNOW_BREAK, 1.0F, 1.0F);
        }
    }

    @Override
    public int getCooldown() {
        return 300; // Cooldown in seconds
    }

    @Override
    public ItemStack getIcon() {
        ItemStack icon = new ItemStack(Material.ICE);
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&7&l[&b&lFreeze&7&l]"));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Freezes entities in front of you"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Prevents them from moving temporarily"));
            lore.add(ChatColor.translateAlternateColorCodes('&', ""));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Cooldown: &b&l30s"));
            meta.setLore(lore);
            icon.setItemMeta(meta);
        }
        return icon;
    }
}
