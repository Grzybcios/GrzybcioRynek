package com.example.marketplace.inventory.impl;

import com.example.marketplace.MarketPlace;
import com.example.marketplace.inventory.InventoryButton;
import com.example.marketplace.inventory.InventoryGUI;
import com.example.marketplace.inventory.MarketCategory;
import com.example.marketplace.managers.GuiConfigManager;
import com.example.marketplace.model.MarketListing;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MarketBrowseGUI extends InventoryGUI {
    private final MarketPlace plugin;
    private final int page;
    private final MarketCategory category;

    public MarketBrowseGUI(MarketPlace plugin, int page) {
        this(plugin, page, MarketCategory.TOOLS);
    }

    public MarketBrowseGUI(MarketPlace plugin, int page, MarketCategory category) {
        this.plugin = plugin;
        this.page = page;
        this.category = category;
    }

    private GuiConfigManager guiConfig() {
        return plugin.getGuiConfigManager();
    }

    @Override
    protected Inventory createInventory() {
        String title = plugin.getMessageManager().getMessage("gui.title");
        return Bukkit.createInventory(null, guiConfig().getBrowseSize(), title);
    }

    @Override
    public void decorate(Player player) {
        fillBorder();
        fillCategories();

        List<MarketListing> listings = getFilteredListings();
        fillListings(player, listings);
        fillPagination(listings.size());
        super.decorate(player);
    }

    private List<MarketListing> getFilteredListings() {
        return plugin.getStorageManager().getAllListings().stream()
            .filter(category::matches)
            .collect(Collectors.toList());
    }

    private void fillBorder() {
        if (!guiConfig().isBrowseFillerEnabled()) {
            return;
        }

        for (int slot : guiConfig().getBrowseBorderSlots()) {
            addButton(slot, new InventoryButton()
                .creator(p -> createGlassPane())
                .consumer(event -> {})
            );
        }
    }

    private void fillCategories() {
        for (MarketCategory targetCategory : MarketCategory.values()) {
            GuiConfigManager.CategoryButtonConfig buttonConfig = guiConfig().getCategoryButton(targetCategory);
            addCategoryButton(
                buttonConfig.getSlot(),
                targetCategory,
                buttonConfig.getMaterial(),
                "gui.category." + targetCategory.name().toLowerCase()
            );
        }
    }

    private void addCategoryButton(int slot, MarketCategory targetCategory, Material material, String namePath) {
        addButton(slot, new InventoryButton()
            .creator(p -> createCategoryItem(material, namePath, targetCategory == category))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                clicker.closeInventory();
                plugin.getGuiManager().openGUI(new MarketBrowseGUI(plugin, 1, targetCategory), clicker);
            })
        );
    }

    private void fillListings(Player player, List<MarketListing> listings) {
        int itemsPerPage = guiConfig().getItemsPerPage();
        int[] listingSlots = guiConfig().getListingSlots();
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, listings.size());

        for (int i = startIndex; i < endIndex; i++) {
            MarketListing listing = listings.get(i);
            final int listingId = listing.getId();
            final boolean ownListing = listing.getSeller().equals(player.getUniqueId());
            int slot = listingSlots[i - startIndex];

            addButton(slot, new InventoryButton()
                .creator(p -> createListingItem(listing, player))
                .consumer(event -> {
                    if (ownListing) {
                        plugin.getMessageManager().sendMessage((Player) event.getWhoClicked(), "buy.own-listing");
                        return;
                    }

                    Player clicker = (Player) event.getWhoClicked();
                    clicker.closeInventory();
                    plugin.getGuiManager().openGUI(
                        new MarketConfirmGUI(plugin, listingId, page, category),
                        clicker
                    );
                })
            );
        }
    }

    private void fillPagination(int totalListings) {
        int itemsPerPage = guiConfig().getItemsPerPage();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalListings / itemsPerPage));

        addButton(guiConfig().getPaginationPrevious(), new InventoryButton()
            .creator(p -> createArrowItem(page > 1))
            .consumer(event -> {
                if (page <= 1) {
                    return;
                }
                Player clicker = (Player) event.getWhoClicked();
                clicker.closeInventory();
                plugin.getGuiManager().openGUI(new MarketBrowseGUI(plugin, page - 1, category), clicker);
            })
        );

        addButton(guiConfig().getPaginationPage(), new InventoryButton()
            .creator(p -> createPageIndicator(page, totalPages))
            .consumer(event -> {})
        );

        addButton(guiConfig().getPaginationNext(), new InventoryButton()
            .creator(p -> createArrowItem(page < totalPages))
            .consumer(event -> {
                if (page >= totalPages) {
                    return;
                }
                Player clicker = (Player) event.getWhoClicked();
                clicker.closeInventory();
                plugin.getGuiManager().openGUI(new MarketBrowseGUI(plugin, page + 1, category), clicker);
            })
        );
    }

    private ItemStack createGlassPane() {
        ItemStack item = new ItemStack(guiConfig().getBrowseFillerMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(guiConfig().getBrowseFillerName());
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createArrowItem(boolean active) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (!active) {
                meta.setDisplayName(ChatColor.DARK_GRAY + "—");
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createPageIndicator(int currentPage, int totalPages) {
        Map<String, String> replacements = plugin.getMessageManager().createReplacements();
        replacements.put("page", String.valueOf(currentPage));
        replacements.put("total", String.valueOf(totalPages));

        ItemStack item = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getMessageManager().getMessage("gui.page-indicator", replacements));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createCategoryItem(Material material, String namePath, boolean selected) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getMessageManager().getMessage(namePath));
            if (selected) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createListingItem(MarketListing listing, Player viewer) {
        ItemStack displayItem = plugin.getCustomItemsHook().recreateItem(listing);
        ItemMeta meta = displayItem.getItemMeta();

        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(displayItem.getType());
        }

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add("");

        Map<String, String> replacements = plugin.getMessageManager().createReplacements();
        replacements.put("price", plugin.getVaultHook().format(listing.getPrice()));
        replacements.put("seller", listing.getSellerName());
        replacements.put("id", String.valueOf(listing.getId()));

        lore.add(plugin.getMessageManager().getMessage("gui.lore.price", replacements));
        lore.add(plugin.getMessageManager().getMessage("gui.lore.seller", replacements));
        lore.add(plugin.getMessageManager().getMessage("gui.lore.id", replacements));
        lore.add("");

        if (listing.getSeller().equals(viewer.getUniqueId())) {
            lore.add(plugin.getMessageManager().getMessage("gui.lore.your-listing"));
        } else {
            lore.add(plugin.getMessageManager().getMessage("gui.lore.click-to-buy"));
        }

        meta.setLore(lore);
        displayItem.setItemMeta(meta);

        return displayItem;
    }
}
