package me.quared.hubpvp.listeners.abilities;

import me.quared.hubpvp.HubPvP;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Bat;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BatmanAbility implements Ability {

    @Override
    public void cast(PlayerInteractEvent event) {
        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            World world = player.getWorld();
            Location eyeLocation = player.getEyeLocation();
            Vector direction = eyeLocation.getDirection().normalize();

            // Define parameters for the Batman ability
            double range = 20.0; // Range in blocks to carry target players
            int numberOfBats = 10; // Number of bats to spawn
            int durationTicks = 100; // 5 seconds in ticks (20 ticks per second)
            double speed = 1.0; // Speed of bats flying in direction

            // Create a list to store the spawned bats
            List<Bat> spawnedBats = new ArrayList<>();

            // Spawn bats and make them fly in the direction the player is looking
            for (int i = 0; i < numberOfBats; i++) {
                Location spawnLocation = eyeLocation.clone().add(direction.clone().multiply(i * 0.5));
                Bat bat = (Bat) world.spawnEntity(spawnLocation, org.bukkit.entity.EntityType.BAT);

                // Set the bat to fly in the direction
                bat.setVelocity(direction.clone().multiply(speed));

                // Add bat to the list of spawned bats
                spawnedBats.add(bat);

                // Optional: Add visual effect like particles around the spawn location
                world.spawnParticle(Particle.SMOKE_LARGE, spawnLocation, 10, 0.5, 0.5, 0.5, 0.1);
            }

            // Play a sound effect for the Batman ability
            Objects.requireNonNull(player.getLocation().getWorld()).playSound(player.getLocation(), Sound.ENTITY_BAT_AMBIENT, 1.0F, 1.0F);

            // Schedule task to gradually lift players and remove bats after the duration
            new BukkitRunnable() {
                @Override
                public void run() {
                   removeBats(spawnedBats);
                }
            }.runTaskLater(HubPvP.instance(), durationTicks);
        }
    }

    private void removeBats(List<Bat> spawnedBats) {
        for (Bat bat : spawnedBats) {
            if (bat.isValid()) {
                bat.remove();
            }
        }
    }

    @Override
    public int getCooldown() {
        return 300; // Cooldown in seconds
    }

    @Override
    public ItemStack getIcon() {
        ItemStack icon = new ItemStack(Material.BAT_SPAWN_EGG);
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&7&l[&0&lBatman&7&l]"));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Spawns a swarm of bats"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Bats will lift any player they encounter"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Bats will fly in the direction you're looking"));
            lore.add(ChatColor.translateAlternateColorCodes('&', ""));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Cooldown: &0&l30s"));
            meta.setLore(lore);
            icon.setItemMeta(meta);
        }
        return icon;
    }
}
