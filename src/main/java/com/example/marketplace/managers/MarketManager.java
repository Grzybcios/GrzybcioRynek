package com.example.marketplace.managers;

import com.example.marketplace.MarketPlace;
import com.example.marketplace.model.MarketListing;
import com.example.marketplace.util.SellerNotifier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class MarketManager {
    private final MarketPlace plugin;
    private final Object transactionLock = new Object();

    public MarketManager(MarketPlace plugin) {
        this.plugin = plugin;
    }

    public boolean sellItem(Player player, ItemStack item, double price) {
        if (price < plugin.getConfigManager().getMinPrice() || price > plugin.getConfigManager().getMaxPrice()) {
            sendInvalidPriceMessage(player);
            return false;
        }

        int currentListings = plugin.getStorageManager().getListingCountBySeller(player.getUniqueId());
        if (currentListings >= plugin.getConfigManager().getMaxListingsPerPlayer()) {
            Map<String, String> replacements = plugin.getMessageManager().createReplacements();
            replacements.put("limit", String.valueOf(plugin.getConfigManager().getMaxListingsPerPlayer()));
            plugin.getMessageManager().sendMessage(player, "sell.limit-reached", replacements);
            return false;
        }

        String nexoId = plugin.getCustomItemsHook().getNexoId(item);
        String oraxenId = plugin.getCustomItemsHook().getOraxenId(item);
        ItemStack itemClone = item.clone();

        plugin.getStorageManager().createListing(
            player.getUniqueId(),
            player.getName(),
            itemClone,
            price,
            oraxenId,
            nexoId
        );

        player.getInventory().setItemInMainHand(null);

        Map<String, String> replacements = plugin.getMessageManager().createReplacements();
        replacements.put("item", getItemName(itemClone));
        replacements.put("price", plugin.getVaultHook().format(price));
        plugin.getMessageManager().sendMessage(player, "sell.success", replacements);

        return true;
    }

    public boolean buyItem(Player buyer, int listingId) {
        synchronized (transactionLock) {
            MarketListing listing = plugin.getStorageManager().getListing(listingId);

            if (listing == null) {
                plugin.getMessageManager().sendMessage(buyer, "buy.not-available");
                return false;
            }

            if (listing.getSeller().equals(buyer.getUniqueId())) {
                plugin.getMessageManager().sendMessage(buyer, "buy.own-listing");
                return false;
            }

            if (!plugin.getVaultHook().has(buyer, listing.getPrice())) {
                Map<String, String> replacements = plugin.getMessageManager().createReplacements();
                replacements.put("price", plugin.getVaultHook().format(listing.getPrice()));
                plugin.getMessageManager().sendMessage(buyer, "buy.not-enough-money", replacements);
                return false;
            }

            if (listing.getPrice() <= 0) {
                plugin.getMessageManager().sendMessage(buyer, "buy.not-available");
                return false;
            }

            if (buyer.getInventory().firstEmpty() == -1) {
                plugin.getMessageManager().sendMessage(buyer, "buy.inventory-full");
                return false;
            }

            ItemStack item = plugin.getCustomItemsHook().recreateItem(listing);
            if (item == null || item.getType() == Material.AIR) {
                plugin.getMessageManager().sendMessage(buyer, "buy.not-available");
                return false;
            }

            double price = listing.getPrice();
            if (!plugin.getVaultHook().withdraw(buyer, price)) {
                Map<String, String> replacements = plugin.getMessageManager().createReplacements();
                replacements.put("price", plugin.getVaultHook().format(price));
                plugin.getMessageManager().sendMessage(buyer, "buy.not-enough-money", replacements);
                return false;
            }

            if (!plugin.getVaultHook().deposit(
                Bukkit.getOfflinePlayer(listing.getSeller()),
                price,
                listing.getSellerName()
            )) {
                plugin.getVaultHook().deposit(buyer, price);
                plugin.getMessageManager().sendMessage(buyer, "buy.transaction-failed");
                return false;
            }

            if (!plugin.getStorageManager().removeListing(listingId)) {
                plugin.getVaultHook().deposit(buyer, price);
                plugin.getVaultHook().withdraw(
                    Bukkit.getOfflinePlayer(listing.getSeller()),
                    price
                );
                plugin.getMessageManager().sendMessage(buyer, "buy.not-available");
                return false;
            }

            buyer.getInventory().addItem(item);

            Map<String, String> replacements = plugin.getMessageManager().createReplacements();
            replacements.put("item", getItemName(item));
            replacements.put("price", plugin.getVaultHook().format(price));
            plugin.getMessageManager().sendMessage(buyer, "buy.success", replacements);

            notifySeller(listing, buyer, item, price);

            return true;
        }
    }

    private void notifySeller(MarketListing listing, Player buyer, ItemStack item, double price) {
        Map<String, String> sellerReplacements = plugin.getMessageManager().createReplacements();
        sellerReplacements.put("buyer", buyer.getName());
        sellerReplacements.put("item", getPlainItemName(item));
        sellerReplacements.put("price", plugin.getVaultHook().format(price));

        Player seller = SellerNotifier.findOnlineSeller(listing);
        if (seller != null) {
            plugin.getServer().getScheduler().runTask(plugin, () ->
                plugin.getMessageManager().sendMessage(seller, "buy.seller-notified", sellerReplacements)
            );
            return;
        }

        plugin.getStorageManager().addPendingSaleNotification(
            listing.getSeller(),
            listing.getSellerName(),
            buyer.getName(),
            getPlainItemName(item),
            price
        );
    }

    public boolean removeListing(Player player, int listingId) {
        MarketListing listing = plugin.getStorageManager().getListing(listingId);

        if (listing == null) {
            Map<String, String> replacements = plugin.getMessageManager().createReplacements();
            replacements.put("id", String.valueOf(listingId));
            plugin.getMessageManager().sendMessage(player, "remove.not-found", replacements);
            return false;
        }

        if (!listing.getSeller().equals(player.getUniqueId()) && !player.hasPermission("market.admin")) {
            plugin.getMessageManager().sendMessage(player, "remove.not-owner");
            return false;
        }

        if (player.getInventory().firstEmpty() == -1) {
            plugin.getMessageManager().sendMessage(player, "buy.inventory-full");
            return false;
        }

        ItemStack item = plugin.getCustomItemsHook().recreateItem(listing);
        player.getInventory().addItem(item);

        plugin.getStorageManager().removeListing(listingId);

        Map<String, String> replacements = plugin.getMessageManager().createReplacements();
        replacements.put("id", String.valueOf(listingId));
        plugin.getMessageManager().sendMessage(player, "remove.success", replacements);

        return true;
    }

    public void sendInvalidPriceMessage(Player player) {
        Map<String, String> replacements = plugin.getMessageManager().createReplacements();
        replacements.put("min", plugin.getVaultHook().format(plugin.getConfigManager().getMinPrice()));
        replacements.put("max", plugin.getVaultHook().format(plugin.getConfigManager().getMaxPrice()));
        plugin.getMessageManager().sendMessage(player, "sell.invalid-price", replacements);
    }

    private String getItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().name().replace("_", " ");
    }

    private String getPlainItemName(ItemStack item) {
        return ChatColor.stripColor(getItemName(item));
    }
}
