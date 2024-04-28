package org.wasalona.bounties;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.security.SecureRandom;

import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DatabaseManager {
    private final Material[] defaultItems = { Material.AIR, Material.RED_WOOL, Material.GREEN_WOOL };

    public Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://db-mfl-01.sparkedhost.us:3306/s129819_Bounties";
        String username = "u129819_SpXHYSxT48";
        String password = "VJKW.5Da1Lp!ZfTYisY=+hfU";
        return DriverManager.getConnection(url, username, password);
    }

    public boolean hasActiveBounty(String playerUUID) {
        // Initialize the result to false
        boolean hasActiveBounty = false;
        int playerId = getPlayerId(playerUUID);

        // Construct the SQL SELECT query to check for active bounties
        String query = "SELECT COUNT(*) > 0 AS has_active_bounty " +
                "FROM bounties " +
                "WHERE target_player_id = ? AND active = 1";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, playerId);

            // Execute the query
            try (ResultSet resultSet = statement.executeQuery()) {
                // Check if the result set has any rows
                if (resultSet.next()) {
                    // Retrieve the boolean result indicating if the player has an active bounty
                    hasActiveBounty = resultSet.getBoolean("has_active_bounty");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return hasActiveBounty;
    }

    public String getActiveBountyClaimCode(UUID playerUUID) {
        String code = null;

        // Construct the SQL SELECT query to retrieve the claim code of the active bounty
        String selectQuery = "SELECT claim_code FROM bounties " +
                "WHERE target_player_id = ? AND active = 1";

        // Get the player ID
        int playerId = getPlayerId(playerUUID.toString());

        try (Connection connection = getConnection();
             PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {

            // Set the player ID as a parameter in the SELECT query
            selectStatement.setInt(1, playerId);

            // Execute the SELECT query to retrieve the claim code of the active bounty
            try (ResultSet resultSet = selectStatement.executeQuery()) {
                // Check if the result set has any rows
                if (resultSet.next()) {
                    // Retrieve the claim code for the active bounty
                    code = resultSet.getString("claim_code");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return code;
    }

    public boolean updateBountyToInactive(String playerUUID) {
        // Construct the SQL UPDATE query to set the bounty as inactive
        String query = "UPDATE bounties " +
                "SET active = 0 " +
                "WHERE target_player_id = ? AND active = 1";

        int playerId = getPlayerId(playerUUID);

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            // Set the player UUID as a parameter in the prepared statement
            statement.setInt(1, playerId);

            // Execute the update query
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createBounty(Player player, Player target, ItemStack[] rewardItems) {
        // Get the UUIDs of the sender and target players
        String senderUUID = player.getUniqueId().toString();
        String targetUUID = target.getUniqueId().toString();

        int senderId = getPlayerId(senderUUID);
        int targetId = getPlayerId(targetUUID);

        String coordinates = getCoordinates(target);

        String formattedItems = getFormattedItems(rewardItems, player);

        // Generate a random code (replace with your code generation logic)
        String generatedCode = generateCode();

        // Construct the SQL INSERT query
        String query = "INSERT INTO bounties (target_player_id, creator_player_id, target_coordinates, reward_items, claim_code) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            // Set values for parameters in the prepared statement
            statement.setInt(1, targetId);
            statement.setInt(2, senderId);
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

    public String getFormattedItems(ItemStack[] items, Player player) {
        Map<String, Integer> itemCounts = new HashMap<>();


        // Count the quantities of each item type
        for (ItemStack item : items) {
            if (item == null || Arrays.asList(defaultItems).contains(item.getType())) {
                continue;
            }

            if(CoinList.contains(item)){
                String itemName = item.getType().toString();
                itemCounts.put(itemName, itemCounts.getOrDefault(itemName, 0) + item.getAmount());
            } else {
                player.getInventory().addItem(item);
            }
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
