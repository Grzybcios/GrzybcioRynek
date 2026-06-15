package com.example.marketplace.inventory;

import com.example.marketplace.model.MarketListing;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.Set;

public enum MarketCategory {
    TOOLS,
    BLOCKS,
    COMBAT,
    OTHER;

    private static final Set<Enchantment> COMBAT_ENCHANTMENTS = new HashSet<>();

    static {
        registerCombatEnchantments(
            "sharpness",
            "smite",
            "bane_of_arthropods",
            "knockback",
            "fire_aspect",
            "looting",
            "sweeping",
            "impaling",
            "loyalty",
            "riptide",
            "channeling",
            "density",
            "breach",
            "wind_burst",
            "cleaving",
            "lunge",
            "power",
            "punch",
            "flame",
            "infinity",
            "quick_charge",
            "multishot",
            "piercing",
            "protection",
            "fire_protection",
            "blast_protection",
            "projectile_protection",
            "thorns",
            "binding_curse",
            "respiration",
            "aqua_affinity",
            "depth_strider",
            "frost_walker",
            "feather_falling",
            "soul_speed",
            "swift_sneak"
        );
    }

    public boolean matches(MarketListing listing) {
        ItemStack item = listing.getItem();
        if (item == null) {
            return this == OTHER;
        }

        Material material = item.getType();

        switch (this) {
            case TOOLS:
                return isTool(material);
            case BLOCKS:
                return isBlock(material) && !isTool(material);
            case COMBAT:
                if (isTool(material)) {
                    return false;
                }
                return isCombatMaterial(material) || hasCombatEnchantment(item);
            case OTHER:
                return !isTool(material)
                    && !isBlock(material)
                    && !isCombatMaterial(material)
                    && !hasCombatEnchantment(item);
            default:
                return false;
        }
    }

    private static void registerCombatEnchantments(String... keys) {
        for (String key : keys) {
            Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(key));
            if (enchantment != null) {
                COMBAT_ENCHANTMENTS.add(enchantment);
            }
        }
    }

    private boolean isTool(Material material) {
        String name = material.name();
        return name.endsWith("_PICKAXE")
            || name.endsWith("_AXE")
            || name.endsWith("_SHOVEL")
            || name.endsWith("_HOE")
            || material == Material.FISHING_ROD
            || material == Material.SHEARS
            || material == Material.FLINT_AND_STEEL
            || material == Material.BRUSH
            || material == Material.CARROT_ON_A_STICK
            || material == Material.WARPED_FUNGUS_ON_A_STICK;
    }

    private boolean isBlock(Material material) {
        return material.isBlock();
    }

    private boolean isCombatMaterial(Material material) {
        String name = material.name();

        if (name.endsWith("_SWORD") || name.endsWith("_SPEAR")) {
            return true;
        }

        if (name.endsWith("_HELMET")
            || name.endsWith("_CHESTPLATE")
            || name.endsWith("_LEGGINGS")
            || name.endsWith("_BOOTS")
            || name.endsWith("_HORSE_ARMOR")) {
            return true;
        }

        if (name.endsWith("_HEAD") || name.endsWith("_SKULL")) {
            return true;
        }

        if (name.equals("MACE") || name.equals("WIND_CHARGE")) {
            return true;
        }

        switch (material) {
            case TRIDENT:
            case BOW:
            case CROSSBOW:
            case SHIELD:
            case ARROW:
            case SPECTRAL_ARROW:
            case TIPPED_ARROW:
            case FIREWORK_ROCKET:
            case SNOWBALL:
            case EGG:
            case ENDER_PEARL:
            case TOTEM_OF_UNDYING:
            case EXPERIENCE_BOTTLE:
            case TURTLE_HELMET:
            case CARVED_PUMPKIN:
            case ELYTRA:
            case TNT:
            case END_CRYSTAL:
            case FIRE_CHARGE:
            case SPLASH_POTION:
            case LINGERING_POTION:
                return true;
            default:
                return name.contains("POTION") && (name.contains("SPLASH") || name.contains("LINGERING"));
        }
    }

    private boolean hasCombatEnchantment(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        for (Enchantment enchantment : meta.getEnchants().keySet()) {
            if (COMBAT_ENCHANTMENTS.contains(enchantment)) {
                return true;
            }
        }

        if (meta instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) meta;
            for (Enchantment enchantment : bookMeta.getStoredEnchants().keySet()) {
                if (COMBAT_ENCHANTMENTS.contains(enchantment)) {
                    return true;
                }
            }
        }

        return false;
    }
}
