package me.quared.hubpvp.core;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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

    public boolean checkIfPlayerIsInRegion(Player player, String regionName) {
        WorldGuard worldGuard = WorldGuard.getInstance();
        RegionContainer container = worldGuard.getPlatform().getRegionContainer();
        World world = BukkitAdapter.adapt(Objects.requireNonNull(player.getWorld())); // Convert Bukkit world to WorldEdit world
        com.sk89q.worldguard.protection.managers.RegionManager regions = container.get(world);
        if (regions == null) return false; // Always check if regions is null
        ProtectedRegion region = regions.getRegion(regionName);
        if (region == null) return false; // Check if the region exists
        return region.contains(BukkitAdapter.asBlockVector(player.getLocation()));
    }

    public boolean checkFlagInRegion(String regionName, String flag) {
        WorldGuard worldGuard = WorldGuard.getInstance();
        RegionContainer container = worldGuard.getPlatform().getRegionContainer();
        World world = BukkitAdapter.adapt(Objects.requireNonNull(Bukkit.getWorld("world"))); // Convert Bukkit world to WorldEdit world
        com.sk89q.worldguard.protection.managers.RegionManager regions = container.get(world);
        if (regions == null) return false; // Always check if regions is null
        ProtectedRegion region = regions.getRegion(regionName);
        if (region == null) return false; // Check if the region exists
        // Check if the flag exists, if it does and is set to yes, return true
        // otherwise return false
        return region.getFlag(WorldGuard.getInstance().getFlagRegistry().get(flag)) == StateFlag.State.ALLOW;
    }
}
