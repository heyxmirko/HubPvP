package me.quared.hubpvp.listeners.abilities;

import org.bukkit.event.player.PlayerInteractEvent;

public interface Ability {
    void cast(PlayerInteractEvent event);
    int getCooldown();
}
