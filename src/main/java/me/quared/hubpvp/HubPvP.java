package me.quared.hubpvp;

import me.quared.hubpvp.commands.HubPvPCommand;
import me.quared.hubpvp.core.PvPManager;
import me.quared.hubpvp.listeners.*;
import me.quared.hubpvp.managers.DatabaseManager;
import me.quared.hubpvp.util.StringUtil;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class HubPvP extends JavaPlugin {

	private static HubPvP instance;

	private PvPManager pvpManager;

	public static HubPvP instance() {
		return instance;
	}

	@Override
	public void onDisable() {
		pvpManager.disable();
		getServer().getConsoleSender().sendMessage(StringUtil.colorize("&c" + getDescription().getName() + " v" + getDescription().getVersion() + " disabled."));
	}

	@Override
	public void onEnable() {
		instance = this;
		pvpManager = new PvPManager();

		registerListeners();
		registerCommands();
		DatabaseManager.initializeDatabase();

		getConfig().options().copyDefaults(true);
		saveDefaultConfig();

		getLogger().info(StringUtil.colorize("&a" + getDescription().getName() + " v" + getDescription().getVersion() + " enabled."));
	}

	private void registerListeners() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new DeathListener(), this);
		pm.registerEvents(new ItemSlotChangeListener(), this);
		pm.registerEvents(new PlayerJoinListener(), this);
		pm.registerEvents(new ProtectionListeners(), this);
		pm.registerEvents(new SpellCastListener(), this);
	}

	private void registerCommands() {
		Objects.requireNonNull(getCommand("hubpvp")).setExecutor(new HubPvPCommand());
		Objects.requireNonNull(getCommand("hubpvp")).setTabCompleter(new HubPvPCommand());
	}

	public PvPManager pvpManager() {
		return pvpManager;
	}

}
