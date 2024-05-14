package me.quared.hubpvp.util.adapters;

import com.google.gson.*;
import org.bukkit.Color;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.potion.PotionData;

import java.lang.reflect.Type;
import java.util.Map;

public class ItemMetaAdapter implements JsonSerializer<ItemMeta>, JsonDeserializer<ItemMeta> {


    @Override
    public JsonElement serialize(ItemMeta src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonMeta = new JsonObject();
        YamlConfiguration config = new YamlConfiguration();
        config.set("meta", src);
        String yamlString = config.saveToString();
        jsonMeta.addProperty("metaYml", yamlString.replace("=", ":"));

        if (src instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta) src;
            PotionData potionData = potionMeta.getBasePotionData();
            jsonMeta.addProperty("potionType", potionData.getType().name());
            jsonMeta.addProperty("extended", potionData.isExtended());
            jsonMeta.addProperty("upgraded", potionData.isUpgraded());
        }

        if (src instanceof LeatherArmorMeta) {
            LeatherArmorMeta leatherMeta = (LeatherArmorMeta) src;
            Color color = leatherMeta.getColor();
            jsonMeta.addProperty("color", color.asRGB());
        }

        if (src instanceof Repairable) {
            Repairable repairable = (Repairable) src;
            if (repairable.hasRepairCost()) {
                jsonMeta.addProperty("repairCost", repairable.getRepairCost());
            }
        }

        Map<Enchantment, Integer> enchants = src.getEnchants();
        if (!enchants.isEmpty()) {
            JsonObject jsonEnchants = new JsonObject();
            for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                jsonEnchants.addProperty(entry.getKey().getKey().getKey(), entry.getValue());
            }
            jsonMeta.add("enchants", jsonEnchants);

        }

        return jsonMeta;
    }

    @Override
    public ItemMeta deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String yamlString = jsonObject.get("metaYml").getAsString().replace(":", "=");
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(yamlString);
            return (ItemMeta) config.get("meta");
        } catch (Exception e) {
            throw new JsonParseException("Failed to deserialize ItemMeta", e);
        }
    }
}
