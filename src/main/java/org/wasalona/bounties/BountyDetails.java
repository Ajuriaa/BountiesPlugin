package org.wasalona.bounties;

import java.util.HashMap;
import java.util.Map;

public class BountyDetails {
    private int targetPlayerId;
    private Map<String, Integer> rewardItems;

    public BountyDetails(int targetPlayerId, String rewardItems) {
        this.targetPlayerId = targetPlayerId;
        this.rewardItems = CurrencyParser.convertToMap(rewardItems);
    }

    public int getTargetPlayerId() {
        return targetPlayerId;
    }

    public void setTargetPlayerId(int targetPlayerId) {
        this.targetPlayerId = targetPlayerId;
    }

    public Map<String, Integer> getRewardItems() {
        return rewardItems;
    }

    public void setRewardItems(Map<String, Integer> rewardItems) {
        this.rewardItems = rewardItems;
    }
}