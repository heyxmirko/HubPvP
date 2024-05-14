package me.quared.hubpvp.util.adapters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemStackAdapter {

    public Material type = Material.AIR;
    public int amount = 0;
    public ItemMeta meta = null;


    public static ItemStackAdapter fromItemStack(ItemStack itemStack) {
        ItemStackAdapter itemStackAdapter = new ItemStackAdapter();
        itemStackAdapter.type = itemStack.getType();
        itemStackAdapter.amount = itemStack.getAmount();
        itemStackAdapter.meta = itemStack.getItemMeta();
        return itemStackAdapter;
    }

    public ItemStack toItemStack() {
        ItemStack itemStack = new ItemStack(type, amount);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public String serialize() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ItemMeta.class, new ItemMetaAdapter())
                .setPrettyPrinting()
                .create();
        return gson.toJson(this);
    }

    public static ItemStackAdapter deserialize(String json) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ItemMeta.class, new ItemMetaAdapter())
                .create();
        return gson.fromJson(json, ItemStackAdapter.class);
    }
}
