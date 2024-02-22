package me.quared.hubpvp.core;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public record OldPlayerData(Player player, ItemStack[] armor, boolean canFly) {


    public String getArmorString() {
        if (armor == null || armor.length ==0) {
            return "Nor armor";
        }

        StringBuilder armorTypes = new StringBuilder();
        for (ItemStack armorPiece : armor) {
            if (armorPiece != null) {
                String type = armorPiece.getType().name();
                if (!armorTypes.isEmpty()) {
                    armorTypes.append(", ");
                }
                armorTypes.append(type);
            }
        }
        return armorTypes.toString();
    }

}
