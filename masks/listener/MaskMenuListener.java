package org.axial.prisonsCore.masks.listener;

import org.axial.prisonsCore.masks.menu.MaskMenu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class MaskMenuListener implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof MaskMenu) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof MaskMenu) {
            event.setCancelled(true);
        }
    }
}
