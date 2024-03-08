package me.quared.hubpvp.core;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
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


    public boolean isPlayerInAnyRegion(Player player) {
        WorldGuard worldGuard = WorldGuard.getInstance();
        RegionContainer container = worldGuard.getPlatform().getRegionContainer();
        World world = BukkitAdapter.adapt(Objects.requireNonNull(player.getWorld())); // Convert Bukkit world to WorldEdit world
        com.sk89q.worldguard.protection.managers.RegionManager regions = container.get(world);
        if (regions == null) return false; // Always check if regions is null
        return regions.getApplicableRegions(BukkitAdapter.asBlockVector(player.getLocation())).size() > 0;
    }


    public boolean isPlayerInKeepInventoryRegion(Player player) {
        WorldGuard wg = WorldGuard.getInstance();
        RegionQuery query = wg.getPlatform().getRegionContainer().createQuery();
        Location loc = BukkitAdapter.adapt(player.getLocation());

        Flag<?> keepInvFlag = wg.getFlagRegistry().get("keep-inventory");
        if (keepInvFlag == null) return false;
        Object flagValue = query.queryValue(loc, null, keepInvFlag);
        if (flagValue == null) return false;
        return (Boolean) flagValue;
    }


}
