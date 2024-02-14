package me.quared.hubpvp.commands;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.core.PvPManager;
import me.quared.hubpvp.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HubPvPCommand implements CommandExecutor, TabCompleter {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		if (args.length != 0) {
			if (args[0].equalsIgnoreCase("reload")) {
				HubPvP plugin = HubPvP.instance();
				plugin.reloadConfig();
				plugin.pvpManager().loadItems();
				sender.sendMessage(StringUtil.colorize(plugin.getConfig().getString("lang.reloaded")));
			}
			else if (args[0].equalsIgnoreCase("giveWeapon")) {

				if(args.length < 2) {
					sender.sendMessage(ChatColor.RED + "Invalid usage. Use /" + label + "giveSword <playerName>");
					return true;
				}

				Player player = Bukkit.getPlayer(args[1]);
				if (player == null) {
					sender.sendMessage(ChatColor.RED + "Player is not online or invalid");
					return true;
				}

				HubPvP plugin = HubPvP.instance();
				PvPManager pvpManager = plugin.pvpManager();
				ItemStack weapon = pvpManager.getWeapon();
				player.getInventory().addItem(weapon);

				sender.sendMessage(ChatColor.GREEN + "A weapon was given to player " + player.getName());
			}
			else {
				sender.sendMessage(ChatColor.RED + "Invalid arguments. Use: /" + label + " <reload>");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Invalid usage. Use: /" + label + " <args>");
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if (args.length == 1) {
			// List of the first argument commands
			List<String> commands = Arrays.asList("reload", "giveWeapon");
			// Filter based on what the user has already typed
			return commands.stream().filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
		} else if (args.length == 2 && "giveWeapon".equalsIgnoreCase(args[0])) {
			// If the first argument is "reset", suggest online player names for the second argument
			return Bukkit.getOnlinePlayers().stream()
					.map(Player::getName)
					.filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
					.collect(Collectors.toList());
		}
		// Return an empty list for other cases
		return Collections.emptyList();
	}

}
