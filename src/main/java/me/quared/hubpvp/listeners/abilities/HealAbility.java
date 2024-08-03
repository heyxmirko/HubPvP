package me.quared.hubpvp.listeners.abilities;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class HealAbility implements Ability {

    private boolean applyCooldown = true;

    @Override
    public void cast(PlayerInteractEvent event) {
        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            double maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();

            if (player.getHealth() >= maxHealth) {
                player.sendMessage(ChatColor.GREEN + "You are already at full health!");
                applyCooldown = false;
                return;
            }

            double newHealth = Math.min(player.getHealth() + 8.0, maxHealth);
            player.setHealth(newHealth);
            player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 1.0F);

            // Spawn emerald particles around the player
            for (int i = 0; i < 50; i++) {
                double x = (Math.random() - 0.5) * 2;
                double y = Math.random();
                double z = (Math.random() - 0.5) * 2;
                player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(x, y, z), 1);
            }
        }
    }

    @Override
    public int getCooldown() {
        if (!applyCooldown) {
            applyCooldown = true;
            return 0;
        }
        return 30 * 20;
    }

    @Override
    public ItemStack getIcon() {
        ItemStack icon = new ItemStack(Material.EMERALD);
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&2&lInstant Heal"));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Heals you for 4 hearts."));
            lore.add(ChatColor.translateAlternateColorCodes('&', ""));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Cooldown: &2&l30s"));
            meta.setLore(lore);
            icon.setItemMeta(meta);
        }
        return icon;
    }
}
