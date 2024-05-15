package me.quared.hubpvp.managers;

import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.core.OldPlayerData;
import me.quared.hubpvp.util.ArmorSerializationUtil;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

public class DatabaseManager {


    public static void saveOldPlayerData(OldPlayerData oldPlayerData) throws SQLException {
        Connection connection = createConnection();

        ItemStack[] armorArray = oldPlayerData.armor();
        String allArmorSerialized = ArmorSerializationUtil.serializeArmorArray(armorArray);

        if (connection == null) return;
        try {
            connection.createStatement().executeUpdate("INSERT INTO oldPlayerData (uuid, armor, canFly) VALUES ('" + oldPlayerData.playerUUID() + "', '" + allArmorSerialized + "', " + oldPlayerData.canFly() + ")");
        } catch (Exception e) {
            HubPvP.instance().getLogger().severe("Failed to save old player data: " + e.getMessage());
        }
        finally {
            try {
                connection.close();
            } catch (SQLException e) {
                HubPvP.instance().getLogger().severe("Failed to close database connection: " + e.getMessage());
            }
        }
    }

    public static OldPlayerData getOldPlayerData(UUID playerUUID) {
        Connection connection = createConnection();
        if (connection == null) return null;
        try {
            var resultSet = connection.createStatement().executeQuery("SELECT * FROM oldPlayerData WHERE uuid = '" + playerUUID + "'");
            if (!resultSet.next()) return null;
            String armorSerialized = resultSet.getString("armor");
            ItemStack[] armorArray = ArmorSerializationUtil.deserializeArmorArray(armorSerialized);
            boolean canFly = resultSet.getBoolean("canFly");
            return new OldPlayerData(playerUUID, armorArray, canFly);

        } catch (Exception e) {
            HubPvP.instance().getLogger().severe("Failed to get old player data: " + e.getMessage());
            return null;
        }
        finally {
            try {
                connection.close();
            } catch (SQLException e) {
                HubPvP.instance().getLogger().severe("Failed to close database connection: " + e.getMessage());
            }
        }
    }

    public static void deleteOldPlayerData(UUID playerUUID) {
        Connection connection = createConnection();
        if (connection == null) return;
        try {
            connection.createStatement().executeUpdate("DELETE FROM oldPlayerData WHERE uuid = '" + playerUUID + "'");
            HubPvP.instance().getLogger().info("--------------- DELETEING OLD PLAYER DATA: " + playerUUID + " ---------------");
        } catch (Exception e) {
            HubPvP.instance().getLogger().severe("Failed to delete old player data: " + e.getMessage());
        }
        finally {
            try {
                connection.close();
            } catch (SQLException e) {
                HubPvP.instance().getLogger().severe("Failed to close database connection: " + e.getMessage());
            }
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
        finally {
            try {
                connection.close();
            } catch (SQLException e) {
                HubPvP.instance().getLogger().severe("Failed to close database connection: " + e.getMessage());
            }
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
