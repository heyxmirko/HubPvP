package me.quared.hubpvp.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import me.quared.hubpvp.HubPvP;
import me.quared.hubpvp.core.PvPManager;
import me.quared.hubpvp.listeners.abilities.*;
import me.quared.hubpvp.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class AbilitySelectorListener implements Listener {

    private final String ABILITY_SELECTOR_GUI_TITLE = "%tab_replace_luckperms_inherits_permission_resourcepack.pvpsword_abilities%";
    private List<Ability> abilities;

    public AbilitySelectorListener() {
        this.abilities = List.of(new Ability[]{
                new PurpleStrikeAbility(),
                new HealAbility(),
                //new FlamethrowerAbility(),
                new IceShardAbility(),
                //new GeyserAbility(),
                //new MeteorShowerAbility()
                // Add other abilities here
        });
    }


    @EventHandler
    public void swapHandItems(PlayerSwapHandItemsEvent event) {
        ItemStack mainHandItem = event.getOffHandItem();
        if (mainHandItem != null && mainHandItem.getType() == Material.DIAMOND_SWORD) {
            if (isValidWeapon(mainHandItem)) {
                event.setCancelled(true);
                openAbilitySelectorGUI(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onMiddleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Inventory inventory = event.getInventory();

        if (event.getClick() == ClickType.SWAP_OFFHAND) {
            ItemStack item = event.getCurrentItem();
            if (item != null && item.getType() == Material.DIAMOND_SWORD) {
                if (isValidWeapon(item)) {
                    event.setCancelled(true);
                    openAbilitySelectorGUI(player);
                }
            }
        }
    }



    private boolean isValidWeapon(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        if (!item.getItemMeta().hasCustomModelData()) return false;

        PvPManager pvPManager = HubPvP.instance().pvpManager();
        int customModelData = pvPManager.getWeapon().getItemMeta().getCustomModelData();
        if (item.getItemMeta().getCustomModelData() == customModelData) return true;
        else return false;
    }

    private void openAbilitySelectorGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 36, PlaceholderAPI.setPlaceholders(player, ABILITY_SELECTOR_GUI_TITLE));

        // Add ability icons to the GUI
        int slot = 10;
        for (Ability ability : abilities) {
            gui.setItem(slot++, ability.getIcon());
            if (slot >= gui.getSize()) break;  // Prevents adding more items than the inventory size
        }

//        // Create black glass pane item
//        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
//        ItemMeta paneMeta = blackPane.getItemMeta();
//        if (paneMeta != null) {
//            paneMeta.setDisplayName(" ");  // Set an empty display name
//            blackPane.setItemMeta(paneMeta);
//        }
//
//        // Fill remaining slots with black glass panes
//        for (int i = 0; i < gui.getSize(); i++) {
//            if (gui.getItem(i) == null || gui.getItem(i).getType() == Material.AIR) {
//                gui.setItem(i, blackPane);
//            }
//        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (e.getView().getTitle().equals(PlaceholderAPI.setPlaceholders(player, ABILITY_SELECTOR_GUI_TITLE))) {
            e.setCancelled(true);  // Prevent the player from taking items

            ItemStack clickedItem = e.getCurrentItem();

            if (clickedItem != null && clickedItem.hasItemMeta()) {
                for (Ability ability : abilities) {
                    if (clickedItem.isSimilar(ability.getIcon())) {
                        HubPvP.instance().pvpManager().setSelectedAbilities(player.getUniqueId(), ability);
                        player.sendMessage(StringUtil.colorize("&7You have selected the " + ability.getIcon().getItemMeta().getDisplayName() + " &7ability!"));
                        player.closeInventory();
                        break;
                    }
                }
            }
        }
    }
}
