/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryDragEvent
 *  org.bukkit.event.inventory.InventoryType$SlotType
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.event.player.PlayerItemHeldEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.event.player.PlayerSwapHandItemsEvent
 *  org.bukkit.inventory.EquipmentSlot
 *  org.bukkit.inventory.ItemStack
 */
package org.axial.prisonsCore.listener;

import org.axial.prisonsCore.service.GearManager;
import org.axial.prisonsCore.util.Lang;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class BrokenGearUseListener
implements Listener {
    private final GearManager gearManager;

    public BrokenGearUseListener(GearManager gearManager) {
        this.gearManager = gearManager;
    }

    private boolean isBroken(ItemStack stack) {
        return this.gearManager.isBroken(stack);
    }

    @EventHandler(ignoreCancelled=true)
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (this.isBroken(item)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Lang.msg("gear.broken.repair", "&cThat gear is broken. Repair it in the enchanter."));
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if (this.isBroken(event.getMainHandItem()) || this.isBroken(event.getOffHandItem())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Lang.msg("gear.broken.repair", "&cThat gear is broken. Repair it in the enchanter."));
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        if (this.isBroken(newItem)) {
            event.setCancelled(true);
            player.sendMessage(Lang.msg("gear.broken.repair", "&cThat gear is broken. Repair it in the enchanter."));
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onClick(InventoryClickEvent event) {
        if (event.getCurrentItem() != null && this.isBroken(event.getCurrentItem()) && event.getSlotType() == InventoryType.SlotType.ARMOR) {
            event.setCancelled(true);
            ((Player)event.getWhoClicked()).sendMessage(Lang.msg("gear.broken.no-equip", "&cBroken gear cannot be equipped."));
        }
        if (event.getCursor() != null && this.isBroken(event.getCursor()) && event.getSlotType() == InventoryType.SlotType.ARMOR) {
            event.setCancelled(true);
            ((Player)event.getWhoClicked()).sendMessage(Lang.msg("gear.broken.no-equip", "&cBroken gear cannot be equipped."));
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onAttack(EntityDamageByEntityEvent event) {
        Entity entity = event.getDamager();
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player)entity;
        ItemStack hand = player.getInventory().getItem(EquipmentSlot.HAND);
        if (this.isBroken(hand)) {
            event.setCancelled(true);
            player.sendMessage(Lang.msg("gear.broken.no-use", "&cBroken gear cannot be used."));
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onDrag(InventoryDragEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        ItemStack cursor = event.getOldCursor();
        if (this.isBroken(cursor) && event.getInventorySlots().stream().anyMatch(slot -> event.getView().getSlotType(slot.intValue()) == InventoryType.SlotType.ARMOR)) {
            event.setCancelled(true);
            player.sendMessage(Lang.msg("gear.broken.no-equip", "&cBroken gear cannot be equipped."));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
    }
}

