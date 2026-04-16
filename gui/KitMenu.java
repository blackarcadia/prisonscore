package org.axial.prisonsCore.gui;

import java.util.List;
import org.axial.prisonsCore.service.KitService;
import org.axial.prisonsCore.service.PlayerLevelService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.stream.Collectors;

public class KitMenu implements Listener {
    private static final int ROOT_SIZE = 27;
    private static final int PREVIEW_SIZE = 27;
    private static final int STARTER_SLOT = 11;
    private static final int BETA_SLOT = 15;
    private static final int BACK_SLOT = 22;
    private static final int INFO_SLOT = 4;
    private static final int[] PREVIEW_SLOTS = new int[]{10, 11, 12, 13, 14, 15, 16};
    private final KitService kitService;
    private final PlayerLevelService playerLevelService;

    public KitMenu(KitService kitService, PlayerLevelService playerLevelService) {
        this.kitService = kitService;
        this.playerLevelService = playerLevelService;
    }

    public void open(Player player) {
        RootHolder holder = new RootHolder();
        Inventory inventory = Bukkit.createInventory(holder, ROOT_SIZE, Text.color("&8Kits"));
        holder.inventory = inventory;
        this.fillBackground(inventory);
        inventory.setItem(INFO_SLOT, this.infoItem(player));
        inventory.setItem(STARTER_SLOT, this.kitItem(player, KitService.KitType.STARTER));
        inventory.setItem(BETA_SLOT, this.kitItem(player, KitService.KitType.BETA));
        player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.0f);
        player.openInventory(inventory);
    }

    private void openPreview(Player player, KitService.KitType kitType) {
        PreviewHolder holder = new PreviewHolder(kitType);
        Inventory inventory = Bukkit.createInventory(holder, PREVIEW_SIZE, Text.color("&8" + kitType.plainName() + " Preview"));
        holder.inventory = inventory;
        this.fillBackground(inventory);
        inventory.setItem(INFO_SLOT, this.previewInfoItem(player, kitType));
        inventory.setItem(BACK_SLOT, this.namedItem(Material.NETHER_STAR, "&c&lBack", List.of("&7Return to the kit menu.")));
        List<ItemStack> preview = this.kitService.previewItems(player, kitType);
        for (int i = 0; i < preview.size() && i < PREVIEW_SLOTS.length; ++i) {
            inventory.setItem(PREVIEW_SLOTS[i], preview.get(i));
        }
        player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.0f);
        player.openInventory(inventory);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof InventoryHolder holder)) {
            return;
        }
        if (!(holder instanceof RootHolder) && !(holder instanceof PreviewHolder)) {
            return;
        }
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getView().getTopInventory())) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (holder instanceof RootHolder) {
            this.handleRootClick(player, event.getRawSlot(), event.getClick());
            return;
        }
        if (holder instanceof PreviewHolder previewHolder) {
            this.handlePreviewClick(player, previewHolder, event.getRawSlot());
        }
    }

    private void handleRootClick(Player player, int slot, ClickType clickType) {
        KitService.KitType kitType = null;
        if (slot == STARTER_SLOT) {
            kitType = KitService.KitType.STARTER;
        } else if (slot == BETA_SLOT) {
            kitType = KitService.KitType.BETA;
        }
        if (kitType == null) {
            return;
        }
        if (clickType.isRightClick()) {
            this.openPreview(player, kitType);
            return;
        }
        if (!clickType.isLeftClick()) {
            return;
        }
        KitService.ClaimResult result = this.kitService.claim(player, kitType);
        switch (result) {
            case CLAIMED -> this.open(player);
            case NO_PERMISSION -> {
                player.sendMessage(Text.color("&cYou do not have access to that kit."));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            }
            case ON_COOLDOWN -> {
                player.sendMessage(Text.color("&eThat kit is on cooldown for &f" + this.kitService.formatCooldown(this.kitService.getRemainingCooldown(player, kitType)) + "&e."));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            }
        }
    }

    private void handlePreviewClick(Player player, PreviewHolder holder, int slot) {
        if (slot == BACK_SLOT) {
            this.open(player);
            return;
        }
        if (slot == INFO_SLOT) {
            this.open(player);
        }
    }

    private ItemStack infoItem(Player player) {
        return this.namedItem(Material.BOOK, "&f&lKit Information", List.of(
                Text.color("&7Mining Level: &f" + this.playerLevelService.getLevel(player)),
                Text.color("&7Reward Bracket: &f" + this.kitService.bracketDescription(player)),
                Text.color("&7Left-click to claim a kit."),
                Text.color("&7Right-click to preview rewards.")
        ));
    }

    private ItemStack previewInfoItem(Player player, KitService.KitType kitType) {
        return this.namedItem(Material.BOOK, "&f&l" + kitType.plainName() + " Preview", List.of(
                Text.color("&7Mining Level: &f" + this.playerLevelService.getLevel(player)),
                Text.color("&7Reward Bracket: &f" + this.kitService.bracketDescription(player)),
                Text.color("&7These are the rewards for"),
                Text.color("&7your current mining level.")
        ));
    }

    private ItemStack kitItem(Player player, KitService.KitType kitType) {
        if (!this.kitService.hasAccess(player, kitType)) {
            return this.namedPane(Material.RED_STAINED_GLASS_PANE, "&c&l" + kitType.menuLabel(), List.of(
                    Text.color("&7Unlock access at &c&Nstore.axialprisons.com&7."),
                    Text.color("&7Right-click to preview rewards.")
            ));
        }
        long remaining = this.kitService.getRemainingCooldown(player, kitType);
        if (remaining > 0L) {
            return this.namedPane(Material.YELLOW_STAINED_GLASS_PANE, "&e&l" + kitType.menuLabel(), List.of(
                    Text.color("&7Next available in: &e" + this.kitService.formatCooldown(remaining)),
                    Text.color("&7Right-click to preview rewards.")
            ));
        }
        return this.namedPane(this.availableMaterial(kitType), "&a&l" + kitType.menuLabel(), List.of(
                Text.color("&7Ready to claim."),
                Text.color("&7Left-click to claim this kit."),
                Text.color("&7Right-click to preview rewards.")
        ));
    }

    private Material availableMaterial(KitService.KitType kitType) {
        return switch (kitType) {
            case STARTER -> Material.CHEST;
            case BETA -> Material.ENDER_CHEST;
        };
    }

    private void fillBackground(Inventory inventory) {
        ItemStack filler = this.namedPane(Material.GRAY_STAINED_GLASS_PANE, "&7", null);
        for (int slot = 0; slot < inventory.getSize(); ++slot) {
            inventory.setItem(slot, filler);
        }
    }

    private ItemStack namedPane(Material material, String name, List<String> lore) {
        return this.namedItem(material, name, lore);
    }

    private ItemStack namedItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Text.color(name));
        if (lore != null && !lore.isEmpty()) {
            meta.setLore(lore.stream().map(Text::color).collect(Collectors.toList()));
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private static class RootHolder implements InventoryHolder {
        private Inventory inventory;

        @Override
        public Inventory getInventory() {
            return this.inventory;
        }
    }

    private static class PreviewHolder implements InventoryHolder {
        private final KitService.KitType kitType;
        private Inventory inventory;

        private PreviewHolder(KitService.KitType kitType) {
            this.kitType = kitType;
        }

        @Override
        public Inventory getInventory() {
            return this.inventory;
        }
    }
}
