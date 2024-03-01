package me.quared.hubpvp.listeners;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.core.PvPManager;
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
		if (invType != InventoryType.SMITHING) return;

		boolean isShiftClick = event.isShiftClick();
		ItemStack targetItem;

		if (isShiftClick) {
			// For shift-clicks, the current item is what's being moved.
			targetItem = event.getCurrentItem();
		} else {
			// For normal clicks, check both the cursor and the slot to cover all cases.
			ItemStack cursorItem = event.getCursor();
			ItemStack currentItem = event.getCurrentItem();

			// Determine the item to check based on the context of the click.
			// This is a simplification and might need refinement for complex scenarios.
			targetItem = (cursorItem != null && cursorItem.getType() != Material.AIR) ? cursorItem : currentItem;
		}

		if (targetItem != null && shouldBlockItem(targetItem)) {
			event.setCancelled(true);
			((Player) event.getWhoClicked()).sendMessage(ChatColor.RED + "This item cannot be used in a smithing table.");
		}

		// Now we determine if the item is the one we want to restrict
		if (shouldBlockItem(targetItem)) {
			event.setCancelled(true);
			((Player) event.getWhoClicked()).sendMessage(ChatColor.RED + "This item cannot be used in a smithing table.");
			return;
		}

		// Additional handling for shift-clicks, where we need to consider the direction of the move
		if (isShiftClick && canMoveToSmithingTable(event.getClick(), targetItem)) {
			event.setCancelled(true);
			((Player) event.getWhoClicked()).sendMessage(ChatColor.RED + "This item cannot be used in a smithing table.");
		}
	}

	private boolean shouldBlockItem(ItemStack item) {
		if (item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			if (meta.hasCustomModelData()) {
				int weaponCustomModelData = getWeaponCustomModelData();
				return meta.getCustomModelData() == weaponCustomModelData;
			}
		}
		return false;
	}

	private int getWeaponCustomModelData() {
		ItemMeta weaponMeta = pvpManager.getWeapon().getItemMeta();
		if (weaponMeta != null && weaponMeta.hasCustomModelData()) {
			return weaponMeta.getCustomModelData();
		}
		return -1; // Return an invalid value if there's no custom model data
	}

	private boolean canMoveToSmithingTable(ClickType clickType, ItemStack item) {
		if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT) {
			if (shouldBlockItem(item));
		}
		return true;
	}

}
