package me.quared.hubpvp.listeners;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.core.PvPManager;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Objects;

public class SpellCastListener implements Listener {

    JavaPlugin plugin = JavaPlugin.getPlugin(HubPvP.class);

    @EventHandler
    public void onSpellCast(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        HubPvP instance = HubPvP.instance();
        PvPManager pvpManager = instance.pvpManager();

        if (pvpManager.isInPvP(player)) {
            ItemStack item = e.getItem();
            ItemStack weapon = pvpManager.getWeapon();

            if (item != null && item.getType() == weapon.getType()) {
                ItemMeta meta = item.getItemMeta();
                if( (meta != null && meta.hasCustomModelData()) && meta.getCustomModelData() == Objects.requireNonNull(weapon.getItemMeta()).getCustomModelData()) {
                    if (e.getAction() == Action.RIGHT_CLICK_AIR) {
                        castSpell(player);
                    }
                }
            }
        }
    }

    public void castSpell(Player player) {
        final Location startLocation = player.getEyeLocation();
        final Vector direction = startLocation.getDirection().normalize();
        final double speedPerTick = 10.0 / 20.0; // 3 blocks per second, converted to blocks per tick
        final double maxDistance = 10.0;
        final Color purpleColor = Color.fromRGB(128, 0, 128); // Define custom purple color
        final Particle.DustOptions dustOptions = new Particle.DustOptions(purpleColor, 0.75F); // DustOptions for color and size

        player.getWorld().playSound(startLocation, Sound.ENTITY_EVOKER_CAST_SPELL, SoundCategory.PLAYERS, 1.0f, 1.0f);

        new BukkitRunnable() {
            private double distanceTravelled = 0.0; // Distance traveled along the direction
            private int ticks = 0; // Keep track of ticks for gradual increase

            @Override
            public void run() {
                if (ticks % 5 == 0) { // Play the sound every half second (10 ticks)
                    player.getWorld().playSound(startLocation.clone().add(direction.clone().multiply(distanceTravelled)), Sound.ENTITY_WITHER_SHOOT, SoundCategory.PLAYERS, 0.1f, 2.0f);
                }

                // Gradually increase the density of the spiral trail as it travels
                int particlesPerTick = 6 + ticks / 2; // Increase particle count over time

                for (int i = 0; i < particlesPerTick; i++) {
                    double angle = 2 * Math.PI * i / particlesPerTick + ticks * 0.2; // Adjust angle for spiral
                    double offsetX = Math.cos(angle) * 0.25; // Spiral radius
                    double offsetZ = Math.sin(angle) * 0.25; // Spiral radius

                    Vector offset = new Vector(offsetX, 0, offsetZ);
                    offset = rotateVector(offset, direction);

                    Location particleLocation = startLocation.clone().add(direction.clone().multiply(distanceTravelled)).add(offset);
                    player.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 1, 0, 0, 0, 0, dustOptions);
                }

                distanceTravelled += speedPerTick;
                ticks++; // Increment tick count

                if (distanceTravelled >= maxDistance) {
                    Objects.requireNonNull(startLocation.getWorld()).spawnParticle(Particle.LAVA, startLocation.clone().add(direction.clone().multiply(distanceTravelled)), 10);
                    Location impactLocation = startLocation.clone().add(direction.clone().multiply(distanceTravelled));
                    player.getWorld().playSound(impactLocation, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.0f, 0.5f);
                    cancel();
                }
            }

            private Vector rotateVector(Vector vector, Vector direction) {
                Vector axis = new Vector(0, 1, 0).crossProduct(direction).normalize();
                double angle = Math.acos(new Vector(0, 1, 0).dot(direction));
                return vector.rotateAroundAxis(axis, angle);
            }
        }.runTaskTimer(JavaPlugin.getPlugin(HubPvP.class), 0L, 1L); // Adjust YourPluginClass to match your plugin's main class.
    }
}
