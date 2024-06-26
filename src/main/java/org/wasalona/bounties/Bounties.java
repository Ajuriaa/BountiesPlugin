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
    private final MessageBroadcast messageBroadcast = new MessageBroadcast();
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
        if(event == null) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        InventoryView invView = event.getView();

        if (!invView.getTitle().equals(ChatColor.DARK_GREEN + "Bounty") && !invView.getTitle().equals(ChatColor.DARK_GREEN + "Raise Bounty")) return;
        Player player = getSender();
        Player target = getTarget();

        Inventory clickedInventory = event.getClickedInventory();
        int slot = event.getRawSlot();

        if (clickedInventory == null) return;

        if (invView.getTitle().equals(ChatColor.DARK_GREEN + "Bounty")) {
            if (slot == 18) {
                ItemStack[] items = clickedInventory.getStorageContents();

                returnItems(items, player);
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "Bounty creation has been cancelled, items returned.");
                event.setCancelled(true);
            } else if (slot == 26) {
                ItemStack[] items = clickedInventory.getStorageContents();

                if(!validateItems(items)){
                    player.closeInventory();
                    returnItems(items, player);
                    player.sendMessage(ChatColor.RED + "Place coins in the inventory to create bounty.");
                    event.setCancelled(true);
                    return;
                }

                if(!haveMinimum(items, "create")){
                    player.closeInventory();
                    returnItems(items, player);
                    player.sendMessage(ChatColor.RED + "You must have at least 20 diamond coins, 1 emerald or netherite coin to create a bounty.");
                    event.setCancelled(true);
                    return;
                }

                if(createBounty(player, target, items)) {
                    messageBroadcast.printPlayerLocation(target);
                    player.sendMessage(ChatColor.GREEN + "Bounty has been placed!");
                } else {
                    player.sendMessage(ChatColor.RED + "Error creating bounty!");
                }
                player.closeInventory();
                event.setCancelled(true);
            }
        }

        if (invView.getTitle().equals(ChatColor.DARK_GREEN + "Raise Bounty")) {
            if (slot == 18) {
                ItemStack[] items = clickedInventory.getStorageContents();

                returnItems(items, player);
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "Bounty raise has been cancelled, items returned.");
                event.setCancelled(true);
            }else if (slot == 26) {
                ItemStack[] items = clickedInventory.getStorageContents();

                if(!validateItems(items)){
                    player.closeInventory();
                    returnItems(items, player);
                    player.sendMessage(ChatColor.RED + "Place coins in the inventory to raise bounty.");
                    event.setCancelled(true);
                    return;
                }

                if(!haveMinimum(items, "raise")){
                    player.closeInventory();
                    returnItems(items, player);
                    player.sendMessage(ChatColor.RED + "You must have at least 15 diamond coins, 1 emerald or netherite coin to raise a bounty.");
                    event.setCancelled(true);
                    return;
                }

                if(raiseBounty(player, target, items)) {
                    Bukkit.broadcastMessage(ChatColor.GOLD + "Bounty has been raised on " + target.getName() + " by " + player.getName() + "!");
                    messageBroadcast.messagePlayerLocation(target, player);
                    player.sendMessage(ChatColor.GREEN + "Bounty has been raised!");
                } else {
                    player.sendMessage(ChatColor.RED + "Error raising bounty!");
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
                sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
                return true;
            }

            Player player = (Player) sender;
            setSender(player);

            // Check permissions
            if (!player.hasPermission("bounties.bounty")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }

            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "Usage: /bounty <playername>");
                return true;
            }

            // Check sub-commands
            if (args[0].equalsIgnoreCase("checkcode")) {
                if (!player.hasPermission("bounties.checkcode")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }
                // Handle checkcode command
                if (args.length != 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /bounty checkcode <code>");
                    return true;
                }
                String code = args[1];
                handleCheckCode(player, code);
                return true;
            }

            if (args[0].equalsIgnoreCase("raise")) {
                if (!player.hasPermission("bounties.raise")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }
                // Handle checkcode command
                if (args.length != 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /bounty raise <playername>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                setTarget(target);

                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }

                if(player.getUniqueId() == target.getUniqueId()) {
                    player.sendMessage(ChatColor.RED + "You cannot raise your own bounty!");
                    return true;
                }

                if(!databaseManager.hasActiveBounty(target.getUniqueId().toString())) {
                    player.sendMessage(ChatColor.RED + "The player does not have an active bounty!");
                    return true;
                }

                bountyRaise(target, player);
                return true;
            }

            if (args[0].equalsIgnoreCase("items")) {
                if (!player.hasPermission("bounties.items")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }
                // Handle items command
                if (args.length != 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /bounty items <playername> <code>");
                    return true;
                }
                String code = args[2];
                Player claimer = Bukkit.getPlayer(args[1]);
                if (claimer == null || !claimer.isOnline()) {
                    player.sendMessage(ChatColor.RED + "Player not found or is not online.");
                    return false;
                }
                giveReward(claimer, player, code);
                return true;
            }

            if (args[0].equalsIgnoreCase("location")) {
                if (!player.hasPermission("bounties.location")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }

                // Handle location command
                if (args.length != 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /bounty location <playername>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(ChatColor.RED + "Player not found or is not online.");
                    return true;
                }

                if(!databaseManager.hasActiveBounty(target.getUniqueId().toString())) {
                    player.sendMessage(ChatColor.RED + "The player does not have an active bounty.");
                    return true;
                }

                getCurrentCoordinates(target, player);
                return true;
            }

            if (args[0].equalsIgnoreCase("top")) {
                if (!player.hasPermission("bounties.top")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }

                // Handle location command
                if (args.length != 1) {
                    player.sendMessage(ChatColor.RED + "Usage: /bounty top");
                    return true;
                }

                List<BountyDetails> activeBounties = databaseManager.getActiveBounties();

                BountySorter bountySorter = new BountySorter();

                bountySorter.displayTopBounties(activeBounties, player);

                return true;
            }

            // Handle main bounty command
            if (args.length != 1) {
                player.sendMessage(ChatColor.RED + "Usage: /bounty <playername>");
                return true;
            }

            if(databaseManager.hasActiveBounty(player.getUniqueId().toString())) {
                player.sendMessage(ChatColor.RED + "You have an active bounty, you cannot create bounties!");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                player.sendMessage(ChatColor.RED + "Player not found or is not online.");
                return true;
            }

            if(!databaseManager.canCreateBounty(player.getUniqueId().toString())){
                String timeLeft = databaseManager.getTimeRemainingToCreateBounty(player.getUniqueId().toString());
                player.sendMessage(ChatColor.RED + "You have a bounty cooldown. Time left: " + timeLeft);
                player.closeInventory();
                return true;
            }

            if(player.getUniqueId() == target.getUniqueId()) {
                player.sendMessage(ChatColor.RED + "You cannot create a bounty on yourself!");
                return true;
            }

            if(databaseManager.hasActiveBounty(target.getUniqueId().toString())) {
                player.sendMessage(ChatColor.RED + "The player has an active bounty, use /bounty raise instead!");
                return true;
            }

            openInv(player, false);
            setTarget(target);
            return true;
        }
        return false;
    }

    private void giveReward(Player claimer, Player admin, String code) {
        String items = databaseManager.getItems(code);
        if (items == null || items.isEmpty()) {
            claimer.sendMessage(ChatColor.RED + "Bounty already claimed!");
            admin.sendMessage(ChatColor.RED + "Bounty already claimed!");
            return;
        }

        List<String[]> itemList = CurrencyParser.parseCurrencyString(items);

        databaseManager.setClaimer(claimer, code);

        for (String[] item : itemList) {
            if (item == null) {
                continue;
            }

            String name = item[0];
            int amount = Integer.parseInt(item[1]);
            String command = "give " + claimer.getName() + " " + "lightmanscurrency:coin_" + name + " " + amount;

            executeCommand(command);
        }

        claimer.sendMessage(ChatColor.GREEN + "Items have been added to your inventory!");
    }

    private void openInv(Player player, boolean isRaise) {
        String title = isRaise ? "Raise Bounty" : "Bounty";

        // Open bounty inventory
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.DARK_GREEN + title);
        // Clear the inventory
        inv.clear();
        // Add unmovable items
        ItemStack cancelItem = createItem(Material.RED_WOOL, ChatColor.RED + "Cancel");
        ItemStack acceptItem = createItem(Material.GREEN_WOOL, ChatColor.GREEN + "Accept");
        inv.setItem(18, cancelItem);
        inv.setItem(26, acceptItem);
        player.openInventory(inv);
    }

    private void bountyRaise(Player target, Player player) {
        openInv(player, true);
    }

    private void getCurrentCoordinates(Player target, Player player) {
        Inventory inv = player.getInventory();
        ItemStack[] items = inv.getContents();
        boolean paid = false;

        if (items.length == 0) {
            player.sendMessage("You cannot afford the target coordinates!");
        }

        for (ItemStack item : items) {
            if(item == null || item.getItemMeta() == null) {
                continue;
            }

            boolean isDiamondCoin = item.getType() == Material.valueOf("LIGHTMANSCURRENCY_COIN_DIAMOND");

            if (isDiamondCoin && item.getAmount() >= 15) {
                item.setAmount(item.getAmount() - 15);
                paid = true;
                break;
            }
        }

        if(paid) {
            messageBroadcast.messagePlayerLocation(target, player);
        } else {
            player.sendMessage("You do not have enough diamond coins to purchase the target coordinates!");
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

    private boolean haveMinimum(ItemStack[] items, String type) {
        boolean isCreate = type.equals("create");
        int minCoins = isCreate ? 20 : 15;
        for (ItemStack item : items) {
            if(item == null || item.getItemMeta() == null) {
                continue;
            }

            boolean isCoin = CoinList.contains(item);
            if ( Arrays.asList(defaultItems).contains(item.getType()) || !isCoin) {
                continue;
            }

            if(item.getType() == Material.valueOf("LIGHTMANSCURRENCY_COIN_EMERALD") || item.getType() == Material.valueOf("LIGHTMANSCURRENCY_COIN_NETHERITE")) {
                return true;
            }

            if(item.getType() == Material.valueOf("LIGHTMANSCURRENCY_COIN_DIAMOND") && item.getAmount() >= minCoins) {
                return true;
            }
        }
        return false;
    }

    private Boolean createBounty(Player player, Player target, ItemStack[] items) {
       return databaseManager.createBounty(player, target, items);
    }

    private Boolean raiseBounty(Player player, Player target, ItemStack[] items) {
       return databaseManager.raiseBounty(player, target, items);
    }

    private void executeCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}
