package me.quared.hubpvp.listeners;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.core.OldPlayerData;
import me.quared.hubpvp.core.PvPManager;
import me.quared.hubpvp.core.PvPState;
import me.quared.hubpvp.core.RegionManager;
import me.quared.hubpvp.util.StringUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class DeathListener implements Listener {

    private Map<UUID, OldPlayerData> itemsToRestoreAfterRespawn;
    private RegionManager regionManager;

    public DeathListener () {
        itemsToRestoreAfterRespawn = new HashMap<>();
        this.regionManager = new RegionManager();
    }


    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        HubPvP instance = HubPvP.instance();
        PvPManager pvpManager = instance.pvpManager();
        Player victim = e.getEntity();

        // Remove the player from the PvP state and region
        pvpManager.setPlayerState(victim, PvPState.OFF);
        regionManager.removePlayerFromRegion(victim.getUniqueId());

        // Store the player's armor and flight status for later restoration after respawn
        OldPlayerData oldPlayerData = pvpManager.getOldPlayerDataMap().get(victim.getUniqueId());
        if (oldPlayerData != null) {
            itemsToRestoreAfterRespawn.put(victim.getUniqueId(), oldPlayerData);
        }

        // Check drop for pvp armor
        List<ItemStack> itemsToRemove = new ArrayList<>();
        for (ItemStack item : e.getDrops()) {
            if (item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.hasCustomModelData() && pvpManager.getArmorCustomModelData().contains(meta.getCustomModelData())) {
                    itemsToRemove.add(item);
                }
            }
        }
        // Remove all matching items from the drops
        e.getDrops().removeAll(itemsToRemove);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        HubPvP instance = HubPvP.instance();
        Player p = e.getPlayer();

        OldPlayerData oldPlayerData = itemsToRestoreAfterRespawn.get(p.getUniqueId());
        if (oldPlayerData != null) {
            p.getInventory().setHelmet(oldPlayerData.armor()[3] == null ? new ItemStack(Material.AIR) : oldPlayerData.armor()[3]);
            p.getInventory().setChestplate(oldPlayerData.armor()[2] == null ? new ItemStack(Material.AIR) : oldPlayerData.armor()[2]);
            p.getInventory().setLeggings(oldPlayerData.armor()[1] == null ? new ItemStack(Material.AIR) : oldPlayerData.armor()[1]);
            p.getInventory().setBoots(oldPlayerData.armor()[0] == null ? new ItemStack(Material.AIR) : oldPlayerData.armor()[0]);
            p.setAllowFlight(oldPlayerData.canFly());

            itemsToRestoreAfterRespawn.remove(p.getUniqueId());
        }


        ConfigurationSection respawnSection = instance.getConfig().getConfigurationSection("respawn");

        if (!respawnSection.getBoolean("enabled")) return;

        if (respawnSection.getBoolean("use-world-spawn", false)) {
            e.setRespawnLocation(p.getWorld().getSpawnLocation());
        } else {
            Location spawn = new Location(
                    p.getWorld(),
                    respawnSection.getDouble("x"),
                    respawnSection.getDouble("y"),
                    respawnSection.getDouble("z"),
                    respawnSection.getInt("yaw"),
                    respawnSection.getInt("pitch")
            );
            e.setRespawnLocation(spawn);
        }
    }

}
