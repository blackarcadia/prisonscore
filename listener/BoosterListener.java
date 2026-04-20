/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.inventory.EquipmentSlot
 *  org.bukkit.inventory.ItemStack
 */
package org.axial.prisonsCore.listener;

import org.axial.prisonsCore.service.BoosterService;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class BoosterListener
implements Listener {
    private final BoosterService boosterService;

    public BoosterListener(BoosterService boosterService) {
        this.boosterService = boosterService;
    }

    @EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=false)
    public void onUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        if (!this.boosterService.isBooster(item)) {
            return;
        }
        BoosterService.BoosterType type = this.boosterService.parseType(item);
        if (type == null) {
            return;
        }
        event.setCancelled(true);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
        double mult = this.boosterService.readMultiplier(item);
        int duration = this.boosterService.readDuration(item);
        this.boosterService.activateBooster(event.getPlayer(), type, mult, duration);
        item.setAmount(item.getAmount() - 1);
    }
}
