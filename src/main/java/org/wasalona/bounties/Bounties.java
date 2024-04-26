package org.wasalona.bounties;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.event.EventHandler;

public final class Bounties extends JavaPlugin implements CommandExecutor, Listener {
    private Player sender;
    private Player target;

    @Override
    public void onEnable() {
        getLogger().info("Bounties plugin has been enabled!");
        // Register command executor for /bounty command
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Bounties plugin has been disabled!");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = getSender();
        Player target = getTarget();

        Inventory clickedInventory = event.getClickedInventory();
        InventoryView invView = event.getView();
        int slot = event.getRawSlot();

        if (clickedInventory == null) return;

        if (invView.getTitle().equals(ChatColor.DARK_GREEN + "Bounty")) {
            if (slot == 18) {
                ItemStack[] items = clickedInventory.getStorageContents();

                returnItems(items, player);
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "Bounty creation has been cancelled.");
                event.setCancelled(true);
            }else if (slot == 26) {
                ItemStack[] items = clickedInventory.getStorageContents();


                for (ItemStack item : items) {
                    if (item != null && item.getType() != Material.AIR) {
                        System.out.println(item.getType() == Material.valueOf("LIGHTMANSCURRENCY_COIN_DIAMOND"));
                        System.out.println("Found an item: " + item.getType());
                        System.out.println("Quantity: " + item.getAmount());
                    }

                    player.sendMessage("Items has been added to the bounty.");
                }

                player.sendMessage(ChatColor.GREEN + "Bounty has been placed!");
                player.closeInventory();
                event.setCancelled(true);
            }else {
                return;
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("bounty")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("You must be a player to use this command!");
                return false;
            }

            if (args.length != 1) {
                sender.sendMessage("Usage: /bounty <playername>");
                return false;
            }

            Player player = (Player) sender;
            Player target = Bukkit.getPlayer(args[0]);

            if (target == null || !target.isOnline()) {
                player.sendMessage("Player not found or is not online.");
                return false;
            }

            Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Bounty");

            // Clear the inventory
            inv.clear();

            // Add unmovable items
            ItemStack cancelItem = createItem(Material.RED_WOOL, ChatColor.RED + "Cancel");
            ItemStack acceptItem = createItem(Material.GREEN_WOOL, ChatColor.GREEN + "Accept");

            inv.setItem(18, cancelItem);
            inv.setItem(26, acceptItem);

            player.openInventory(inv);
            setSender(player);
            setTarget(target);

            return true;
        }
        return false;
    }


    public Player getSender() {
        return sender;
    }

    public void setSender(Player sender) {
        this.sender = sender;
    }

    public Player getTarget() {
        return target;
    }

    public void setTarget(Player target) {
        this.target = target;
    }

    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(name);
        item.setItemMeta(meta);

        return item;
    }

    private void returnItems(ItemStack[] items, Player player) {
        for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR && item.getType() != Material.RED_WOOL && item.getType() != Material.GREEN_WOOL) {
                executeCommand("give " + player.getDisplayName() + " lightmanscurrency:coin_diamond " + item.getAmount());
            }

            player.sendMessage("Items has been added to the bounty.");
        }
    }

    private void executeCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}
