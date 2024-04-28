package org.wasalona.bounties;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.Location;

public class MessageBroadcast {

    public void printPlayerLocation(Player player) {
        Location location = player.getLocation();

        // Extract coordinates
        double x = location.getBlockX();
        double y = location.getBlockY();
        double z = location.getBlockZ();

        // Format the message
        String locationMsg = "[name: " + player.getName() + ", x:" + x + ", y:" + y + ", z:" + z + "]";

        // Broadcast the message to all players
        Bukkit.broadcastMessage(locationMsg);
    }


}
