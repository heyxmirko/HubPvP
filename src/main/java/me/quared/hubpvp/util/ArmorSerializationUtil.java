package me.quared.hubpvp.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import me.quared.hubpvp.util.adapters.ItemMetaAdapter;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ArmorSerializationUtil {

    public static String serializeArmorArray(ItemStack[] armorArray) {
        List<String> serializedItems = new ArrayList<>();
        for (ItemStack armor : armorArray) {
            if (armor == null) {
                serializedItems.add(null);
            } else {
                String armorSerialized = ItemMetaAdapter.serialize(armor);
                if (armorSerialized != null) {
                    armorSerialized = armorSerialized.replace("\\", "");
                    serializedItems.add(armorSerialized);
                } else {
                    serializedItems.add(null);
                }
            }
        }
        Gson gson = new Gson();
        return gson.toJson(serializedItems);
    }

    public static ItemStack[] deserializeArmorArray(String json) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<String>>() {}.getType();
        List<String> serializedItems = gson.fromJson(json, listType);
        ItemStack[] armorArray = new ItemStack[serializedItems.size()];

        for (int i = 0; i < serializedItems.size(); i++) {
            String serializedItem = serializedItems.get(i);
            if (serializedItem != null) {
                armorArray[i] = ItemMetaAdapter.deserialize(serializedItem);
            } else {
                armorArray[i] = null;
            }
        }
        return armorArray;
    }
}
