package me.quared.hubpvp.listeners.abilities;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public interface Ability {
    static Ability getDefaultAbility() {
        return new PurpleStrikeAbility();
    }

    void cast(PlayerInteractEvent event);
    int getCooldown();
    ItemStack getIcon();
}
