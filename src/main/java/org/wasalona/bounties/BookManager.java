package org.wasalona.bounties;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.ChatColor;

public class BookManager {

    public static void giveWrittenBook(Player player, String code) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        // Set the title and author of the book
        assert meta != null;
        meta.setTitle(ChatColor.translateAlternateColorCodes('&', "CLAIM BOUNTY"));
        meta.setAuthor(ChatColor.translateAlternateColorCodes('&', "Wasalona Police"));

        // Set the content of the book
        String pageContent = "Congratulations!!!\nTo receive the bounty present this book to Aju.\nClaim code: " +
                ChatColor.translateAlternateColorCodes('&', code);
        meta.addPage(pageContent);

        book.setItemMeta(meta);

        // Give the written book to the player
        player.getInventory().addItem(book);
    }
}