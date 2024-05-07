package org.wasalona.bounties;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.security.SecureRandom;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
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

    public boolean checkCode(String code) {
        boolean validCode = false;
        String query = "SELECT COUNT(*) AS valid_code FROM bounties " +
                "WHERE claim_code = ? AND claimer_player_id IS NULL AND active = 0";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, code);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int validCount = resultSet.getInt("valid_code");
                    validCode = validCount == 1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return validCode;
    }

    public String getItems(String code) {
        String items = "";

        String query = "SELECT reward_items FROM bounties " +
                "WHERE claim_code = ? AND active = 0 AND claimer_player_id IS NULL";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, code);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    items = resultSet.getString("reward_items");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public void setClaimer(Player player, String code) {
        String query = "UPDATE bounties " +
                "SET claimer_player_id = ? " +
                "WHERE claim_code = ?";

        int playerId = getPlayerId(player.getUniqueId().toString());

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, playerId);
            statement.setString(2, code);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

            updateLastBountyCreatedAt(senderUUID);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getRewardItems(int targetID) {
        String items = "";

        String query = "SELECT reward_items FROM bounties " +
                "WHERE target_player_id = ? AND active = 1";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, targetID);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    items = resultSet.getString("reward_items");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public boolean raiseBounty(Player player, Player target, ItemStack[] rewardItems) {
        // Get the UUIDs of the sender and target players
        String targetUUID = target.getUniqueId().toString();

        int targetId = getPlayerId(targetUUID);

        String formattedItems = getFormattedItems(rewardItems, player);
        String currentReward = getRewardItems(targetId);

        String newReward = RewardMerger.mergeRewards(currentReward, formattedItems);

        // Construct the SQL INSERT query
        String query = "UPDATE bounties " +
                "SET reward_items = ? " +
                "WHERE target_player_id = ? AND active = 1";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            // Set values for parameters in the prepared statement
            statement.setString(1, newReward);
            statement.setInt(2, targetId);

            // Execute the query
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateLastBountyCreatedAt(String playerUUID) {
        String query = "UPDATE players SET last_bounty_created_at = ? WHERE UUID = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setTimestamp(1, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            statement.setString(2, playerUUID);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean canCreateBounty(String playerUUID) {
        String query = "SELECT last_bounty_created FROM players WHERE UUID = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, playerUUID);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    LocalDateTime lastBountyCreatedAt = resultSet.getTimestamp("last_bounty_created_at").toLocalDateTime();
                    LocalDateTime currentDateTime = LocalDateTime.now();

                    Duration duration = Duration.between(lastBountyCreatedAt, currentDateTime);
                    long hoursPassed = duration.toHours();

                    return hoursPassed >= 35;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Default to false if an error occurs
        return false;
    }

    public String getTimeRemainingToCreateBounty(String playerUUID) {
        String query = "SELECT last_bounty_created_at FROM players WHERE UUID = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, playerUUID);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    LocalDateTime lastBountyCreatedAt = resultSet.getTimestamp("last_bounty_created_at").toLocalDateTime();
                    LocalDateTime currentDateTime = LocalDateTime.now();

                    Duration duration = Duration.between(lastBountyCreatedAt, currentDateTime);
                    long hoursPassed = duration.toHours();

                    long timeRemaining = 35 - hoursPassed;
                    if (timeRemaining < 1) {
                        long minutesRemaining = duration.toMinutes();
                        return minutesRemaining + " minutos";
                    } else {
                        return timeRemaining + " horas";
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Por defecto, devolver una cadena vacía si ocurre un error
        return "";
    }

    public boolean checkBountyIssuer(String code, Player player) {
        int playerId = getPlayerId(player.getUniqueId().toString());
        String query = "SELECT COUNT(*) AS matching_bounties FROM bounties " +
                "WHERE claim_code = ? AND creator_player_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, code);
            statement.setInt(2, playerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int matchingBounties = resultSet.getInt("matching_bounties");
                    return matchingBounties > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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
