package com.example.marketplace.inventory.impl;

import com.example.marketplace.MarketPlace;
import com.example.marketplace.inventory.InventoryButton;
import com.example.marketplace.inventory.InventoryGUI;
import com.example.marketplace.inventory.MarketCategory;
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
    private static final int INVENTORY_SIZE = 54;
    private static final int ITEMS_PER_PAGE = 20;

    private static final int[] BORDER_SLOTS = {
        0, 1, 2, 3, 4, 5, 6, 7, 8,
        9, 11, 17,
        18, 20, 26,
        27, 29, 35,
        36, 38, 44,
        45, 46, 47, 48, 52, 53
    };

    private static final int[] CONTENT_SLOTS = {
        12, 13, 14, 15, 16,
        21, 22, 23, 24, 25,
        30, 31, 32, 33, 34,
        39, 40, 41, 42, 43
    };

    private static final int SLOT_TOOLS = 10;
    private static final int SLOT_BLOCKS = 19;
    private static final int SLOT_COMBAT = 28;
    private static final int SLOT_OTHER = 37;
    private static final int SLOT_PREVIOUS = 49;
    private static final int SLOT_PAGE = 50;
    private static final int SLOT_NEXT = 51;

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

    @Override
    protected Inventory createInventory() {
        String title = plugin.getMessageManager().getMessage("gui.title");
        return Bukkit.createInventory(null, INVENTORY_SIZE, title);
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
        for (int slot : BORDER_SLOTS) {
            addButton(slot, new InventoryButton()
                .creator(p -> createGlassPane())
                .consumer(event -> {})
            );
        }
    }

    private void fillCategories() {
        addCategoryButton(SLOT_TOOLS, MarketCategory.TOOLS, Material.DIAMOND_PICKAXE, "gui.category.tools");
        addCategoryButton(SLOT_BLOCKS, MarketCategory.BLOCKS, Material.GRASS_BLOCK, "gui.category.blocks");
        addCategoryButton(SLOT_COMBAT, MarketCategory.COMBAT, Material.DIAMOND_SWORD, "gui.category.combat");
        addCategoryButton(SLOT_OTHER, MarketCategory.OTHER, Material.GUNPOWDER, "gui.category.other");
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
        int startIndex = (page - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, listings.size());

        for (int i = startIndex; i < endIndex; i++) {
            MarketListing listing = listings.get(i);
            final int listingId = listing.getId();
            final boolean ownListing = listing.getSeller().equals(player.getUniqueId());
            int slot = CONTENT_SLOTS[i - startIndex];

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
        int totalPages = Math.max(1, (int) Math.ceil((double) totalListings / ITEMS_PER_PAGE));

        addButton(SLOT_PREVIOUS, new InventoryButton()
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

        addButton(SLOT_PAGE, new InventoryButton()
            .creator(p -> createPageIndicator(page, totalPages))
            .consumer(event -> {})
        );

        addButton(SLOT_NEXT, new InventoryButton()
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
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
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
