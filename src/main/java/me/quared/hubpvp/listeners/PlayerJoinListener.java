package me.quared.hubpvp.listeners;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.core.OldPlayerData;
import me.quared.hubpvp.core.PvPManager;
import me.quared.hubpvp.core.PvPState;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private Map<UUID, OldPlayerData> itemsToRestoreAfterRejoin;

    public PlayerJoinListener() {
        itemsToRestoreAfterRejoin = new HashMap<>();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        PvPManager pvPManager = HubPvP.instance().pvpManager();

        // Now relaying on giving weapon by command /hubpvp giveWeapon
        /*if (p.hasPermission("hubpvp.use") &&
                !HubPvP.instance().getConfig().getStringList("disabled-worlds").contains(p.getWorld().getName())) {
            pvPManager.giveWeapon(p);
        }*/

        pvPManager.setPlayerState(p, PvPState.OFF);

        OldPlayerData oldPlayerData = itemsToRestoreAfterRejoin.get(p.getUniqueId());
        if (oldPlayerData != null) {
            p.getInventory().setHelmet(oldPlayerData.armor()[3] == null ? new ItemStack(Material.AIR) : oldPlayerData.armor()[3]);
            p.getInventory().setChestplate(oldPlayerData.armor()[2] == null ? new ItemStack(Material.AIR) : oldPlayerData.armor()[2]);
            p.getInventory().setLeggings(oldPlayerData.armor()[1] == null ? new ItemStack(Material.AIR) : oldPlayerData.armor()[1]);
            p.getInventory().setBoots(oldPlayerData.armor()[0] == null ? new ItemStack(Material.AIR) : oldPlayerData.armor()[0]);
            p.setAllowFlight(oldPlayerData.canFly());

            itemsToRestoreAfterRejoin.remove(p.getUniqueId());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        PvPManager pvpManager = HubPvP.instance().pvpManager();

        OldPlayerData oldPlayerData = pvpManager.getOldPlayerDataMap().get(p.getUniqueId());
        if (oldPlayerData != null) {
            itemsToRestoreAfterRejoin.put(p.getUniqueId(), oldPlayerData);
        }

        pvpManager.removePlayer(p);
    }

}
