package me.quared.hubpvp.listeners.abilities;

import me.quared.hubpvp.HubPvP;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;
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

public class BeeKeeperAbility implements Ability {

    @Override
    public void cast(PlayerInteractEvent event) {
        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            World world = player.getWorld();
            Location eyeLocation = player.getEyeLocation();
            Vector direction = eyeLocation.getDirection();

            // Define the parameters for the Bee Keeper ability
            double range = 10.0; // Range in blocks to attack target players
            int numberOfBees = 5; // Number of bees to spawn
            int durationTicks = 200; // 10 seconds in ticks (20 ticks per second)

            // Define the spawn location above the player's head
            Location spawnLocation = player.getLocation().add(0, 2, 0);

            // Create a list to store the spawned bees
            List<Bee> spawnedBees = new ArrayList<>();

            // Spawn bees and make them target players within the range
            for (int i = 0; i < numberOfBees; i++) {
                Bee bee = (Bee) world.spawnEntity(spawnLocation, org.bukkit.entity.EntityType.BEE);

                // Add bee to the list of spawned bees
                spawnedBees.add(bee);

                // Make sure the bee targets a player in range
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (Entity entity : world.getNearbyEntities(player.getLocation(), range, range, range)) {
                            if (entity instanceof Player && !entity.equals(player)) {
                                Player target = (Player) entity;
                                if (HubPvP.instance().pvpManager().isInPvP(target)) {
                                    target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 1));



                                    bee.setTarget(target); // Set the target of the bee to the player
                                }
                            }
                        }
                    }
                }.runTaskLater(HubPvP.instance(), i * 5); // Stagger bee targeting

                // Optional: Add visual effect like particles around the spawn location
                world.spawnParticle(Particle.CLOUD, spawnLocation, 10, 0.5, 0.5, 0.5, 0.1);
            }

            // Play a sound effect for the BeeKeeper ability
            Objects.requireNonNull(player.getLocation().getWorld()).playSound(player.getLocation(), Sound.ENTITY_BEE_LOOP_AGGRESSIVE, 1.0F, 1.0F);

            // Schedule bees to despawn after the duration
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Bee bee : spawnedBees) {
                        if (bee.isValid()) {
                            bee.remove(); // Remove the bee from the world
                        }
                    }
                }
            }.runTaskLater(HubPvP.instance(), durationTicks); // Schedule the cleanup task
        }
    }

    @Override
    public int getCooldown() {
        return 300; // Cooldown in seconds
    }

    @Override
    public ItemStack getIcon() {
        ItemStack icon = new ItemStack(Material.BEE_SPAWN_EGG);
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&7&l[&e&lBee Keeper&7&l]"));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Summons a swarm of bees"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Bees attack any player within 10 blocks"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Duration: 10 seconds"));
            lore.add(ChatColor.translateAlternateColorCodes('&', ""));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Cooldown: &e&l30s"));
            meta.setLore(lore);
            icon.setItemMeta(meta);
        }
        return icon;
    }
}
