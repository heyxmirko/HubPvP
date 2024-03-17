package me.quared.hubpvp.listeners;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.core.PvPManager;
import me.quared.hubpvp.core.PvPState;
import me.quared.hubpvp.util.StringUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import javax.swing.*;
import java.util.Objects;

public class ItemSlotChangeListener implements Listener {

	private PvPManager pvpManager;

	public ItemSlotChangeListener() {
		this.pvpManager = HubPvP.instance().pvpManager();
	}

	@EventHandler
	public void onSwordDrop(PlayerDropItemEvent e) {
		ItemStack droppedItem = e.getItemDrop().getItemStack();
		if (droppedItem.getType() != pvpManager.getWeapon().getType()) return;
		if (!droppedItem.hasItemMeta()) return;
		if (!droppedItem.getItemMeta().hasCustomModelData()) return;
		if (droppedItem.getItemMeta().getCustomModelData() != pvpManager.getWeapon().getItemMeta().getCustomModelData()) return;
		Player p = e.getPlayer();
		PvPState pvpState = pvpManager.getPlayerState(e.getPlayer());
		if (pvpState == PvPState.ON || pvpState == PvPState.ENABLING) {
			pvpManager.disablePvP(p);
		}
	}

	@EventHandler
	public void onSlotChange(PlayerItemHeldEvent e) {
		Player p = e.getPlayer();
		ItemStack held = e.getPlayer().getInventory().getItem(e.getNewSlot());
		HubPvP instance = HubPvP.instance();
		PvPManager pvpManager = instance.pvpManager();

		if (!p.hasPermission("hubpvp.use")) return;

		if (hasMatchingCustomModelData(held, pvpManager.getWeapon())) {
			if (pvpManager.getPlayerState(p) == PvPState.DISABLING) pvpManager.setPlayerState(p, PvPState.ON);
			if (pvpManager.getPlayerState(p) == PvPState.ENABLING) return;

			if (HubPvP.instance().getConfig().getStringList("disabled-worlds").contains(p.getWorld().getName())) {
				p.sendMessage(StringUtil.colorize(instance.getConfig().getString("lang.disabled-in-world")));
				return;
			}

			// Equipping
			if (!pvpManager.isInPvP(p)) {
				pvpManager.setPlayerState(p, PvPState.ENABLING);
				BukkitRunnable enableTask = new BukkitRunnable() {
					int time = instance.getConfig().getInt("enable-cooldown") + 1;

					public void run() {
						time--;
						if (pvpManager.getPlayerState(p) != PvPState.ENABLING || !hasMatchingCustomModelData(held, pvpManager.getWeapon())) {
							pvpManager.removeTimer(p);
							cancel();
						} else if (time == 0) {
							pvpManager.enablePvP(p);
							pvpManager.removeTimer(p);
							cancel();
						} else {
							p.sendMessage(StringUtil.colorize(instance.getConfig().getString("lang.pvp-enabling").replaceAll("%time%", Integer.toString(time))));
							p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5F, 2.0F);
						}
					}
				};
				pvpManager.putTimer(p, enableTask);
				enableTask.runTaskTimer(instance, 0L, 20L);
			}
		} else if (pvpManager.isInPvP(p)) {
			if (pvpManager.getPlayerState(p) == PvPState.ENABLING) pvpManager.setPlayerState(p, PvPState.OFF);
			if (pvpManager.getPlayerState(p) == PvPState.DISABLING) return;
			// Dequipping
			pvpManager.setPlayerState(p, PvPState.DISABLING);
			BukkitRunnable disableTask = new BukkitRunnable() {
				int time = instance.getConfig().getInt("disable-cooldown") + 1;

				public void run() {
					time--;
					if (pvpManager.getPlayerState(p) != PvPState.DISABLING || hasMatchingCustomModelData(held, pvpManager.getWeapon())) {
						pvpManager.removeTimer(p);
						cancel();
					} else if (time == 0) {
						pvpManager.disablePvP(p);
						pvpManager.removeTimer(p);
						cancel();
					} else {
						p.sendMessage(StringUtil.colorize(instance.getConfig().getString("lang.pvp-disabling").replaceAll("%time%", Integer.toString(time))));
					}
				}
			};
			pvpManager.putTimer(p, disableTask);
			disableTask.runTaskTimer(instance, 0L, 20L);
		} else {
			// Not in PvP and not equipping
			pvpManager.setPlayerState(p, PvPState.OFF); // Ensure there isn't any lingering state
			pvpManager.removeTimer(p);
		}
	}

	private boolean hasMatchingCustomModelData(ItemStack item1, ItemStack item2) {
		if (item1 == null || item2 == null) return false;
		ItemMeta meta1 = item1.getItemMeta();
		ItemMeta meta2 = item2.getItemMeta();
		if (meta1 != null && meta2 != null && meta1.hasCustomModelData() && meta2.hasCustomModelData()) {
			return meta1.getCustomModelData() == meta2.getCustomModelData();
		}
		return false;
	}
}
