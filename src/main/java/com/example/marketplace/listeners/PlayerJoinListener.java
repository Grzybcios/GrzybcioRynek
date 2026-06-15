package com.example.marketplace.listeners;

import com.example.marketplace.MarketPlace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final MarketPlace plugin;

    public PlayerJoinListener(MarketPlace plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getStorageManager().deliverPendingReturns(player);
        plugin.getServer().getScheduler().runTaskLater(
            plugin,
            () -> plugin.getStorageManager().deliverPendingSaleNotifications(player),
            20L
        );
    }
}
