package me.quared.hubpvp.listeners;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.core.OldPlayerData;
import me.quared.hubpvp.core.PvPManager;
import me.quared.hubpvp.core.PvPState;
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

    public DeathListener () {
        itemsToRestoreAfterRespawn = new HashMap<>();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        HubPvP instance = HubPvP.instance();
        PvPManager pvpManager = instance.pvpManager();
        Player victim = e.getEntity();

        // Always disable PvP for the victim, regardless of whether the killer exists
        //pvpManager.disablePvP(victim);
        pvpManager.setPlayerState(victim, PvPState.OFF);

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

        Player killer = victim.getKiller();
        // Proceed only if both killer and victim are in PvP mode
        if (killer != null && pvpManager.isInPvP(victim) && pvpManager.isInPvP(killer)) {
            int healthOnKill = instance.getConfig().getInt("health-on-kill");

            // Restore health to the killer if specified
            if (healthOnKill != -1) {
                double newHealth = Math.min(killer.getHealth() + healthOnKill, killer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                killer.setHealth(newHealth);
                // Send a health-gained message to the killer
                String healthGainedMessage = instance.getConfig().getString("health-gained-message")
                        .replace("%extra%", String.valueOf(healthOnKill))
                        .replace("%killed%", victim.getDisplayName());
                killer.sendMessage(StringUtil.colorize(healthGainedMessage));
            }

            // Send kill messages
            String killedMessage = instance.getConfig().getString("lang.killed")
                    .replace("%killer%", killer.getDisplayName());
            victim.sendMessage(StringUtil.colorize(killedMessage));

            String killedOtherMessage = instance.getConfig().getString("lang.killed-other")
                    .replace("%killed%", victim.getDisplayName());
            killer.sendMessage(StringUtil.colorize(killedOtherMessage));
        }

        // Optionally, you can uncomment these if you decide to keep inventory and level on death
        // e.setKeepInventory(true);
        // e.setKeepLevel(true);

        // Set the victim's selected inventory slot to 0 (first slot)
        //victim.getInventory().setHeldItemSlot(0);

        // Clear the default death message
        e.setDeathMessage("");
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
