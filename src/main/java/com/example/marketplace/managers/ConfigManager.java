package com.example.marketplace.managers;

import com.example.marketplace.MarketPlace;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class ConfigManager {
    private final MarketPlace plugin;
    private FileConfiguration config;
    
    private int maxListingsPerPlayer;
    private int listingExpiryDays;
    private int guiRows;
    private double minPrice;
    private double maxPrice;
    
    public ConfigManager(MarketPlace plugin) {
        this.plugin = plugin;
        reload();
    }
    
    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        
        this.maxListingsPerPlayer = config.getInt("settings.max-listings-per-player", 10);
        this.listingExpiryDays = config.getInt("settings.listing-expiry-days", 7);
        this.guiRows = config.getInt("settings.gui-rows", 6);
        this.minPrice = config.getDouble("settings.min-price", 1.0);
        this.maxPrice = config.getDouble("settings.max-price", 1000000.0);

        if (!config.isSet("messages.buy.seller-notified")) {
            config.set(
                "messages.buy.seller-notified",
                "Twoj przedmiot &e{item} &7zostal sprzedany graczowi &e{buyer} &7za &a{price}"
            );
            plugin.saveConfig();
        }
    }
}