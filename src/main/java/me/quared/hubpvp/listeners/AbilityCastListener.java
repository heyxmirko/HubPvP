package me.quared.hubpvp.listeners;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.core.PvPManager;
import me.quared.hubpvp.listeners.abilities.PurpleStrikeAbility;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import me.quared.hubpvp.listeners.abilities.Ability;

public class AbilityCastListener implements Listener {

    private JavaPlugin plugin = JavaPlugin.getPlugin(HubPvP.class);

    private Ability hardcodedAbility = new PurpleStrikeAbility(); // Hardcoded ability

    @EventHandler
    public void onAbilityCast(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItem();

        if (!isPlayerInPvPAndHoldingCorrectWeapon(player, item)) {
            return;
        }

        Action action = e.getAction();
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (player.getCooldown(item.getType()) > 0) {
                player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 0.1F, 1.0F);
                return;
            }

            hardcodedAbility.cast(e);
            player.setCooldown(item.getType(), hardcodedAbility.getCooldown());
        }
    }

    private boolean isPlayerInPvPAndHoldingCorrectWeapon(Player player, ItemStack item) {
        HubPvP instance = HubPvP.instance();
        PvPManager pvpManager = instance.pvpManager();
        if (!pvpManager.isInPvP(player)) {
            return false;
        }

        // Check if the player is holding a diamond sword
        return item != null && item.getType() == Material.DIAMOND_SWORD;
    }
}
