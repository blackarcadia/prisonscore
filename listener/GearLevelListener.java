package org.axial.prisonsCore.listener;

import org.axial.prisonsCore.service.GearManager;
import org.axial.prisonsCore.service.PlayerLevelService;
import org.axial.prisonsCore.service.SeasonService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class GearLevelListener implements Listener {
    private final GearManager gearManager;
    private final PlayerLevelService playerLevelService;
    private final SeasonService seasonService;

    public GearLevelListener(GearManager gearManager, PlayerLevelService playerLevelService, SeasonService seasonService) {
        this.gearManager = gearManager;
        this.playerLevelService = playerLevelService;
        this.seasonService = seasonService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        this.normalizeGear(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR || !this.isArmor(item)) {
            return;
        }
        Player player = event.getPlayer();
        if (!this.canEquip(player, item)) {
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            this.sendDenied(player, item);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getSlotType() == InventoryType.SlotType.ARMOR && !this.canEquip(player, event.getCursor())) {
            event.setCancelled(true);
            this.sendDenied(player, event.getCursor());
            return;
        }
        if (event.getClick() == ClickType.RIGHT && event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.PLAYER) {
            ItemStack clickedItem = event.getCurrentItem();
            if (this.isArmor(clickedItem) && !this.canEquip(player, clickedItem)) {
                event.setCancelled(true);
                this.sendDenied(player, clickedItem);
                return;
            }
        }
        if (event.isShiftClick() && this.isArmor(event.getCurrentItem()) && !this.canEquip(player, event.getCurrentItem())) {
            event.setCancelled(true);
            this.sendDenied(player, event.getCurrentItem());
            return;
        }
        if (event.getClick() == ClickType.NUMBER_KEY) {
            ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
            if (event.getSlotType() == InventoryType.SlotType.ARMOR && !this.canEquip(player, hotbarItem)) {
                event.setCancelled(true);
                this.sendDenied(player, hotbarItem);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack cursor = event.getOldCursor();
        if (!this.isArmor(cursor) || this.canEquip(player, cursor)) {
            return;
        }
        if (event.getInventorySlots().stream().anyMatch(slot -> event.getView().getSlotType(slot) == InventoryType.SlotType.ARMOR)) {
            event.setCancelled(true);
            this.sendDenied(player, cursor);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDispense(BlockDispenseArmorEvent event) {
        if (event.getTargetEntity() instanceof Player player) {
            ItemStack item = event.getItem();
            if (!this.canEquip(player, item)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean canEquip(Player player, ItemStack item) {
        if (!this.isArmor(item)) {
            return true;
        }
        int requiredLevel = this.gearManager.getRequiredLevel(item);
        if (requiredLevel > 0 && this.playerLevelService.getLevel(player) < requiredLevel) {
            return false;
        }
        SeasonService.UnlockFeature unlockFeature = this.seasonService.getFeature(item.getType());
        return unlockFeature == null || this.seasonService.isFeatureUnlocked(unlockFeature);
    }

    private boolean isArmor(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !this.gearManager.isGear(item)) {
            return false;
        }
        String type = item.getType().name();
        return type.endsWith("_HELMET") || type.endsWith("_CHESTPLATE") || type.endsWith("_LEGGINGS") || type.endsWith("_BOOTS");
    }

    private void normalizeGear(Player player) {
        if (player == null) {
            return;
        }
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (this.gearManager.isGear(item)) {
                this.gearManager.updateLore(item);
            }
        }
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (this.gearManager.isGear(item)) {
                this.gearManager.updateLore(item);
            }
        }
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (this.gearManager.isGear(offhand)) {
            this.gearManager.updateLore(offhand);
        }
    }

    private void sendDenied(Player player, ItemStack item) {
        SeasonService.UnlockFeature unlockFeature = this.seasonService.getFeature(item.getType());
        if (unlockFeature != null && !this.seasonService.isFeatureUnlocked(unlockFeature)) {
            player.sendMessage(Text.color("&cThat gear unlocks on Day " + this.seasonService.getUnlockDay(unlockFeature) + "."));
            return;
        }
        int requiredLevel = this.gearManager.getRequiredLevel(item);
        player.sendMessage(Text.color("&cYou must be level " + requiredLevel + " to wear this gear."));
    }
}
