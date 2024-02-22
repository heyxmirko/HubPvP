package me.quared.hubpvp.util;

import me.quared.hubpvp.HubPvP;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class DebugLogger {

    JavaPlugin plugin = JavaPlugin.getPlugin(HubPvP.class);

    public void log(String message) {
        String formattedMessage = "[HubPvP - DebugLogger] " + message;

        Player player1 = Bukkit.getPlayer("lukyn76");
        Player player2 = Bukkit.getPlayer("Renzotom");

        if (player1 != null) player1.sendMessage(formattedMessage);
        if (player2 != null) player2.sendMessage(formattedMessage);

        plugin.getLogger().info(formattedMessage);
    }
}
