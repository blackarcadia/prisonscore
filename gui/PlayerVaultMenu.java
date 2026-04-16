package org.axial.prisonsCore.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.service.PlayerVaultService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerVaultMenu implements Listener {
    private static final int SELECTOR_SIZE = 27;
    private static final int[] VAULT_SLOTS = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24};
    private final PrisonsCore plugin;
    private final PlayerVaultService vaultService;

    public PlayerVaultMenu(PrisonsCore plugin, PlayerVaultService vaultService) {
        this.plugin = plugin;
        this.vaultService = vaultService;
    }

    public void openSelector(Player viewer) {
        this.openSelector(viewer, viewer, false);
    }

    public void openSelector(Player viewer, OfflinePlayer owner, boolean adminView) {
        List<Integer> vaultNumbers = adminView ? this.vaultService.adminVaultNumbers(owner.getUniqueId(), owner.isOnline() ? owner.getPlayer() : null) : this.vaultService.accessibleVaultNumbers(viewer);
        SelectorHolder holder = new SelectorHolder(viewer.getUniqueId(), owner.getUniqueId(), this.ownerName(owner), adminView);
        Inventory inventory = Bukkit.createInventory(holder, SELECTOR_SIZE, Text.color(adminView ? "&8" + this.ownerName(owner) + " Vaults" : "&8Player Vaults"));
        holder.inventory = inventory;
        this.fillBackground(inventory);
        inventory.setItem(4, this.infoItem(adminView, owner, vaultNumbers.size()));
        for (int i = 0; i < vaultNumbers.size() && i < VAULT_SLOTS.length; ++i) {
            int vaultNumber = vaultNumbers.get(i);
            inventory.setItem(VAULT_SLOTS[i], this.vaultIcon(vaultNumber, owner.getUniqueId()));
        }
        viewer.playSound(viewer.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.0f);
        viewer.openInventory(inventory);
    }

    public boolean openVault(Player viewer, OfflinePlayer owner, int vaultNumber, boolean adminView) {
        if (!this.vaultService.isValidVaultNumber(vaultNumber)) {
            return false;
        }
        if (!adminView && !this.vaultService.canAccess(viewer, vaultNumber)) {
            return false;
        }
        VaultHolder holder = new VaultHolder(viewer.getUniqueId(), owner.getUniqueId(), this.ownerName(owner), vaultNumber, adminView);
        Inventory inventory = Bukkit.createInventory(holder, PlayerVaultService.VAULT_SIZE, Text.color(adminView ? "&8" + this.ownerName(owner) + " PV " + vaultNumber : "&8PV " + vaultNumber));
        holder.inventory = inventory;
        inventory.setContents(this.vaultService.loadVault(owner.getUniqueId(), vaultNumber));
        viewer.playSound(viewer.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.0f);
        viewer.openInventory(inventory);
        return true;
    }

    public void saveOpenVaults() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory top = player.getOpenInventory().getTopInventory();
            if (!(top.getHolder() instanceof VaultHolder holder)) {
                continue;
            }
            this.vaultService.saveVault(holder.ownerId, holder.vaultNumber, top.getContents());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSelectorClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof SelectorHolder holder)) {
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
        for (int i = 0; i < VAULT_SLOTS.length; ++i) {
            if (event.getRawSlot() != VAULT_SLOTS[i]) {
                continue;
            }
            int vaultNumber = i + 1;
            OfflinePlayer owner = Bukkit.getOfflinePlayer(holder.ownerId);
            if (!holder.adminView && !this.vaultService.canAccess(player, vaultNumber)) {
                player.sendMessage(Text.color("&cYou do not have access to that player vault."));
                return;
            }
            this.openVault(player, owner, vaultNumber, holder.adminView);
            return;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVaultClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof VaultHolder holder)) {
            return;
        }
        if (holder.adminView) {
            event.setCancelled(true);
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!player.getUniqueId().equals(holder.viewerId) || !player.getUniqueId().equals(holder.ownerId)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVaultDrag(InventoryDragEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof VaultHolder holder)) {
            return;
        }
        if (!holder.adminView) {
            return;
        }
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < event.getView().getTopInventory().getSize()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder inventoryHolder = event.getInventory().getHolder();
        if (!(inventoryHolder instanceof VaultHolder holder)) {
            return;
        }
        this.vaultService.saveVault(holder.ownerId, holder.vaultNumber, event.getInventory().getContents());
        HumanEntity who = event.getPlayer();
        if (!(who instanceof Player player) || !player.isOnline()) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            this.openSelector(player, Bukkit.getOfflinePlayer(holder.ownerId), holder.adminView);
        }, 1L);
    }

    private ItemStack infoItem(boolean adminView, OfflinePlayer owner, int count) {
        List<String> lore = new ArrayList<String>();
        lore.add(adminView ? "&7Viewing vaults owned by &f" + this.ownerName(owner) : "&7Select a player vault to open.");
        lore.add("&7Available vaults: &f" + count);
        if (adminView) {
            lore.add("&cAdmin view is read-only.");
        } else {
            lore.add("&7Closing a vault returns here.");
        }
        return this.namedItem(Material.BOOK, adminView ? "&f&lAdmin Vault View" : "&f&lPlayer Vaults", lore);
    }

    private ItemStack vaultIcon(int vaultNumber, UUID ownerId) {
        boolean hasData = this.vaultService.hasVaultData(ownerId, vaultNumber);
        Material material = hasData ? Material.CHEST : Material.ENDER_CHEST;
        return this.namedItem(material, "&e&l" + this.vaultService.formatVaultName(vaultNumber), List.of(
                "&7Click to open this vault.",
                "&7Vault size: &f54 slots"
        ));
    }

    private void fillBackground(Inventory inventory) {
        ItemStack filler = this.namedItem(Material.GRAY_STAINED_GLASS_PANE, "&7", null);
        for (int slot = 0; slot < inventory.getSize(); ++slot) {
            inventory.setItem(slot, filler);
        }
    }

    private ItemStack namedItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Text.componentNoItalics(name));
            if (lore != null && !lore.isEmpty()) {
                meta.lore(lore.stream().map(Text::componentNoItalics).collect(Collectors.toList()));
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String ownerName(OfflinePlayer owner) {
        return owner.getName() != null ? owner.getName() : owner.getUniqueId().toString();
    }

    private static final class SelectorHolder implements InventoryHolder {
        private final UUID viewerId;
        private final UUID ownerId;
        private final String ownerName;
        private final boolean adminView;
        private Inventory inventory;

        private SelectorHolder(UUID viewerId, UUID ownerId, String ownerName, boolean adminView) {
            this.viewerId = viewerId;
            this.ownerId = ownerId;
            this.ownerName = ownerName;
            this.adminView = adminView;
        }

        @Override
        public Inventory getInventory() {
            return this.inventory;
        }
    }

    private static final class VaultHolder implements InventoryHolder {
        private final UUID viewerId;
        private final UUID ownerId;
        private final String ownerName;
        private final int vaultNumber;
        private final boolean adminView;
        private Inventory inventory;

        private VaultHolder(UUID viewerId, UUID ownerId, String ownerName, int vaultNumber, boolean adminView) {
            this.viewerId = viewerId;
            this.ownerId = ownerId;
            this.ownerName = ownerName;
            this.vaultNumber = vaultNumber;
            this.adminView = adminView;
        }

        @Override
        public Inventory getInventory() {
            return this.inventory;
        }
    }
}
