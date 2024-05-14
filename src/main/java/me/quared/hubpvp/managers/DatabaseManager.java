package me.quared.hubpvp.managers;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.core.OldPlayerData;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseManager {


    public static void saveOldPlayerData(OldPlayerData oldPlayerData) {
        Connection connection = createConnection();
        if (connection == null) return;
        try {
            connection.createStatement().executeUpdate("INSERT INTO oldPlayerData (uuid, armor, canFly) VALUES ('" + oldPlayerData.playerUUID() + "', '" + oldPlayerData.armor() + "', " + oldPlayerData.canFly() + ")");
        } catch (Exception e) {
            HubPvP.instance().getLogger().severe("Failed to save old player data: " + e.getMessage());
        }
    }

    public static OldPlayerData getOldPlayerData(String playerUUID) {
        Connection connection = createConnection();
        if (connection == null) return null;
        try {
            return connection.createStatement().executeQuery("SELECT * FROM oldPlayerData WHERE uuid = '" + playerUUID + "'").next() ? new OldPlayerData(null, null, false) : null;
        } catch (Exception e) {
            HubPvP.instance().getLogger().severe("Failed to get old player data: " + e.getMessage());
            return null;
        }
    }

    public static void initializeDatabase() {
        Connection connection = createConnection();
        if (connection == null) return;

        try {
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS oldPlayerData (uuid TEXT PRIMARY KEY, armor TEXT, canFly BOOLEAN)");
        } catch (Exception e) {
            HubPvP.instance().getLogger().severe("Failed to create database tables: " + e.getMessage());
        }
    }

    private static Connection createConnection() {
        try {
            String pluginFolderPath = HubPvP.instance().getDataFolder().getPath();
            return DriverManager.getConnection("jdbc:sqlite:" + pluginFolderPath + "/data.db");
        } catch (Exception e) {
            HubPvP.instance().getLogger().severe("Failed to create database connection: " + e.getMessage());
            return null;
        }
    }
}
