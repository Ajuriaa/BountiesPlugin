package org.wasalona.bounties;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class BountySorter {
    private static final int NETHERITE_TO_EMERALD = 64;
    private static final int EMERALD_TO_DIAMOND = 64;
    private static final int DIAMOND_TO_GOLD = 64;
    private static final int GOLD_TO_IRON = 64;
    private static final int IRON_TO_COPPER = 64;

    public void displayTopBounties(List<BountyDetails> activeBounties, Player player) {
        if (activeBounties.isEmpty()) {
            // If there are no active bounties, inform the player
            player.sendMessage("There are no active bounties at the moment!");
            return;
        }

        // Sort the bounties based on their converted values
        activeBounties.sort(Comparator.comparingInt(this::convertBountyValue));

        // Get the top 3 bounties (or all if there are less than 3)
        int numBountiesToShow = Math.min(activeBounties.size(), 3);
        List<BountyDetails> topBounties = activeBounties.subList(0, numBountiesToShow);

        // Display the top 3 bounties to the player
        player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "TOP BOUNTIES:");
        for (int i = 0; i < topBounties.size(); i++) {
            BountyDetails bounty = topBounties.get(i);
            String playerName = getPlayerNameFromID(bounty.getTargetPlayerId());
            player.sendMessage(ChatColor.DARK_PURPLE + "- " + ChatColor.LIGHT_PURPLE  + ChatColor.BOLD + playerName);
            player.sendMessage(ChatColor.DARK_PURPLE + "Worth: " + ChatColor.GOLD + ChatColor.BOLD + convertBountyValue(bounty) + " copper coins");
        }
    }

    private int convertBountyValue(BountyDetails bounty) {
        Map<String, Integer> rewardItems = bounty.getRewardItems();

        // Initialize variables to keep track of the total value
        int totalValue = 0;

        // Calculate the total value based on the reward items
        totalValue += rewardItems.getOrDefault("NETHERITE", 0) * NETHERITE_TO_EMERALD * EMERALD_TO_DIAMOND * DIAMOND_TO_GOLD * GOLD_TO_IRON * IRON_TO_COPPER;
        totalValue += rewardItems.getOrDefault("EMERALD", 0) * EMERALD_TO_DIAMOND * DIAMOND_TO_GOLD * GOLD_TO_IRON * IRON_TO_COPPER;
        totalValue += rewardItems.getOrDefault("DIAMOND", 0) * DIAMOND_TO_GOLD * GOLD_TO_IRON * IRON_TO_COPPER;
        totalValue += rewardItems.getOrDefault("GOLD", 0) * GOLD_TO_IRON * IRON_TO_COPPER;
        totalValue += rewardItems.getOrDefault("IRON", 0) * IRON_TO_COPPER;
        totalValue += rewardItems.getOrDefault("COPPER", 0);

        return totalValue;
    }

    private String getPlayerNameFromID(int playerId) {
        DatabaseManager databaseManager = new DatabaseManager();
        return databaseManager.getPlayerNameByID(playerId);
    }
}
