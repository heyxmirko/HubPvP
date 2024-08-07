package me.quared.hubpvp.core;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.listeners.abilities.Ability;
import me.quared.hubpvp.managers.PermissionManager;
import me.quared.hubpvp.managers.RegionManager;
import me.quared.hubpvp.util.ArmorSerializationUtil;
import me.quared.hubpvp.util.StringUtil;
import me.quared.hubpvp.util.adapters.ItemMetaAdapter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import nl.marido.deluxecombat.api.DeluxeCombatAPI;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;
import java.util.List;

public class PvPManager {

	private final RegionManager regionManager;
	private final Map<Player, PvPState> playerPvpStates;
	private final Map<Player, BukkitRunnable> currentTimers;
	private final Map<UUID, OldPlayerData> oldPlayerDataMap;
	private final DeluxeCombatAPI deluxeCombatAPI;
	public Map<UUID, OldPlayerData> getOldPlayerDataMap() {
		return this.oldPlayerDataMap;
	}
	private ItemStack weapon, helmet, chestplate, leggings, boots;
	private Map<UUID, Ability> selectedAbilities;

	// Constructor
	public PvPManager() {
		this.regionManager = new RegionManager();
		playerPvpStates = new HashMap<>();
		currentTimers = new HashMap<>();
		oldPlayerDataMap = new HashMap<>();
		this.deluxeCombatAPI = new DeluxeCombatAPI();
		this.selectedAbilities = new HashMap<>();
		loadItemsFromConfig();
	}


	// Getters
	public ItemStack getWeapon(){
		return this.weapon;
	}
	public ItemStack getHelmet() {
		return this.helmet;
	}
	public ItemStack getChestplate() {
		return this.chestplate;
	}
	public ItemStack getLeggings() {
		return this.leggings;
	}
	public ItemStack getBoots() {
		return this.boots;
	}
	public Map<Player, BukkitRunnable> getCurrentTimers() {
		return this.currentTimers;
	}
	public List<Integer> getArmorCustomModelData() {
		List<Integer> list = new ArrayList<>();
		list.add(Objects.requireNonNull(getHelmet().getItemMeta()).getCustomModelData());
		list.add(Objects.requireNonNull(getChestplate().getItemMeta()).getCustomModelData());
		list.add(Objects.requireNonNull(getLeggings().getItemMeta()).getCustomModelData());
		list.add(Objects.requireNonNull(getBoots().getItemMeta()).getCustomModelData());
		return list;
	}
	public PvPState getPlayerState(Player p) {
		return playerPvpStates.get(p);
	}

	// Methods
	public void loadItemsFromConfig() {
		this.weapon = getItemFromConfig("weapon");
		this.helmet = getItemFromConfig("helmet");
		this.chestplate = getItemFromConfig("chestplate");
		this.leggings = getItemFromConfig("leggings");
		this.boots = getItemFromConfig("boots");
	}

	public void enablePvP(Player player) {
		setPlayerState(player, PvPState.ON);
		PermissionManager.assignPermission(player, "duels.duel", false);

		if (getOldPlayerDataFromMap(player) != null) getOldPlayerDataMap().remove(player.getUniqueId());
		OldPlayerData oldPlayerData = new OldPlayerData(player.getUniqueId(), player.getInventory().getArmorContents(), player.getAllowFlight());
		getOldPlayerDataMap().put(player.getUniqueId(), oldPlayerData);

		player.setAllowFlight(false);
		player.getInventory().setHelmet(getHelmet());
		player.getInventory().setChestplate(getChestplate());
		player.getInventory().setLeggings(getLeggings());
		player.getInventory().setBoots(getBoots());

		if (!deluxeCombatAPI.isInCombat(player)) {
			regionManager.addPlayerToRegion(player.getUniqueId());
		}

		player.sendMessage(StringUtil.colorize(HubPvP.instance().getConfig().getString("lang.pvp-enabled")));
		// to change abilities press F
		player.sendMessage(StringUtil.colorize("&7Press &l[&9&lF&7&l] &7to change abilities"));
		player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5F, 2.0F);
		player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1.0F, 1.0F);
		if (!selectedAbilities.containsKey(player.getUniqueId())) {
			this.setSelectedAbilities(player.getUniqueId(), Ability.getDefaultAbility());
		}
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(StringUtil.colorize("Selected Ability: " + selectedAbilities.get(player.getUniqueId()).getIcon().getItemMeta().getDisplayName())));
	}

	public void disablePvP(Player player) {
		setPlayerState(player, PvPState.OFF);
		PermissionManager.assignPermission(player, "duels.duel", true);
		OldPlayerData oldPlayerData = getOldPlayerDataFromMap(player);

		if (oldPlayerData != null) {
			player.getInventory().setHelmet(oldPlayerData.armor()[3] == null ? new ItemStack(Material.AIR) : oldPlayerData.armor()[3]);
			player.getInventory().setChestplate(oldPlayerData.armor()[2] == null ? new ItemStack(Material.AIR) : oldPlayerData.armor()[2]);
			player.getInventory().setLeggings(oldPlayerData.armor()[1] == null ? new ItemStack(Material.AIR) : oldPlayerData.armor()[1]);
			player.getInventory().setBoots(oldPlayerData.armor()[0] == null ? new ItemStack(Material.AIR) : oldPlayerData.armor()[0]);
			player.setAllowFlight(oldPlayerData.canFly());
			oldPlayerDataMap.remove(player.getUniqueId());
		}
		regionManager.removePlayerFromRegion(player.getUniqueId());
		player.sendMessage(StringUtil.colorize(HubPvP.instance().getConfig().getString("lang.pvp-disabled")));
		player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1.F, 1.0F);
	}


	public void setPlayerState(Player p, PvPState state) {
		playerPvpStates.put(p, state);
	}

	public void removePlayer(Player p) {
		this.setPlayerState(p, PvPState.OFF);
		regionManager.removePlayerFromRegion(p.getUniqueId());
		clearArmorFromInventory(p);
	}

	public void disable() {
		for (Player p : playerPvpStates.keySet()) {
			if (isInPvP(p)) disablePvP(p);
		}
		playerPvpStates.clear();
	}

	public boolean isInPvP(Player player) {
		return getPlayerState(player) == PvPState.ON || getPlayerState(player) == PvPState.DISABLING;
	}

	public void putTimer(Player p, BukkitRunnable timerTask) {
		if (getCurrentTimers().containsKey(p)) {
			getCurrentTimers().get(p).cancel();
		}
		getCurrentTimers().put(p, timerTask);
	}

	public void removeTimer(Player p) {
		if (getCurrentTimers().containsKey(p)) {
			getCurrentTimers().get(p).cancel();
		}
		getCurrentTimers().remove(p);
	}


	// Private methods
	private ItemStack getItemFromConfig(String name) {
		HubPvP instance = HubPvP.instance();

		String materialName = instance.getConfig().getString("items." + name + ".material");
		Material material = Material.getMaterial(materialName.toUpperCase());
		ItemStack item = new ItemStack(material, 1);
		ItemMeta meta = item.getItemMeta();

		if (meta != null) {
			String itemName = instance.getConfig().getString("items." + name + ".name");
			if (itemName != null && !itemName.isEmpty()) meta.setDisplayName(StringUtil.colorize(itemName));

			List<String> lore = instance.getConfig().getStringList("items." + name + ".lore");
			if (!lore.isEmpty()) meta.setLore(StringUtil.colorize(lore));

			int customModelData = instance.getConfig().getInt("items." + name + ".custom_model_data", 0);
			if (customModelData != 0) meta.setCustomModelData(customModelData);

			List<String> enchantments = instance.getConfig().getStringList("items." + name + ".enchantments");
			for (String enchString : enchantments) {
				String[] parts = enchString.split(":");
				Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(parts[0].toLowerCase()));
				if (enchantment != null) meta.addEnchant(enchantment, Integer.parseInt(parts[1]), true);
			}
			item.setItemMeta(meta);
		}
		return item;
	}

	private @Nullable OldPlayerData getOldPlayerDataFromMap(Player player) {
		return oldPlayerDataMap.get(player.getUniqueId());
	}

	private void clearArmorFromInventory(Player p) {
		Inventory inv = p.getInventory();
		List<Integer> slotsToRemove = new ArrayList<>();

		// Collect slots of items to remove
		for (int i = 0; i < inv.getSize(); i++) {
			ItemStack item = inv.getItem(i);
			if (item != null && item.hasItemMeta()) {
				ItemMeta meta = item.getItemMeta();
				if (meta.hasCustomModelData() && getArmorCustomModelData().contains(meta.getCustomModelData())) {
					slotsToRemove.add(i);
				}
			}
		}

		// Remove items from collected slots
		for (Integer slot : slotsToRemove) {
			inv.clear(slot);
		}
	}


	public Map<UUID, Ability> getSelectedAbilities() {
		return selectedAbilities;
	}

	public void setSelectedAbilities(UUID uniqueId, Ability ability) {
		selectedAbilities.put(uniqueId, ability);
	}
}
