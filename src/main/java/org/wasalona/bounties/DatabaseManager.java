package org.wasalona.bounties;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.security.SecureRandom;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {

    public Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://db-mfl-01.sparkedhost.us:3306/s129819_Bounties";
        String username = "u129819_SpXHYSxT48";
        String password = "VJKW.5Da1Lp!ZfTYisY=+hfU";
        return DriverManager.getConnection(url, username, password);
    }

    public boolean createBounty(Player player, Player target, ItemStack[] rewardItems) {
        // Get the UUIDs of the sender and target players
        String senderUUID = player.getUniqueId().toString();
        String targetUUID = target.getUniqueId().toString();

        int senderId = getPlayerId(senderUUID);
        int targetId = getPlayerId(targetUUID);

        String coordinates = getCoordinates(target);

        String formattedItems = getFormattedItems(rewardItems);

        // Generate a random code (replace with your code generation logic)
        String generatedCode = generateCode();

        // Construct the SQL INSERT query
        String query = "INSERT INTO bounties (target_player_id, creator_player_id, target_coordinates, reward_items, claim_code) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            // Set values for parameters in the prepared statement
            statement.setInt(1, senderId);
            statement.setInt(2, targetId);
            statement.setString(3, coordinates);
            statement.setString(4, formattedItems);
            statement.setString(5, generatedCode);

            // Execute the query
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getFormattedItems(ItemStack[] items) {
        Map<String, Integer> itemCounts = new HashMap<>();

        // Count the quantities of each item type
        for (ItemStack item : items) {
            String itemName = item.getType().toString();
            itemCounts.put(itemName, itemCounts.getOrDefault(itemName, 0) + item.getAmount());
        }

        // Construct the string
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(entry.getKey()).append(": ").append(entry.getValue());
        }

        return result.toString();
    }

    public static String generateCode() {
        SecureRandom random = new SecureRandom();
        String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            sb.append(randomChar);
        }
        return sb.toString();
    }

    public String getCoordinates(Player target) {
        Location loc = target.getLocation();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        return  "x:" + x + ", y:" + y + ", z:" + z;
    }

    public int getPlayerId(String playerUUID) {
        // Initialize player ID to -1 (indicating not found)
        int playerId = -1;

        // Construct the SQL SELECT query
        String query = "SELECT id FROM players WHERE UUID = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            // Set the player UUID as a parameter in the prepared statement
            statement.setString(1, playerUUID);

            // Execute the query
            try (ResultSet resultSet = statement.executeQuery()) {
                // Check if the result set has any rows
                if (resultSet.next()) {
                    // Get the player ID from the result set
                    playerId = resultSet.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return playerId;
    }

}
