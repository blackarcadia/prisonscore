package org.axial.prisonsCore.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.service.PlayerToggleService;
import org.axial.prisonsCore.service.PlayerToggleService.Category;
import org.axial.prisonsCore.service.PlayerToggleService.Toggle;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class ToggleMenu implements Listener {
    private static final int SIZE = 27;
    private static final int CATEGORY_SIZE = 36;
    private static final int ROOT_NOTIFICATIONS = 10;
    private static final int ROOT_CONFIRMATIONS = 12;
    private static final int ROOT_REQUESTS = 14;
    private static final int ROOT_OTHER = 16;
    private static final int BACK_SLOT = 31;
    private static final long TOGGLE_CLICK_COOLDOWN_MS = 500L;
    private static final int[] TOGGLE_SLOTS = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22};

    private final PrisonsCore plugin;
    private final PlayerToggleService toggleService;
    private final Map<UUID, Long> lastToggleClickAt = new HashMap<UUID, Long>();

    public ToggleMenu(PrisonsCore plugin, PlayerToggleService toggleService) {
        this.plugin = plugin;
        this.toggleService = toggleService;
    }

    public void open(Player player) {
        this.openRoot(player);
    }

    public void openRoot(Player player) {
        if (player == null) {
            return;
        }
        Holder holder = new Holder(player.getUniqueId(), null);
        Inventory inventory = Bukkit.createInventory(holder, SIZE, Text.color("&8Toggle Menu"));
        holder.inventory = inventory;
        this.renderRoot(player, inventory);
        player.openInventory(inventory);
    }

    public void openCategory(Player player, Category category) {
        if (player == null || category == null) {
            return;
        }
        Holder holder = new Holder(player.getUniqueId(), category);
        Inventory inventory = Bukkit.createInventory(holder, CATEGORY_SIZE, Text.color("&8" + this.prettyCategoryTitle(category)));
        holder.inventory = inventory;
        this.renderCategory(player, inventory, category);
        player.openInventory(inventory);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof Holder holder)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || !player.getUniqueId().equals(holder.owner)) {
            return;
        }
        int slot = event.getRawSlot();
        int limit = holder.category == null ? SIZE : CATEGORY_SIZE;
        if (slot < 0 || slot >= limit) {
            return;
        }
        if (holder.category == null) {
            this.handleRootClick(player, slot);
            return;
        }
        this.handleCategoryClick(player, holder.category, slot);
    }

    private void handleRootClick(Player player, int slot) {
        Category category = switch (slot) {
            case ROOT_NOTIFICATIONS -> Category.NOTIFICATIONS;
            case ROOT_CONFIRMATIONS -> Category.CONFIRMATIONS;
            case ROOT_REQUESTS -> Category.REQUESTS;
            case ROOT_OTHER -> Category.OTHER;
            default -> null;
        };
        if (category == null) {
            return;
        }
        Bukkit.getScheduler().runTask(this.plugin, () -> this.openCategory(player, category));
    }

    private void handleCategoryClick(Player player, Category category, int slot) {
        if (slot == BACK_SLOT) {
            Bukkit.getScheduler().runTask(this.plugin, () -> this.openRoot(player));
            return;
        }
        Toggle toggle = this.toggleAt(category, slot);
        if (toggle == null) {
            return;
        }
        long now = System.currentTimeMillis();
        long lastClick = this.lastToggleClickAt.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastClick < TOGGLE_CLICK_COOLDOWN_MS) {
            return;
        }
        this.lastToggleClickAt.put(player.getUniqueId(), now);
        this.toggleService.toggle(player, toggle);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        Bukkit.getScheduler().runTask(this.plugin, () -> this.openCategory(player, category));
    }

    private Toggle toggleAt(Category category, int slot) {
        List<Toggle> toggles = this.toggleService.togglesFor(category);
        for (int i = 0; i < TOGGLE_SLOTS.length && i < toggles.size(); ++i) {
            if (TOGGLE_SLOTS[i] == slot) {
                return toggles.get(i);
            }
        }
        return null;
    }

    private void renderRoot(Player player, Inventory inventory) {
        this.fill(inventory, Material.BLACK_STAINED_GLASS_PANE);
        inventory.setItem(ROOT_NOTIFICATIONS, this.categoryItem(
                Material.GREEN_STAINED_GLASS_PANE,
                "&2&lNotification Toggles",
                this.buildCategoryLore(
                        "&7Click to view all notification related toggles",
                        "&2&lToggles",
                        Category.NOTIFICATIONS,
                        player
                )));
        inventory.setItem(ROOT_CONFIRMATIONS, this.categoryItem(
                Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                "&b&lConfirmation Toggles",
                this.buildCategoryLore(
                        "&7Click to view all confirmation related toggles",
                        "&b&lToggles",
                        Category.CONFIRMATIONS,
                        player
                )));
        inventory.setItem(ROOT_REQUESTS, this.categoryItem(
                Material.YELLOW_STAINED_GLASS_PANE,
                "&e&lRequest Toggles",
                this.buildCategoryLore(
                        "&7Click to view all request related toggles",
                        "&e&lToggles",
                        Category.REQUESTS,
                        player
                )));
        inventory.setItem(ROOT_OTHER, this.categoryItem(
                Material.RED_STAINED_GLASS_PANE,
                "&c&lOther Toggles",
                this.buildCategoryLore(
                        "&7Click to view all other toggles",
                        "&c&lToggles",
                        Category.OTHER,
                        player
                )));
    }

    private void renderCategory(Player player, Inventory inventory, Category category) {
        this.fill(inventory, Material.GRAY_STAINED_GLASS_PANE);
        List<Toggle> toggles = this.toggleService.togglesFor(category);
        for (int i = 0; i < TOGGLE_SLOTS.length && i < toggles.size(); ++i) {
            Toggle toggle = toggles.get(i);
            boolean enabled = this.toggleService.isEnabled(player, toggle);
            inventory.setItem(TOGGLE_SLOTS[i], this.toggleItem(toggle, enabled));
        }
        inventory.setItem(BACK_SLOT, this.categoryItem(Material.NETHER_STAR, "&c&lBack", List.of("&7Return to categories.")));
    }

    private ItemStack categoryItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Text.componentNoItalics(name));
            if (lore != null && !lore.isEmpty()) {
                meta.lore(lore.stream().map(Text::componentNoItalics).toList());
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack toggleItem(Toggle toggle, boolean enabled) {
        ItemStack item = new ItemStack(enabled ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Text.componentNoItalics((enabled ? "&a&l" : "&c&l") + toggle.displayName()));
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(enabled ? "&7Status: &aEnabled" : "&7Status: &cDisabled");
            lore.add("&7Click to " + (enabled ? "disable" : "enable") + ".");
            for (String line : toggle.lore()) {
                lore.add("&7" + line);
            }
            meta.lore(lore.stream().map(Text::componentNoItalics).toList());
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    private List<String> buildCategoryLore(String introLine, String headerLine, Category category, Player player) {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(introLine);
        lore.add(headerLine);
        for (Toggle toggle : this.toggleService.togglesFor(category)) {
            lore.add(this.toggleSummaryLine(category, toggle, player));
        }
        return lore;
    }

    private String toggleSummaryLine(Category category, Toggle toggle, Player player) {
        boolean enabled = this.toggleService.isEnabled(player, toggle);
        return this.categoryPrefix(category) + "&l* &f" + toggle.displayName() + " " + (enabled ? "&a&lENABLED" : "&c&lDISABLED");
    }

    private String categoryPrefix(Category category) {
        return switch (category) {
            case NOTIFICATIONS -> "&2";
            case CONFIRMATIONS -> "&b";
            case REQUESTS -> "&e";
            case OTHER -> "&c";
        };
    }

    private void fill(Inventory inventory, Material material) {
        ItemStack filler = this.categoryItem(material, " ", List.of());
        for (int i = 0; i < inventory.getSize(); ++i) {
            inventory.setItem(i, filler);
        }
    }

    private String prettyCategoryTitle(Category category) {
        return switch (category) {
            case NOTIFICATIONS -> "Notifications";
            case CONFIRMATIONS -> "Confirmations";
            case REQUESTS -> "Requests";
            case OTHER -> "Other";
        };
    }

    private static final class Holder implements InventoryHolder {
        private final UUID owner;
        private final Category category;
        private Inventory inventory;

        private Holder(UUID owner, Category category) {
            this.owner = owner;
            this.category = category;
        }

        @Override
        public Inventory getInventory() {
            return this.inventory;
        }
    }
}
