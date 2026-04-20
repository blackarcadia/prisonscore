/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.persistence.PersistentDataType
 */
package org.axial.prisonsCore.listener;

import org.axial.prisonsCore.Keys;
import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.grappling.GrapplingHookModule;
import org.axial.prisonsCore.service.EnergyItemService;
import org.axial.prisonsCore.service.GearManager;
import org.axial.prisonsCore.service.PickaxeManager;
import org.axial.prisonsCore.service.SatchelManager;
import org.axial.prisonsCore.util.Lang;
import org.axial.prisonsCore.util.Text;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class EnergyApplyListener
implements Listener {
    private final PickaxeManager pickaxeManager;
    private final GearManager gearManager;
    private final EnergyItemService energyItemService;
    private final SatchelManager satchelManager;
    private final GrapplingHookModule grapplingHookModule;
    private final PrisonsCore plugin;

    public EnergyApplyListener(PrisonsCore plugin, PickaxeManager pickaxeManager, GearManager gearManager, SatchelManager satchelManager, EnergyItemService energyItemService, GrapplingHookModule grapplingHookModule) {
        this.pickaxeManager = pickaxeManager;
        this.gearManager = gearManager;
        this.satchelManager = satchelManager;
        this.energyItemService = energyItemService;
        this.grapplingHookModule = grapplingHookModule;
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled=true)
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity p;
        boolean isGrapple;
        ItemStack cursor = event.getCursor();
        ItemStack target = event.getCurrentItem();
        if (cursor == null || target == null) {
            return;
        }
        if (this.isPrestigeToken(cursor) && this.pickaxeManager.isPrisonPickaxe(target)) {
            this.pickaxeManager.setPrestigeToken(target, true);
            HumanEntity humanEntity = event.getWhoClicked();
            if (humanEntity instanceof Player) {
                Player p2 = (Player)humanEntity;
                p2.sendMessage(Text.color("&aPrestige token applied to your pickaxe."));
            }
            event.setCursor(null);
            event.setCancelled(true);
            return;
        }
        if (!this.energyItemService.isEnergyItem(cursor)) {
            return;
        }
        if (this.energyItemService.isEnergyItem(target)) {
            int combined = this.energyItemService.getTotalAmount(cursor) + this.energyItemService.getTotalAmount(target);
            event.setCurrentItem(this.energyItemService.createEnergyItem(combined));
            event.setCursor(null);
            event.setCancelled(true);
            return;
        }
        boolean isPickaxe = this.pickaxeManager.isPrisonPickaxe(target);
        boolean isBuster = this.plugin.getCellBusterManager().isBuster(target);
        boolean isGear = !isPickaxe && this.gearManager.isGear(target);
        boolean isSatchel = this.satchelManager.isSatchel(target);
        boolean bl = isGrapple = !isPickaxe && !isGear && this.grapplingHookModule != null && this.grapplingHookModule.isGrapplingHook(target);
        if (!(isPickaxe || isGear || isSatchel || isBuster || isGrapple)) {
            return;
        }
        int energyInItem = this.energyItemService.getTotalAmount(cursor);
        if (energyInItem <= 0) {
            return;
        }
        if (isPickaxe) {
            int current = this.pickaxeManager.getEnergy(target);
            this.pickaxeManager.setEnergyOverflow(target, current + energyInItem);
            HumanEntity clicker = event.getWhoClicked();
            if (this.pickaxeManager.isFull(target) && clicker instanceof Player player) {
                player.sendTitle(Text.color("&cPickaxe is full of charge"), "", 5, 40, 10);
            }
        } else if (isBuster) {
            int current = this.plugin.getCellBusterManager().getEnergy(target);
            this.plugin.getCellBusterManager().setEnergy(target, current + energyInItem);
            HumanEntity clicker = event.getWhoClicked();
            if (this.plugin.getCellBusterManager().isFull(target) && clicker instanceof Player player) {
                player.sendTitle(Text.color("&cCell Buster is full of charge"), "", 5, 40, 10);
            }
        } else if (isGear) {
            int current = this.gearManager.getEnergy(target);
            this.gearManager.setEnergy(target, current + energyInItem);
        } else if (isGrapple) {
            this.grapplingHookModule.addCharge(target, energyInItem);
        } else {
            this.satchelManager.addEnergy(target, energyInItem);
        }
        event.setCursor(null);
        event.setCurrentItem(target);
        event.setCancelled(true);
        p = event.getWhoClicked();
        if (p instanceof Player) {
            Player player = (Player)p;
            String targetName = isPickaxe ? "pickaxe" : (isGear ? "gear" : (isGrapple ? "grappling hook" : "satchel"));
            player.sendMessage(Lang.msg("extract.energy-applied", "&aAdded {amount} charge to your {item}.").replace("{amount}", String.valueOf(energyInItem)).replace("{item}", targetName));
        }
    }

    private boolean isPrestigeToken(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return false;
        }
        Byte b = (Byte)stack.getItemMeta().getPersistentDataContainer().get(Keys.pickaxePrestigeToken(this.plugin), PersistentDataType.BYTE);
        return b != null && b == 1;
    }
}
