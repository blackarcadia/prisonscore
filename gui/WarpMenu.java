package org.axial.prisonsCore.gui;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.axial.prisonsCore.service.FixedTeleportLocations;
import org.axial.prisonsCore.service.PlayerDataService;
import org.axial.prisonsCore.service.TeleportService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class WarpMenu implements Listener {
    private static final int INVENTORY_SIZE = 27;
    private static final int[] WARP_SLOTS = new int[]{10, 11, 12, 13, 14, 15, 16};
    private static final int UNLOCK_RADIUS = 100;
    private final TeleportService teleportService;
    private final PlayerDataService playerDataService;
    private final Map<WarpDefinition, Integer> slotByWarp = new EnumMap<>(WarpDefinition.class);

    public WarpMenu(TeleportService teleportService, PlayerDataService playerDataService) {
        this.teleportService = teleportService;
        this.playerDataService = playerDataService;
        for (int i = 0; i < WarpDefinition.values().length && i < WARP_SLOTS.length; ++i) {
            this.slotByWarp.put(WarpDefinition.values()[i], WARP_SLOTS[i]);
        }
    }

    public void open(Player player) {
        MenuHolder holder = new MenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, INVENTORY_SIZE, Text.color("&8Warps"));
        holder.inventory = inventory;
        this.fillBackground(inventory);
        for (WarpDefinition warp : WarpDefinition.values()) {
            Integer slot = this.slotByWarp.get(warp);
            if (slot == null) {
                continue;
            }
            inventory.setItem(slot, this.createWarpItem(player, warp));
        }
        player.openInventory(inventory);
    }

    public boolean handleWarpCommand(Player player, String name) {
        WarpDefinition warp = WarpDefinition.byKey(name);
        if (warp == null) {
            Location directWarp = this.teleportService.getWarp(name);
            if (directWarp == null) {
                player.sendMessage(Text.color("&cWarp not found."));
                return true;
            }
            this.teleportService.scheduleTeleport(player, directWarp, name, null);
            return true;
        }
        if (!this.isUnlocked(player, warp)) {
            player.sendMessage(Text.color("&c" + warp.getDisplayName() + " is locked."));
            return true;
        }
        Location warpLocation = this.teleportService.getWarp(warp.getKey());
        if (warpLocation == null) {
            player.sendMessage(Text.color("&cWarp not found."));
            return true;
        }
        this.teleportService.scheduleTeleport(player, warpLocation, warp.getKey(), null);
        return true;
    }

    public void handleSuccessfulMine(Player player, Material oreType, Location blockLocation) {
        if (player == null || oreType == null || blockLocation == null) {
            return;
        }
        for (WarpDefinition warp : WarpDefinition.values()) {
            if (warp.getOreMaterial() != oreType || this.isUnlocked(player, warp)) {
                continue;
            }
            Location warpLocation = this.teleportService.getWarp(warp.getKey());
            if (!this.isWithinUnlockRange(blockLocation, warpLocation)) {
                continue;
            }
            this.unlock(player, warp);
        }
    }

    public boolean isUnlocked(Player player, WarpDefinition warp) {
        return this.playerDataService.get(player.getUniqueId()).hasUnlockedWarp(warp.getKey());
    }

    private boolean isWithinUnlockRange(Location mined, Location warp) {
        if (warp == null || mined.getWorld() == null || warp.getWorld() == null) {
            return false;
        }
        if (!mined.getWorld().getUID().equals(warp.getWorld().getUID())) {
            return false;
        }
        return Math.abs(mined.getBlockX() - warp.getBlockX()) <= UNLOCK_RADIUS && Math.abs(mined.getBlockZ() - warp.getBlockZ()) <= UNLOCK_RADIUS;
    }

    private void unlock(Player player, WarpDefinition warp) {
        String warpKey = warp.getKey();
        String warpDisplayName = warp.getDisplayName();
        PlayerDataService.PlayerData data = this.playerDataService.get(player.getUniqueId());
        data.unlockWarp(warpKey);
        this.playerDataService.update(player.getUniqueId(), ignored -> {
        });
        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0.0, 1.0, 0.0), 30, 0.5, 0.5, 0.5, 0.05);
        player.sendTitle(Text.color("&a&l" + warpDisplayName + " Unlocked"), "", 10, 50, 10);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof MenuHolder)) {
            return;
        }
        event.setCancelled(true);
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getView().getTopInventory())) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        int slot = event.getRawSlot();
        for (WarpDefinition warp : WarpDefinition.values()) {
            Integer warpSlot = this.slotByWarp.get(warp);
            if (warpSlot == null || warpSlot != slot) {
                continue;
            }
            if (!this.isUnlocked(player, warp)) {
                player.sendMessage(Text.color("&c" + warp.getDisplayName() + " is locked."));
                return;
            }
            Location warpLocation = this.teleportService.getWarp(warp.getKey());
            if (warpLocation == null) {
                player.sendMessage(Text.color("&cWarp not found."));
                return;
            }
            player.closeInventory();
            this.teleportService.scheduleTeleport(player, warpLocation, warp.getKey(), null);
            return;
        }
    }

    private ItemStack createWarpItem(Player player, WarpDefinition warp) {
        boolean unlocked = this.isUnlocked(player, warp);
        ItemStack item = new ItemStack(unlocked ? warp.getDisplayMaterial() : Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.displayName(Text.componentNoItalics((unlocked ? "&a&l" : "&c&l") + warp.getDisplayName()));
        List<Component> lore = new ArrayList<>();
        lore.add(Text.componentNoItalics(FixedTeleportLocations.getWarpCoordinateLine(warp.getKey())));
        lore.add(Text.componentNoItalics(""));
        lore.add(Text.componentNoItalics(unlocked ? "&a&lUnlocked" : "&c&lLocked"));
        if (unlocked) {
            lore.add(Text.componentNoItalics("&7Click to teleport to the " + warp.getKey() + " mine."));
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void fillBackground(Inventory inventory) {
        ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.displayName(Text.componentNoItalics(" "));
            pane.setItemMeta(meta);
        }
        for (int slot = 0; slot < inventory.getSize(); ++slot) {
            inventory.setItem(slot, pane);
        }
    }

    public enum WarpDefinition {
        COAL("coal", "Coal Mine", Material.COAL_ORE),
        IRON("iron", "Iron Mine", Material.IRON_ORE),
        LAPIS("lapis", "Lapis Mine", Material.LAPIS_ORE),
        GOLD("gold", "Gold Mine", Material.GOLD_ORE),
        REDSTONE("redstone", "Redstone Mine", Material.REDSTONE_ORE),
        DIAMOND("diamond", "Diamond Mine", Material.DIAMOND_ORE),
        EMERALD("emerald", "Emerald Mine", Material.EMERALD_ORE);

        private final String key;
        private final String displayName;
        private final Material oreMaterial;
        private final Material displayMaterial;

        WarpDefinition(String key, String displayName, Material oreMaterial) {
            this.key = key;
            this.displayName = displayName;
            this.oreMaterial = oreMaterial;
            this.displayMaterial = oreMaterial;
        }

        public String getKey() {
            return this.key;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public Material getOreMaterial() {
            return this.oreMaterial;
        }

        public Material getDisplayMaterial() {
            return this.displayMaterial;
        }

        private static WarpDefinition byKey(String input) {
            if (input == null) {
                return null;
            }
            String normalized = input.toLowerCase(Locale.ROOT);
            for (WarpDefinition warp : values()) {
                if (warp.getKey().equals(normalized)) {
                    return warp;
                }
            }
            return null;
        }
    }

    private static final class MenuHolder implements InventoryHolder {
        private Inventory inventory;

        @Override
        public Inventory getInventory() {
            return this.inventory;
        }
    }
}
