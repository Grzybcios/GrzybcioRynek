package com.example.marketplace.util;

import com.example.marketplace.model.MarketListing;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class SellerNotifier {
    private SellerNotifier() {
    }

    public static Player findOnlineSeller(MarketListing listing) {
        if (listing == null) {
            return null;
        }

        Player seller = Bukkit.getPlayer(listing.getSeller());
        if (seller != null) {
            return seller;
        }

        String sellerName = listing.getSellerName();
        if (!isValidName(sellerName)) {
            return null;
        }

        seller = Bukkit.getPlayerExact(sellerName);
        if (seller != null) {
            return seller;
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getName().equalsIgnoreCase(sellerName)) {
                return online;
            }
        }

        return null;
    }

    private static boolean isValidName(String name) {
        return name != null && !name.isEmpty() && !"Nieznany".equalsIgnoreCase(name);
    }
}
