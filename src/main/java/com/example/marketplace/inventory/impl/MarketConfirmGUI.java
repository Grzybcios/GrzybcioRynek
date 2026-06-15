package com.example.marketplace.inventory.impl;

import com.example.marketplace.MarketPlace;
import com.example.marketplace.inventory.InventoryButton;
import com.example.marketplace.inventory.InventoryGUI;
import com.example.marketplace.inventory.MarketCategory;
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
    private static final int INVENTORY_SIZE = 27;
    private static final int SLOT_CONFIRM = 11;
    private static final int SLOT_ITEM = 13;
    private static final int SLOT_CANCEL = 15;

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

    @Override
    protected Inventory createInventory() {
        String title = plugin.getMessageManager().getMessage("gui.confirm.title");
        return Bukkit.createInventory(null, INVENTORY_SIZE, title);
    }

    @Override
    public void decorate(Player player) {
        fillBorder();

        MarketListing listing = plugin.getStorageManager().getListing(listingId);

        addButton(SLOT_ITEM, new InventoryButton()
            .creator(p -> listing != null ? createPreviewItem(listing) : createGlassPane())
            .consumer(event -> {})
        );

        if (listing != null) {
            addButton(SLOT_CONFIRM, new InventoryButton()
                .creator(p -> createActionItem(Material.LIME_CONCRETE, "gui.confirm.confirm"))
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    clicker.closeInventory();
                    plugin.getMarketManager().buyItem(clicker, listingId);
                    plugin.getGuiManager().openGUI(new MarketBrowseGUI(plugin, page, category), clicker);
                })
            );
        } else {
            addButton(SLOT_CONFIRM, new InventoryButton()
                .creator(p -> createGlassPane())
                .consumer(event -> {})
            );
        }

        addButton(SLOT_CANCEL, new InventoryButton()
            .creator(p -> createActionItem(Material.RED_CONCRETE, "gui.confirm.cancel"))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                clicker.closeInventory();
                plugin.getGuiManager().openGUI(new MarketBrowseGUI(plugin, page, category), clicker);
            })
        );

        super.decorate(player);
    }

    private void fillBorder() {
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            if (slot == SLOT_CONFIRM || slot == SLOT_ITEM || slot == SLOT_CANCEL) {
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
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
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
