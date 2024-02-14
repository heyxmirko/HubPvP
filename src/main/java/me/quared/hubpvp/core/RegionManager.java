package me.quared.hubpvp.core;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;

import java.util.Objects;
import java.util.UUID;

public class RegionManager {

    public void addPlayerToRegion(UUID playerUUID) {
        WorldGuard worldGuard = WorldGuard.getInstance();
        RegionContainer container = worldGuard.getPlatform().getRegionContainer();
        World world = BukkitAdapter.adapt(Objects.requireNonNull(Bukkit.getWorld("world"))); // Convert Bukkit world to WorldEdit world
        com.sk89q.worldguard.protection.managers.RegionManager regions = container.get(world);
        if (regions == null) return; // Always check if regions is null
        ProtectedRegion spawnRegion = regions.getRegion("spawn");
        if (spawnRegion == null) return; // Check if the spawnRegion exists

        spawnRegion.getMembers().addPlayer(playerUUID);

        try {
            regions.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removePlayerFromRegion(UUID playerUUID) {
        WorldGuard worldGuard = WorldGuard.getInstance();
        RegionContainer container = worldGuard.getPlatform().getRegionContainer();
        World world = BukkitAdapter.adapt(Objects.requireNonNull(Bukkit.getWorld("world"))); // Convert Bukkit world to WorldEdit world
        com.sk89q.worldguard.protection.managers.RegionManager regions = container.get(world);
        if (regions == null) return; // Always check if regions is null
        ProtectedRegion spawnRegion = regions.getRegion("spawn");
        if (spawnRegion == null) return; // Check if the spawnRegion exists

        spawnRegion.getMembers().removePlayer(playerUUID);

        try {
            regions.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
