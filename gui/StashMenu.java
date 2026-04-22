package org.axial.prisonsCore.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.service.StashService;
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

public final class StashMenu implements Listener {
    private static final int SIZE = 54;
    private static final int ITEM_END = 44;
    private static final int PAGE_SIZE = ITEM_END + 1;
    private static final int PREV_SLOT = 45;
    private static final int CLAIM_ALL_SLOT = 49;
    private static final int CLOSE_SLOT = 51;
    private static final int NEXT_SLOT = 53;

    private final PrisonsCore plugin;
    private final StashService stashService;

    public StashMenu(PrisonsCore plugin, StashService stashService) {
        this.plugin = plugin;
        this.stashService = stashService;
    }

    public void open(Player player) {
        this.open(player, 0);
    }

    private void open(Player player, int page) {
        if (player == null) {
            return;
        }
        int clampedPage = Math.max(0, Math.min(page, this.maxPage(player)));
        Holder holder = new Holder(player.getUniqueId(), clampedPage);
        Inventory inventory = Bukkit.createInventory(holder, SIZE, this.title(player, clampedPage));
        holder.inventory = inventory;
        this.render(player, inventory, holder.page);
        player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.0f);
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
        if (slot == PREV_SLOT) {
            holder.page = Math.max(0, holder.page - 1);
            this.refresh(player, holder.page);
            return;
        }
        if (slot == NEXT_SLOT) {
            int maxPage = this.maxPage(player);
            holder.page = Math.min(maxPage, holder.page + 1);
            this.refresh(player, holder.page);
            return;
        }
        if (slot == CLAIM_ALL_SLOT) {
            if (this.stashService.claimAll(player)) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                player.sendMessage(Text.color("&aClaimed all stash items."));
                this.refresh(player, holder.page);
            } else {
                player.sendMessage(Text.color("&cYou need more inventory space to claim all stash items."));
            }
            return;
        }
        if (slot == CLOSE_SLOT) {
            player.closeInventory();
            return;
        }
        if (slot < 0 || slot > ITEM_END) {
            return;
        }
        int index = holder.page * PAGE_SIZE + slot;
        ItemStack item = this.stashService.getItem(player.getUniqueId(), index);
        if (item == null) {
            return;
        }
        if (!this.stashService.claimItem(player, index)) {
            player.sendMessage(Text.color("&cYou need more inventory space for that item."));
            return;
        }
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.2f);
        player.sendMessage(Text.color("&aClaimed &f" + item.getAmount() + "x " + this.prettyName(item) + "&a from your stash."));
        this.refresh(player, holder.page);
    }

    private void refresh(Player player, int page) {
        Inventory top = player.getOpenInventory().getTopInventory();
        if (!(top.getHolder() instanceof Holder)) {
            return;
        }
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            if (player.isOnline() && player.getOpenInventory().getTopInventory().getHolder() instanceof Holder) {
                this.open(player, page);
            }
        });
    }

    private void render(Player player, Inventory inventory, int page) {
        this.fillBackground(inventory);
        List<ItemStack> stash = this.stashService.getItems(player.getUniqueId());
        if (stash.isEmpty()) {
            inventory.setItem(22, this.button(Material.BARRIER, "&c&lNo Stashed Items", List.of("&7Items you overflowed", "&7will appear here.")));
            inventory.setItem(PREV_SLOT, this.button(Material.GRAY_DYE, "&7Previous Page", List.of("&7No pages.")));
            inventory.setItem(NEXT_SLOT, this.button(Material.GRAY_DYE, "&7Next Page", List.of("&7No pages.")));
        } else {
            int start = page * PAGE_SIZE;
            int end = Math.min(stash.size(), start + PAGE_SIZE);
            for (int i = start; i < end; ++i) {
                inventory.setItem(i - start, this.displayItem(stash.get(i)));
            }
            inventory.setItem(PREV_SLOT, this.button(page > 0 ? Material.ARROW : Material.GRAY_DYE, "&ePrevious Page", List.of("&7Go to the previous page.")));
            inventory.setItem(NEXT_SLOT, this.button(page < this.maxPage(player) ? Material.ARROW : Material.GRAY_DYE, "&eNext Page", List.of("&7Go to the next page.")));
        }
        inventory.setItem(47, this.button(Material.PAPER, "&fPage &7" + (page + 1) + "&8/&7" + (this.maxPage(player) + 1), List.of("&7Stashed items: &f" + stash.size())));
        inventory.setItem(CLAIM_ALL_SLOT, this.button(Material.LIME_DYE, "&a&lClaim All", List.of("&7Claim every item", "&7if you have room.")));
        inventory.setItem(CLOSE_SLOT, this.button(Material.BARRIER, "&c&lClose", List.of("&7Close this menu.")));
    }

    private ItemStack displayItem(ItemStack stack) {
        ItemStack copy = stack.clone();
        ItemMeta meta = copy.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add("");
            lore.add(Text.color("&7Click to claim."));
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            copy.setItemMeta(meta);
        }
        return copy;
    }

    private ItemStack button(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Text.color(name));
        meta.setLore(lore.stream().map(Text::color).toList());
        item.setItemMeta(meta);
        return item;
    }

    private void fillBackground(Inventory inventory) {
        ItemStack filler = this.button(Material.BLACK_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 0; i < inventory.getSize(); ++i) {
            inventory.setItem(i, filler);
        }
    }

    private int maxPage(Player player) {
        int count = this.stashService.getItemCount(player.getUniqueId());
        if (count <= 0) {
            return 0;
        }
        return (count - 1) / PAGE_SIZE;
    }

    private String title(Player player, int page) {
        return Text.color("&8Player Stash &7(" + this.stashService.getItemCount(player.getUniqueId()) + ") &8Page &7" + (page + 1) + "&8/&7" + (this.maxPage(player) + 1));
    }

    private String prettyName(ItemStack item) {
        if (item == null) {
            return "Item";
        }
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return Text.strip(item.getItemMeta().getDisplayName());
        }
        return item.getType().name().toLowerCase().replace('_', ' ');
    }

    private static final class Holder implements InventoryHolder {
        private final UUID owner;
        private int page;
        private Inventory inventory;

        private Holder(UUID owner, int page) {
            this.owner = owner;
            this.page = page;
        }

        @Override
        public Inventory getInventory() {
            return this.inventory;
        }
    }
}
