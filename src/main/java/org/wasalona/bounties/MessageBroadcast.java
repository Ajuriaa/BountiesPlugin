package org.wasalona.bounties;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.Location;

public class MessageBroadcast {

    public void printPlayerLocation(Player player) {
        // Format the message
        String locationMsg = getLocationString(player);

        // Broadcast the message to all players
        Bukkit.broadcastMessage(locationMsg);
    }

    public void messagePlayerLocation(Player target, Player player) {
        // Format the message
        String locationMsg = getLocationString(target);

        // Broadcast the message to all players
        player.sendMessage(locationMsg);
    }

    private String getLocationString(Player player) {
        Location location = player.getLocation();

        // Extract coordinates
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        // Format the message
        return "[name: " + player.getName() + ", x:" + x + ", y:" + y + ", z:" + z + "]";
    }
}
