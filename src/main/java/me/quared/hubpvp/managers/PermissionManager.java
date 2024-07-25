package me.quared.hubpvp.managers;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.entity.Player;

public class PermissionManager {

    private static final LuckPerms luckPerms = LuckPermsProvider.get();

    public static void assignPermission(Player player, String permission, boolean value) {
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user != null) {
            Node node = Node.builder(permission).value(value).build();
            user.data().add(node);
            luckPerms.getUserManager().saveUser(user);
        }
    }
}
