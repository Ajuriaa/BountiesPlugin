package org.wasalona.bounties;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class BountyListener implements Listener {
    private final DatabaseManager databaseManager = new DatabaseManager();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        // Get the player who died
        Player player = event.getEntity();

        boolean hasBounty = databaseManager.hasActiveBounty(player.getUniqueId().toString());


        // Check if the death cause was another player
        if (player.getKiller() != null && hasBounty) {
            Player killer = player.getKiller();

            // Handle the case where the player was killed by another player
            handlePlayerKilledByPlayer(player, killer);
        }
    }

    private void handlePlayerKilledByPlayer(Player player, Player killer) {
        String code = databaseManager.getActiveBountyClaimCode(player.getUniqueId());
        boolean bountyUpdated = databaseManager.updateBountyToInactive(player.getUniqueId().toString());

        if(bountyUpdated) {
            BookManager.giveWrittenBook(killer, code);
            killer.sendMessage("Go to the police station to get the reward!");
        }
    }
}
