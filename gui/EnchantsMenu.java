package org.axial.prisonsCore.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.axial.prisonsCore.model.CustomEnchant;
import org.axial.prisonsCore.model.EnchantRarity;
import org.axial.prisonsCore.service.CustomEnchantService;
import org.axial.prisonsCore.service.GearEnchantService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EnchantsMenu implements Listener {
    private static final int INVENTORY_SIZE = 54;
    private static final int[] ENCHANT_SLOTS = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

    private final CustomEnchantService enchantService;

    public EnchantsMenu(CustomEnchantService enchantService, GearEnchantService gearEnchantService) {
        this.enchantService = enchantService;
    }

    public void open(Player player) {
        Holder holder = new Holder();
        Inventory inventory = Bukkit.createInventory(holder, INVENTORY_SIZE, Text.color("&8Server Enchants"));
        holder.inventory = inventory;
        this.fillBackground(inventory);
        this.placeEnchants(inventory);
        player.openInventory(inventory);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof Holder)) {
            return;
        }
        event.setCancelled(true);
    }

    private void fillBackground(Inventory inventory) {
        ItemStack pane = this.namedItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack accent = this.namedItem(Material.MAGENTA_STAINED_GLASS_PANE, " ");
        for (int slot = 0; slot < INVENTORY_SIZE; ++slot) {
            inventory.setItem(slot, pane);
        }
        for (int slot : ENCHANT_SLOTS) {
            inventory.setItem(slot, null);
        }
        inventory.setItem(1, this.infoItem(Material.NETHER_STAR, "&dPickaxe Enchants", List.of("&7Only pickaxe enchants are shown here.", "&7Ordered by rarity, then name.")));
        inventory.setItem(7, this.infoItem(Material.BOOK, "&fRarity Order", List.of("&fBasic &7-> &3Unique/&bUncommon", "&7-> &aRare &7-> &eElite &7-> &6Legendary")));
        inventory.setItem(4, accent);
        inventory.setItem(49, accent);
    }

    private void placeEnchants(Inventory inventory) {
        List<Entry> entries = new ArrayList<Entry>();
        for (CustomEnchant enchant : this.enchantService.getAll()) {
            entries.add(new Entry(enchant));
        }
        if (entries.isEmpty()) {
            inventory.setItem(31, this.infoItem(Material.BARRIER, "&cNo Enchants", List.of("&7No enchants are currently", "&7loaded on the server.")));
            return;
        }
        entries.sort(Comparator.comparingInt((Entry entry) -> this.rarityOrder(entry.enchant.getRarity())).thenComparing(entry -> Text.strip(entry.enchant.getDisplayName()), String.CASE_INSENSITIVE_ORDER));
        int limit = Math.min(entries.size(), ENCHANT_SLOTS.length);
        for (int i = 0; i < limit; ++i) {
            Entry entry = entries.get(i);
            inventory.setItem(ENCHANT_SLOTS[i], this.enchantItem(entry.enchant));
        }
        if (entries.size() > ENCHANT_SLOTS.length) {
            inventory.setItem(45, this.infoItem(Material.PAPER, "&eMore Enchants", List.of("&7Only the first " + ENCHANT_SLOTS.length + " enchants", "&7fit in this menu.")));
        }
    }

    private ItemStack enchantItem(CustomEnchant enchant) {
        Material material = enchant.getRarity().getDisplayMaterial();
        String displayName = enchant.getRarity().getColorCode() + enchant.getDisplayName();
        ArrayList<String> lore = new ArrayList<String>();
        if (enchant.getDescription() != null && !enchant.getDescription().isEmpty()) {
            lore.add("&7" + enchant.getDescription());
            lore.add("");
        }
        lore.add("&7Rarity: " + enchant.getRarity().getColorCode() + enchant.getRarity().name());
        lore.add("&7Max Level: &f" + this.roman(enchant.getMaxLevel()) + " &7(" + enchant.getMaxLevel() + ")");
        return this.infoItem(material, displayName, lore);
    }

    private int rarityOrder(EnchantRarity rarity) {
        if (rarity == null) {
            return Integer.MAX_VALUE;
        }
        return switch (rarity) {
            case BASIC -> 0;
            case UNIQUE, UNCOMMON -> 1;
            case RARE -> 2;
            case ELITE -> 3;
            case LEGENDARY -> 4;
        };
    }

    private ItemStack infoItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Text.componentNoItalics(name));
            ArrayList<Component> components = new ArrayList<Component>();
            for (String line : lore) {
                components.add(Text.componentNoItalics(line));
            }
            meta.lore(components);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack namedItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Text.componentNoItalics(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    private String roman(int number) {
        int n = Math.max(1, number);
        int[] values = new int[]{1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] numerals = new String[]{"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; ++i) {
            while (n >= values[i]) {
                sb.append(numerals[i]);
                n -= values[i];
            }
        }
        return sb.toString();
    }

    private static class Holder implements InventoryHolder {
        private Inventory inventory;

        @Override
        public Inventory getInventory() {
            return this.inventory;
        }
    }

    private record Entry(CustomEnchant enchant) {
    }
}
