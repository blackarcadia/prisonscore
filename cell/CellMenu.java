package org.axial.prisonsCore.cell;

import org.axial.prisonsCore.Keys;
import org.axial.prisonsCore.service.EconomyService;
import org.axial.prisonsCore.util.Lang;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

public class CellMenu implements Listener {
    private final org.axial.prisonsCore.PrisonsCore plugin;
    private final CellService cellService;
    private final EconomyService economyService;

    public CellMenu(org.axial.prisonsCore.PrisonsCore plugin, CellService cellService, EconomyService economyService) {
        this.plugin = plugin;
        this.cellService = cellService;
        this.economyService = economyService;
    }

    public void open(Player player) {
        openTierSelection(player);
    }

    private void openTierSelection(Player player) {
        TierSession holder = new TierSession(player.getUniqueId());
        String title = msg("cells.menu.select-title", "&8Select Cell Tier", Map.of());
        Inventory inv = Bukkit.createInventory(holder, 27, title);
        holder.inv = inv;

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler());
        }

        int[] slots = new int[]{11, 13, 15};
        CellTier[] tiers = CellTier.values();
        for (int i = 0; i < tiers.length && i < slots.length; i++) {
            ItemStack item = buildTierItem(tiers[i], player);
            inv.setItem(slots[i], item);
            holder.tiersBySlot[slots[i]] = tiers[i];
        }

        player.openInventory(inv);
    }

    private void openTier(Player player, CellTier tier) {
        List<Cell> sorted = new ArrayList<>(cellService.getCells());
        sorted.removeIf(cell -> cell.getTier() != tier);
        sorted.sort(Comparator.comparing(Cell::getId));

        int size = Math.min(54, Math.max(9, ((sorted.size() - 1) / 9 + 1) * 9));
        Session holder = new Session(player.getUniqueId(), tier);
        String title = msg("cells.menu.tier-title", "&8{tier} Cells", Map.of("tier", tier.name()));
        Inventory inv = Bukkit.createInventory(holder, size, title);
        holder.inv = inv;

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler());
        }
        int slot = 0;
        for (Cell cell : sorted) {
            ItemStack item = buildItem(player, cell);
            inv.setItem(slot, item);
            holder.cellsBySlot[slot] = cell.getId();
            slot++;
            if (slot >= inv.getSize()) break;
        }
        player.openInventory(inv);
    }

    private ItemStack filler() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(" ");
        pane.setItemMeta(meta);
        return pane;
    }

    private ItemStack buildTierItem(CellTier tier, Player viewer) {
        Material mat = switch (tier) {
            case INMATE -> Material.IRON_BLOCK;
            case TRUSTEE -> Material.GOLD_BLOCK;
            case WARDEN -> Material.DIAMOND_BLOCK;
        };
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        Map<String, String> ph = Map.of(
                "tier", tier.name(),
                "price", economyService.format(cellService.getPrice(tier, viewer)),
                "required", String.valueOf(tier.getRequiredMiningLevel())
        );
        String namePath = "cells.menu.tier-item.names." + tier.name().toLowerCase(Locale.ROOT);
        meta.setDisplayName(msg(namePath, "&e{tier} Cells", ph));
        List<String> lore = msgList("cells.menu.tier-item.lore", List.of(
                "&7Weekly Price: &f{price}",
                "&7Requirement: &fMining {required}",
                "&aClick to view this tier."
        ), ph);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildItem(Player viewer, Cell cell) {
        boolean owned = cell.isOwned();
        boolean ownedByViewer = owned && viewer.getUniqueId().equals(cell.getOwner());
        Material mat = owned ? (ownedByViewer ? Material.LIME_CONCRETE : Material.RED_CONCRETE) : Material.CYAN_CONCRETE;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Keys.cellMenuId(plugin), PersistentDataType.STRING, cell.getId());
        Map<String, String> ph = new java.util.HashMap<>();
        ph.put("id", cell.getId());
        ph.put("tier", cell.getTier().name());
        ph.put("price", economyService.format(cellService.getPrice(cell.getTier(), viewer)));
        ph.put("required", String.valueOf(cell.getTier().getRequiredMiningLevel()));
        ph.put("owner", cell.ownerName());
        ph.put("expires", cellService.expiryString(cell));
        meta.setDisplayName(msg("cells.menu.list-item.name", "&eCell {id} &7({tier})", ph));
        List<String> lore = new ArrayList<>(msgList("cells.menu.list-item.base-lore", List.of(
                "&7Tier: &f{tier}",
                "&7Price: &f{price} &7/ 7d",
                "&7Requirement: &fMining {required}"
        ), ph));
        if (owned) {
            lore.add(msg("cells.menu.list-item.owner", "&7Owner: &f{owner}", ph));
            lore.add(msg("cells.menu.list-item.expires", "&7Expires: &f{expires}", ph));
            lore.add(msg(ownedByViewer ? "cells.menu.list-item.renew" : "cells.menu.list-item.owned",
                    ownedByViewer ? "&aClick to renew for another 7 days." : "&cAlready owned.", ph));
        } else {
            lore.add(msg("cells.menu.list-item.available", "&aAvailable. Click to rent.", ph));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof TierSession tierSession) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot < 0 || slot >= tierSession.tiersBySlot.length) return;
            CellTier tier = tierSession.tiersBySlot[slot];
            if (tier == null) return;
            Player player = (Player) event.getWhoClicked();
            openTier(player, tier);
            return;
        }

        if (!(event.getInventory().getHolder() instanceof Session session)) return;
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= session.cellsBySlot.length) return;
        String id = session.cellsBySlot[slot];
        if (id == null) return;
        Cell cell = cellService.getCell(id);
        if (cell == null) return;
        Player player = (Player) event.getWhoClicked();
        if (event.isRightClick() && cell.isOwned() && cell.getOwner() != null && cell.getOwner().equals(player.getUniqueId())) {
            if (cellService.teleportToCell(player, cell)) {
                player.closeInventory();
                player.sendMessage(Text.color("&aTeleporting to your cell &f" + cell.getId() + "&a..."));
            } else {
                player.sendMessage(Text.color("&cCould not find a teleport location for that cell."));
            }
            return;
        }
        if (!cell.isOwned()) {
            if (cellService.purchaseCell(player, cell)) {
                reopen(player, session.tier);
            }
            return;
        }
        if (cell.getOwner() != null && cell.getOwner().equals(player.getUniqueId())) {
            if (cellService.renewCell(player, cell)) {
                reopen(player, session.tier);
            }
        } else {
            player.sendMessage(Text.color("&cThat cell is already owned by " + cell.ownerName() + "."));
        }
    }

    private void reopen(Player player, CellTier tier) {
        Bukkit.getScheduler().runTask(plugin, () -> openTier(player, tier));
    }

    private String msg(String path, String def, Map<String, String> placeholders) {
        return replacePlaceholders(Lang.msg(path, def), placeholders);
    }

    private List<String> msgList(String path, List<String> def, Map<String, String> placeholders) {
        return Lang.list(path, def).stream()
                .map(line -> replacePlaceholders(line, placeholders))
                .collect(Collectors.toList());
    }

    private String replacePlaceholders(String input, Map<String, String> placeholders) {
        String out = input;
        if (placeholders != null) {
            for (var e : placeholders.entrySet()) {
                out = out.replace("{" + e.getKey() + "}", e.getValue());
            }
        }
        return out;
    }

    private static class Session implements InventoryHolder {
        private final UUID viewer;
        private Inventory inv;
        private final String[] cellsBySlot = new String[54];
        private final CellTier tier;

        Session(UUID viewer, CellTier tier) {
            this.viewer = viewer;
            this.tier = tier;
        }

        @Override
        public Inventory getInventory() {
            return inv;
        }
    }

    private static class TierSession implements InventoryHolder {
        private final UUID viewer;
        private Inventory inv;
        private final CellTier[] tiersBySlot = new CellTier[27];

        TierSession(UUID viewer) {
            this.viewer = viewer;
        }

        @Override
        public Inventory getInventory() {
            return inv;
        }
    }
}
