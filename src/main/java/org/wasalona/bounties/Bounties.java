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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class Bounties extends JavaPlugin implements CommandExecutor, Listener {
    private Player sender;
    private Player target;
    private DatabaseManager databaseManager;
    private final Material[] defaultItems = { Material.AIR, Material.RED_WOOL, Material.GREEN_WOOL };


    @Override
    public void onEnable() {
        getLogger().info("BountiesPlugin has been enabled!");

        // Initialize the database connection
        databaseManager = new DatabaseManager();

        // Register command executor for /bounty command
        Objects.requireNonNull(getCommand("bounty")).setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("BountiesPlugin has been disabled!");
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
                player.sendMessage(ChatColor.RED + "Bounty creation has been cancelled, items returned.");
                event.setCancelled(true);
            }else if (slot == 26) {
                ItemStack[] items = clickedInventory.getStorageContents();

                if(createBounty(player, target, items)) {
                    player.sendMessage(ChatColor.GREEN + "Bounty has been placed!");
                } else {
                    player.sendMessage(ChatColor.RED + "Error creating bounty!");
                }
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

            if(sender.hasPermission("bounty.create")) {
                sender.sendMessage(ChatColor.RED + "You cannot have permission to run this command.");
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
            if (item == null || Arrays.asList(defaultItems).contains(item.getType())) {
                return;
            }

            int amount = item.getAmount();
            /// TODO VERIFICAR ESTA VALIDATION
            if (CoinList.contains(item.getType())) {
                // Returns coin item to the player's inventory
                String command = CoinList.getCommand(item.getType());
                executeCommand("give " + player.getDisplayName() + " " + command + " " + amount);
            } else {
                // Return the non-coin item to the player's inventory
                player.getInventory().addItem(item);
            }
        }
    }

    private Boolean createBounty(Player player, Player target, ItemStack[] items) {
       return databaseManager.createBounty(player, target, items);
    }

    private void executeCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}
