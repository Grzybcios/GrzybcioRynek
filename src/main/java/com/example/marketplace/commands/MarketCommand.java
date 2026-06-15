package com.example.marketplace.commands;

import com.example.marketplace.MarketPlace;
import com.example.marketplace.inventory.impl.MarketBrowseGUI;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MarketCommand implements CommandExecutor, TabCompleter {
    private final MarketPlace plugin;

    public MarketCommand(MarketPlace plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("market.use")) {
            plugin.getMessageManager().sendMessage(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                plugin.getMessageManager().sendMessage(sender, "player-only");
                return true;
            }
            return handleBrowse((Player) sender);
        }

        switch (args[0].toLowerCase()) {
            case "sell":
            case "sprzedaj":
                return requirePlayer(sender, player -> handleSell(player, args));

            case "browse":
            case "przegladaj":
                return requirePlayer(sender, this::handleBrowse);

            case "remove":
            case "usun":
                return requirePlayer(sender, player -> handleRemove(player, args));

            case "reload":
                return handleReload(sender);

            default:
                plugin.getMessageManager().sendMessage(sender, "usage.main");
                return true;
        }
    }

    private boolean requirePlayer(CommandSender sender, PlayerHandler handler) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "player-only");
            return true;
        }
        return handler.handle((Player) sender);
    }

    private boolean handleSell(Player player, String[] args) {
        if (!player.hasPermission("market.sell")) {
            plugin.getMessageManager().sendMessage(player, "no-permission");
            return true;
        }

        if (args.length < 2) {
            plugin.getMessageManager().sendMessage(player, "usage.sell");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            plugin.getMessageManager().sendMessage(player, item == null ? "sell.no-item" : "sell.air-item");
            return true;
        }

        double price;
        try {
            price = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            plugin.getMarketManager().sendInvalidPriceMessage(player);
            return true;
        }

        plugin.getMarketManager().sellItem(player, item, price);
        return true;
    }

    private boolean handleBrowse(Player player) {
        MarketBrowseGUI gui = new MarketBrowseGUI(plugin, 1);
        plugin.getGuiManager().openGUI(gui, player);
        return true;
    }

    private boolean handleRemove(Player player, String[] args) {
        if (args.length < 2) {
            plugin.getMessageManager().sendMessage(player, "usage.remove");
            return true;
        }

        int listingId;
        try {
            listingId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            plugin.getMessageManager().sendMessage(player, "usage.remove");
            return true;
        }

        plugin.getMarketManager().removeListing(player, listingId);
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("market.reload")) {
            plugin.getMessageManager().sendMessage(sender, "no-permission");
            return true;
        }

        plugin.getConfigManager().reload();
        plugin.getStorageManager().load();
        plugin.getMessageManager().sendMessage(sender, "reload.success");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("market.use")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>(Arrays.asList(
                "sell", "sprzedaj",
                "browse", "przegladaj",
                "remove", "usun",
                "reload"
            ));

            return subcommands.stream()
                .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("sell") || args[0].equalsIgnoreCase("sprzedaj")) {
                return Arrays.asList("<cena>");
            }
            if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("usun")) {
                return Arrays.asList("<id>");
            }
        }

        return new ArrayList<>();
    }

    @FunctionalInterface
    private interface PlayerHandler {
        boolean handle(Player player);
    }
}
