package org.wasalona.bounties;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Bounty {
    private final UUID issuerId;
    private final UUID targetPlayerId;
    private final ItemStack[] rewardItems;

    public Bounty(UUID issuerId, UUID targetId, ItemStack[] rewardItems) {
        this.issuerId = issuerId;
        this.targetPlayerId = targetId;
        this.rewardItems = rewardItems;
    }

    public UUID getIssuerId() {
        return issuerId;
    }

    public UUID getTargetPlayerId() {
        return targetPlayerId;
    }

    public ItemStack[] getRewardItems() {
        return rewardItems;
    }
}
