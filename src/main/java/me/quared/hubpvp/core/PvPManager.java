package me.quared.hubpvp.core;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.util.StringUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PvPManager {

	private final Map<Player, PvPState> playerPvpStates;
	private final Map<Player, BukkitRunnable> currentTimers;
	private final List<OldPlayerData> oldPlayerDataList;

	public List<OldPlayerData> getOldPlayerDataList() {
		return this.oldPlayerDataList;
	}

	private ItemStack weapon, helmet, chestplate, leggings, boots;

	public PvPManager() {
		playerPvpStates = new HashMap<>();
		currentTimers = new HashMap<>();
		oldPlayerDataList = new ArrayList<>();

		loadItems();
	}

	public void loadItems() {
		// Weapon
		this.weapon = getItemFromConfig("weapon");
		// Armor
		this.helmet = getItemFromConfig("helmet");
		this.chestplate = getItemFromConfig("chestplate");
		this.leggings = getItemFromConfig("leggings");
		this.boots = getItemFromConfig("boots");
	}

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

	public ItemStack getItemFromConfig(String name) {
		HubPvP instance = HubPvP.instance();
		String materialName = instance.getConfig().getString("items." + name + ".material");
		Material material = Material.getMaterial(materialName.toUpperCase());
		ItemStack item = new ItemStack(material, 1);
		ItemMeta meta = item.getItemMeta();

		if (meta != null) {
			String itemName = instance.getConfig().getString("items." + name + ".name");
			if (itemName != null && !itemName.isEmpty()) {
				meta.setDisplayName(StringUtil.colorize(itemName));
			}

			List<String> lore = instance.getConfig().getStringList("items." + name + ".lore");
			if (!lore.isEmpty()) {
				meta.setLore(StringUtil.colorize(lore));
			}

			int customModelData = instance.getConfig().getInt("items." + name + ".custom_model_data", 0);
			if (customModelData != 0) {
				meta.setCustomModelData(customModelData);
			}

			List<String> enchantments = instance.getConfig().getStringList("items." + name + ".enchantments");
			for (String enchString : enchantments) {
				String[] parts = enchString.split(":");
				Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(parts[0].toLowerCase()));
				if (enchantment != null) {
					meta.addEnchant(enchantment, Integer.parseInt(parts[1]), true);
				}
			}

			item.setItemMeta(meta);
		}
		return item;
	}

	public void enablePvP(Player player) {
		setPlayerState(player, PvPState.ON);

		if (getOldData(player) != null) getOldPlayerDataList().remove(getOldData(player));
		getOldPlayerDataList().add(new OldPlayerData(player, player.getInventory().getArmorContents(), player.getAllowFlight()));

		player.setAllowFlight(false);
		player.getInventory().setHelmet(getHelmet());
		player.getInventory().setChestplate(getChestplate());
		player.getInventory().setLeggings(getLeggings());
		player.getInventory().setBoots(getBoots());

		player.sendMessage(StringUtil.colorize(HubPvP.instance().getConfig().getString("lang.pvp-enabled")));
		player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5F, 2.0F);
	}

	public void setPlayerState(Player p, PvPState state) {
		playerPvpStates.put(p, state);
	}

	public @Nullable OldPlayerData getOldData(Player p) {
		return oldPlayerDataList.stream().filter(data -> data.player().equals(p)).findFirst().orElse(null);
	}

	public void removePlayer(Player p) {
		disablePvP(p);
		playerPvpStates.remove(p);
	}

	public void disablePvP(Player player) {
		setPlayerState(player, PvPState.OFF);

		OldPlayerData oldPlayerData = getOldData(player);
		if (oldPlayerData != null) {
			player.getInventory().setHelmet(oldPlayerData.armor()[3] == null ? new ItemStack(Material.AIR) : oldPlayerData.armor()[3]);
			player.getInventory().setChestplate(oldPlayerData.armor()[2] == null ? new ItemStack(Material.AIR) : oldPlayerData.armor()[2]);
			player.getInventory().setLeggings(oldPlayerData.armor()[1] == null ? new ItemStack(Material.AIR) : oldPlayerData.armor()[1]);
			player.getInventory().setBoots(oldPlayerData.armor()[0] == null ? new ItemStack(Material.AIR) : oldPlayerData.armor()[0]);
			player.setAllowFlight(oldPlayerData.canFly());
		}

		player.sendMessage(StringUtil.colorize(HubPvP.instance().getConfig().getString("lang.pvp-disabled")));
		player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 0.5F, 1.0F);
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

	public PvPState getPlayerState(Player p) {
		return playerPvpStates.get(p);
	}

	public void giveWeapon(Player p) {
		p.getInventory().setItem(HubPvP.instance().getConfig().getInt("items.weapon.slot") - 1, getWeapon());
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

}
