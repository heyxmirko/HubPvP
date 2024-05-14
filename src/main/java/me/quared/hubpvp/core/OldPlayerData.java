package me.quared.hubpvp.core;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public record OldPlayerData(UUID playerUUID, ItemStack[] armor, boolean canFly) {

}
