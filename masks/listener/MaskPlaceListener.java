/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.BlockPlaceEvent
 *  org.bukkit.inventory.ItemStack
 */
package org.axial.prisonsCore.masks.listener;

import org.axial.prisonsCore.masks.MaskModule;
import org.axial.prisonsCore.masks.model.MaskItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class MaskPlaceListener
implements Listener {
    private final MaskModule module;

    public MaskPlaceListener(MaskModule module) {
        this.module = module;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item == null) {
            return;
        }
        if (MaskItem.isMask(item) || MaskItem.hasAppliedMask(item)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(this.module.getConfigManager().getMessage("mask_not_placeable"));
        }
    }
}

