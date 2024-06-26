package org.wasalona.bounties;

import org.bukkit.ChatColor;
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

        if(databaseManager.checkBountyIssuer(code, killer)) {
            killer.sendMessage(ChatColor.RED + "You can't claim a bounty you issued!");
            return;
        }

        boolean bountyUpdated = databaseManager.updateBountyToInactive(player.getUniqueId().toString());

        if(bountyUpdated) {
            BookManager.giveWrittenBook(killer, player, code);
            killer.sendMessage(" " + ChatColor.GOLD + ChatColor.BOLD + "Go to the police station to get the reward!");
        }
    }
}
