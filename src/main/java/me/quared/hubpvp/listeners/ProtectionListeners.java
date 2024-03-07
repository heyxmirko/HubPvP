package me.quared.hubpvp.listeners;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.core.PvPManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class ProtectionListeners implements Listener {

	private PvPManager pvpManager;

	public ProtectionListeners() {
		pvpManager = HubPvP.instance().pvpManager();
	}

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		ItemStack item = e.getCurrentItem();
		if (item == null) return;

		if (pvpManager.isInPvP(p)) {
			if (item.isSimilar(pvpManager.getWeapon())) {
				e.setCancelled(true);
			} else if (e.getSlotType() == InventoryType.SlotType.ARMOR) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		Player p = e.getPlayer();
		ItemStack item = e.getItemDrop().getItemStack();
		PvPManager pvPManager = HubPvP.instance().pvpManager();

		if (pvPManager.isInPvP(p)) {
			if (item.isSimilar(pvPManager.getWeapon())) {
				e.setCancelled(true);
			} else if (item.getType().toString().toLowerCase().contains("armor")) { // very bad way of doing this, feel free to make a new branch to update
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) return;
		InventoryType invType = event.getInventory().getType();


		if (!(invType == InventoryType.SMITHING || invType == InventoryType.ANVIL || invType == InventoryType.ENCHANTING)) return;

		ItemStack currentItem = event.getCurrentItem();

		if (currentItem == null) return;
		if (currentItem.getType() != pvpManager.getWeapon().getType()) return;
		if (!currentItem.hasItemMeta()) return;

		ItemMeta itemMeta = currentItem.getItemMeta();
		if (!itemMeta.hasCustomModelData()) return;
		int customModelData = itemMeta.getCustomModelData();

		if (customModelData == pvpManager.getWeapon().getItemMeta().getCustomModelData()) {
			event.setCancelled(true);
			event.getWhoClicked().sendMessage(ChatColor.RED + "You cannot upgrade the PvP Sword!");
		}
	}
}
