package com.example.marketplace.inventory.impl;

import com.example.marketplace.MarketPlace;
import com.example.marketplace.inventory.InventoryButton;
import com.example.marketplace.inventory.InventoryGUI;
import com.example.marketplace.inventory.MarketCategory;
import com.example.marketplace.managers.GuiConfigManager;
import com.example.marketplace.model.MarketListing;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MarketConfirmGUI extends InventoryGUI {
    private final MarketPlace plugin;
    private final int listingId;
    private final int page;
    private final MarketCategory category;

    public MarketConfirmGUI(MarketPlace plugin, int listingId, int page, MarketCategory category) {
        this.plugin = plugin;
        this.listingId = listingId;
        this.page = page;
        this.category = category;
    }

    private GuiConfigManager guiConfig() {
        return plugin.getGuiConfigManager();
    }

    @Override
    protected Inventory createInventory() {
        String title = plugin.getMessageManager().getMessage("gui.confirm.title");
        return Bukkit.createInventory(null, guiConfig().getConfirmSize(), title);
    }

    @Override
    public void decorate(Player player) {
        fillBorder();

        MarketListing listing = plugin.getStorageManager().getListing(listingId);

        addButton(guiConfig().getConfirmItemSlot(), new InventoryButton()
            .creator(p -> listing != null ? createPreviewItem(listing) : createGlassPane())
            .consumer(event -> {})
        );

        if (listing != null) {
            addButton(guiConfig().getConfirmSlot(), new InventoryButton()
                .creator(p -> createActionItem(guiConfig().getConfirmButtonMaterial(), "gui.confirm.confirm"))
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    clicker.closeInventory();
                    plugin.getMarketManager().buyItem(clicker, listingId);
                    plugin.getGuiManager().openGUI(new MarketBrowseGUI(plugin, page, category), clicker);
                })
            );
        } else {
            addButton(guiConfig().getConfirmSlot(), new InventoryButton()
                .creator(p -> createGlassPane())
                .consumer(event -> {})
            );
        }

        addButton(guiConfig().getConfirmCancelSlot(), new InventoryButton()
            .creator(p -> createActionItem(guiConfig().getCancelButtonMaterial(), "gui.confirm.cancel"))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                clicker.closeInventory();
                plugin.getGuiManager().openGUI(new MarketBrowseGUI(plugin, page, category), clicker);
            })
        );

        super.decorate(player);
    }

    private void fillBorder() {
        if (!guiConfig().isConfirmFillerEnabled()) {
            return;
        }

        int confirmSlot = guiConfig().getConfirmSlot();
        int itemSlot = guiConfig().getConfirmItemSlot();
        int cancelSlot = guiConfig().getConfirmCancelSlot();

        for (int slot = 0; slot < guiConfig().getConfirmSize(); slot++) {
            if (slot == confirmSlot || slot == itemSlot || slot == cancelSlot) {
                continue;
            }

            addButton(slot, new InventoryButton()
                .creator(p -> createGlassPane())
                .consumer(event -> {})
            );
        }
    }

    private ItemStack createPreviewItem(MarketListing listing) {
        ItemStack displayItem = plugin.getCustomItemsHook().recreateItem(listing);
        ItemMeta meta = displayItem.getItemMeta();

        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(displayItem.getType());
        }

        List<String> lore = new ArrayList<>();
        lore.add(plugin.getMessageManager().getMessage("gui.confirm.question"));
        lore.add("");

        Map<String, String> replacements = plugin.getMessageManager().createReplacements();
        replacements.put("price", plugin.getVaultHook().format(listing.getPrice()));
        replacements.put("seller", listing.getSellerName());
        replacements.put("item", getItemName(displayItem));

        lore.add(plugin.getMessageManager().getMessage("gui.confirm.price", replacements));
        lore.add(plugin.getMessageManager().getMessage("gui.lore.seller", replacements));

        meta.setLore(lore);
        displayItem.setItemMeta(meta);
        return displayItem;
    }

    private ItemStack createActionItem(Material material, String messagePath) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getMessageManager().getMessage(messagePath));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGlassPane() {
        ItemStack item = new ItemStack(guiConfig().getConfirmFillerMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(guiConfig().getConfirmFillerName());
            item.setItemMeta(meta);
        }
        return item;
    }

    private String getItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().name().replace("_", " ");
    }
}
