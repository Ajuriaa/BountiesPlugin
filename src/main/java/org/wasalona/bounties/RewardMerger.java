package org.wasalona.bounties;

import java.util.*;

public class RewardMerger {
    public static String mergeRewards(String currentReward, String raisedItems) {
        Map<String, Integer> rewardMap = new HashMap<>();

        // Parse currentReward string
        parseRewardString(currentReward, rewardMap);

        // Parse raisedItems string
        parseRewardString(raisedItems, rewardMap);

        // Convert map back to string
        StringBuilder mergedRewards = new StringBuilder();
        for (Map.Entry<String, Integer> entry : rewardMap.entrySet()) {
            mergedRewards.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
        }

        // Remove trailing comma and space
        if (mergedRewards.length() > 0) {
            mergedRewards.setLength(mergedRewards.length() - 2);
        }

        return mergedRewards.toString();
    }

    private static void parseRewardString(String rewardString, Map<String, Integer> rewardMap) {
        String[] pairs = rewardString.split(", ");
        for (String pair : pairs) {
            String[] keyValue = pair.split(": ");
            String key = keyValue[0];
            int value = Integer.parseInt(keyValue[1]);
            rewardMap.put(key, rewardMap.getOrDefault(key, 0) + value);
        }
    }
}