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
        getServer().getPluginManager().registerEvents(new BountyListener(), this);

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

                if(!validateItems(items)){
                    player.closeInventory();
                    returnItems(items, player);
                    player.sendMessage(ChatColor.RED + "Place coins in the inventory to create bounty.");
                    event.setCancelled(true);
                    return;
                }

                if(createBounty(player, target, items)) {
                    MessageBroadcast messageBroadcast = new MessageBroadcast();

                    messageBroadcast.printPlayerLocation(target);
                    player.sendMessage(ChatColor.GREEN + "Bounty has been placed!");
                } else {
                    player.sendMessage(ChatColor.RED + "Error creating bounty!");
                }
                player.closeInventory();
                event.setCancelled(true);
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

            Player player = (Player) sender;

            if (args.length == 0) {
                sender.sendMessage("Usage: /bounty <playername>");
                return false;
            }

            if (args[0].equalsIgnoreCase("checkcode")) {

                // Check if the correct number of arguments is provided
                if (args.length != 2) {
                    player.sendMessage("Usage: /bounty checkcode <code>");
                    return false;
                }

                String code = args[1];
                handleCheckCode(player, code);
                return true;
            }

            if (args[0].equalsIgnoreCase("items")) {

                // Check if the correct number of arguments is provided
                if (args.length != 3) {
                    player.sendMessage("Usage: /bounty items <playername> <code>");
                    return false;
                }

                String code = args[2];
                Player claimer = Bukkit.getPlayer(args[1]);

                if (target == null || !target.isOnline()) {
                    player.sendMessage("Player not found or is not online.");
                    return false;
                }

                giveReward(claimer, player, code);
                return true;
            }

            if (args.length != 1) {
                sender.sendMessage("Usage: /bounty <playername>");
                return false;
            }

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

    private void giveReward(Player claimer, Player admin, String code) {
        String items = databaseManager.getItems(code);
        if (items == null || items.isEmpty()) {
            claimer.sendMessage(ChatColor.RED + "Code is invalid!");
            admin.sendMessage(ChatColor.RED + "Code is invalid!");
            return;
        }

        List<String[]> itemList = CurrencyParser.parseCurrencyString(items);

        for (String[] item : itemList) {
            if (item == null) {
                continue;
            }

            String name = item[0];
            int amount = Integer.parseInt(item[1]);
            String command = "give " + claimer.getName() + " " + "lightmanscurrency:coin_" + name + " " + amount;

            executeCommand(command);
        }
    }

    private void handleCheckCode(Player player, String code) {
        if (databaseManager.checkCode(code)) {
            player.sendMessage(ChatColor.GREEN + "Code is valid!");
        } else {
            player.sendMessage(ChatColor.RED + "Code is invalid!");
        }
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
                continue;
            }

            player.getInventory().addItem(item);
        }
    }

    private boolean validateItems(ItemStack[] items) {
        boolean hasItems = false;
        for (ItemStack item : items) {
            if(item == null || item.getItemMeta() == null) {
                continue;
            }

            boolean isCoin = CoinList.contains(item);
            if ( Arrays.asList(defaultItems).contains(item.getType()) || !isCoin) {
                continue;
            }

            hasItems = true;
        }
        return hasItems;
    }

    private Boolean createBounty(Player player, Player target, ItemStack[] items) {
       return databaseManager.createBounty(player, target, items);
    }

    private void executeCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}
