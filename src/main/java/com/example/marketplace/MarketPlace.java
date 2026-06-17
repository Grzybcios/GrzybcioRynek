package com.example.marketplace;

import com.example.marketplace.commands.AukcjeCommand;
import com.example.marketplace.commands.MarketCommand;
import com.example.marketplace.commands.WystawCommand;
import com.example.marketplace.hooks.CustomItemsHook;
import com.example.marketplace.hooks.NexoHook;
import com.example.marketplace.hooks.OraxenHook;
import com.example.marketplace.hooks.VaultHook;
import com.example.marketplace.inventory.gui.GUIListener;
import com.example.marketplace.inventory.gui.GUIManager;
import com.example.marketplace.listeners.PlayerJoinListener;
import com.example.marketplace.managers.ConfigManager;
import com.example.marketplace.managers.GuiConfigManager;
import com.example.marketplace.managers.MarketManager;
import com.example.marketplace.managers.MessageManager;
import com.example.marketplace.model.MarketListing;
import com.example.marketplace.storage.StorageManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class MarketPlace extends JavaPlugin {
    private ConfigManager configManager;
    private GuiConfigManager guiConfigManager;
    private MessageManager messageManager;
    private StorageManager storageManager;
    private MarketManager marketManager;
    private GUIManager guiManager;
    private VaultHook vaultHook;
    private NexoHook nexoHook;
    private OraxenHook oraxenHook;
    private CustomItemsHook customItemsHook;

    @Override
    public void onEnable() {
        ConfigurationSerialization.registerClass(MarketListing.class);

        this.configManager = new ConfigManager(this);
        this.guiConfigManager = new GuiConfigManager(this);
        this.messageManager = new MessageManager(this);

        this.vaultHook = new VaultHook(this);
        if (!vaultHook.isEnabled()) {
            getLogger().severe("Vault nie znaleziono lub brak providera ekonomii! Wyłączanie pluginu.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.nexoHook = new NexoHook(this);
        this.oraxenHook = new OraxenHook(this);
        this.customItemsHook = new CustomItemsHook(nexoHook, oraxenHook);

        this.storageManager = new StorageManager(this);
        this.marketManager = new MarketManager(this);
        this.guiManager = new GUIManager();

        getCommand("market").setExecutor(new MarketCommand(this));
        getCommand("market").setTabCompleter(new MarketCommand(this));
        getCommand("wystaw").setExecutor(new WystawCommand(this));
        getCommand("wystaw").setTabCompleter(new WystawCommand(this));
        getCommand("aukcje").setExecutor(new AukcjeCommand(this));
        getCommand("aukcje").setTabCompleter(new AukcjeCommand(this));

        Bukkit.getPluginManager().registerEvents(new GUIListener(guiManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        Bukkit.getScheduler().runTaskTimer(this, () -> storageManager.removeExpiredListings(), 20L * 60 * 30, 20L * 60 * 30);

        getLogger().info("GrzybcioRynek został włączony!");
        getLogger().info("Załadowano " + storageManager.getAllListings().size() + " aktywnych ofert.");
    }

    @Override
    public void onDisable() {
        if (storageManager != null) {
            storageManager.save();
        }
        getLogger().info("GrzybcioRynek został wyłączony!");
    }
}
