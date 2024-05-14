package me.quared.hubpvp.listeners;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.core.PvPManager;
import me.quared.hubpvp.core.PvPState;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


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
	public void swapHandItems(PlayerSwapHandItemsEvent event) {
		ItemStack mainHandItem = event.getOffHandItem();
		if (mainHandItem == null) return;
		if (mainHandItem.getType() != pvpManager.getWeapon().getType()) return;
		if (!mainHandItem.hasItemMeta()) return;
		if (!mainHandItem.getItemMeta().hasCustomModelData()) return;
		if (mainHandItem.getItemMeta().getCustomModelData() == pvpManager.getWeapon().getItemMeta().getCustomModelData()) {
			pvpManager.disablePvP(event.getPlayer());
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) return;

		ItemStack currentItem = event.getCurrentItem();
		if (currentItem == null) return;

		Player p = ((Player) event.getWhoClicked()).getPlayer();

		InventoryType invType = event.getInventory().getType();
		if (pvpManager.getPlayerState(p) == PvPState.ENABLING || pvpManager.getPlayerState(p) == PvPState.ON) {

			if (invType == InventoryType.CRAFTING) {
				if (event.getAction() == InventoryAction.HOTBAR_SWAP) {
					//Get the item from the hotbar
					ItemStack hotBarItem = p.getInventory().getItem(event.getHotbarButton());
					if (hotBarItem != null) {
						if (hotBarItem.getType() == pvpManager.getWeapon().getType()) {
							if (hotBarItem.hasItemMeta()) {
								ItemMeta hotBarItemMeta = hotBarItem.getItemMeta();
								if (hotBarItemMeta.hasCustomModelData()) {
									if (hotBarItemMeta.getCustomModelData() == pvpManager.getWeapon().getItemMeta().getCustomModelData()) {
										pvpManager.disablePvP(((Player) event.getWhoClicked()).getPlayer());
									}
								}
							}
						}
					}

				}
			}

			if (currentItem.getType() != pvpManager.getWeapon().getType()) return;
			if (!currentItem.hasItemMeta()) return;

			ItemMeta itemMeta = currentItem.getItemMeta();
			if (!itemMeta.hasCustomModelData()) return;
			int customModelData = itemMeta.getCustomModelData();

			if (customModelData == pvpManager.getWeapon().getItemMeta().getCustomModelData()) {
				pvpManager.disablePvP(((Player) event.getWhoClicked()).getPlayer());
			}
		}

		if (!(invType == InventoryType.SMITHING || invType == InventoryType.ANVIL || invType == InventoryType.ENCHANTING)) return;

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
