package me.quared.hubpvp.util.adapters;
import com.google.gson.Gson;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ItemMetaAdapter {

    public static String serialize(ItemStack item) {
        if (item == null) return null;

        Map<String, Object> serializedItem = new HashMap<>();
        serializedItem.put("type", item.getType().name());
        serializedItem.put("amount", item.getAmount());

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            serializedItem.put("meta", serializeMeta(meta));
        }

        return new Gson().toJson(serializedItem);
    }

    public static ItemStack deserialize(String json) {
        if (json == null || json.isEmpty()) return null;

        Map<String, Object> serializedItem = new Gson().fromJson(json, HashMap.class);
        Material type = Material.valueOf((String) serializedItem.get("type"));
        int amount = ((Number) serializedItem.get("amount")).intValue();

        ItemStack item = new ItemStack(type, amount);

        if (serializedItem.containsKey("meta")) {
            ItemMeta meta = deserializeMeta((Map<String, Object>) serializedItem.get("meta"), type);
            item.setItemMeta(meta);
        }

        return item;
    }

    private static Map<String, Object> serializeMeta(ItemMeta meta) {
        Map<String, Object> serializedMeta = new HashMap<>();

        if (meta.hasDisplayName()) {
            serializedMeta.put("displayName", meta.getDisplayName());
        }

        if (meta.hasLore()) {
            serializedMeta.put("lore", meta.getLore());
        }

        if (meta.hasEnchants()) {
            Map<String, Integer> enchants = new HashMap<>();
            for (Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                enchants.put(entry.getKey().getKey().getKey(), entry.getValue());
            }
            serializedMeta.put("enchants", enchants);
        }

        if (meta.hasAttributeModifiers()) {
            Map<String, Map<String, Object>> modifiers = new HashMap<>();
            for (Entry<Attribute, AttributeModifier> entry : meta.getAttributeModifiers().entries()) {
                modifiers.put(entry.getKey().name(), entry.getValue().serialize());
            }
            serializedMeta.put("modifiers", modifiers);
        }

        if (meta instanceof LeatherArmorMeta) {
            serializedMeta.put("color", ((LeatherArmorMeta) meta).getColor().asRGB());
        }

        if (meta instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta) meta;
            serializedMeta.put("potionData", potionMeta.getBasePotionData().toString());
            if (potionMeta.hasCustomEffects()) {
                List<Map<String, Object>> effects = new ArrayList<>();
                for (PotionEffect effect : potionMeta.getCustomEffects()) {
                    effects.add(effect.serialize());
                }
                serializedMeta.put("effects", effects);
            }
        }

        return serializedMeta;
    }

    private static ItemMeta deserializeMeta(Map<String, Object> serializedMeta, Material type) {
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(type);

        if (serializedMeta.containsKey("displayName")) {
            meta.setDisplayName((String) serializedMeta.get("displayName"));
        }

        if (serializedMeta.containsKey("lore")) {
            meta.setLore((List<String>) serializedMeta.get("lore"));
        }

        if (serializedMeta.containsKey("enchants")) {
            Map<String, Double> enchants = (Map<String, Double>) serializedMeta.get("enchants");
            for (Entry<String, Double> entry : enchants.entrySet()) {
                meta.addEnchant(Enchantment.getByKey(NamespacedKey.minecraft(entry.getKey())), entry.getValue().intValue(), true);
            }
        }

        if (serializedMeta.containsKey("modifiers")) {
            Map<String, Map<String, Object>> modifiers = (Map<String, Map<String, Object>>) serializedMeta.get("modifiers");
            for (Entry<String, Map<String, Object>> entry : modifiers.entrySet()) {
                meta.addAttributeModifier(Attribute.valueOf(entry.getKey()), AttributeModifier.deserialize(entry.getValue()));
            }
        }

        if (meta instanceof LeatherArmorMeta && serializedMeta.containsKey("color")) {
            ((LeatherArmorMeta) meta).setColor(Color.fromRGB((int) (double) serializedMeta.get("color")));
        }

        if (meta instanceof PotionMeta && serializedMeta.containsKey("potionData")) {
        }

        return meta;
    }
}