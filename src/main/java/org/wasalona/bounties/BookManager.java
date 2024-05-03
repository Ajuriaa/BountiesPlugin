package org.wasalona.bounties;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.ChatColor;

public class BookManager {

    public static void giveWrittenBook(Player player, Player target, String code) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        // Set the title and author of the book
        assert meta != null;
        meta.setTitle(ChatColor.translateAlternateColorCodes('&', "CLAIM BOUNTY"));
        meta.setAuthor(ChatColor.translateAlternateColorCodes('&', "Wasalona Police"));

        // Set the content of the book
        String pageContent = "Congratulations on killing " + target.getDisplayName() +
                "!!!\nTo receive the bounty present this book to Aju.\nClaim code: " + ChatColor.translateAlternateColorCodes('&', code) +
                "\n Bounty will be given to you once the code is verified. \n Thanks for your service " + player.getDisplayName() + "! \n" +
                ChatColor.BLUE + "Wasalona Police Department";
        meta.addPage(pageContent);

        book.setItemMeta(meta);

        // Give the written book to the player
        player.getInventory().addItem(book);
    }
}