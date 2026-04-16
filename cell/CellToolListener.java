/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.block.Block
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.BlockPlaceEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.inventory.EquipmentSlot
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.persistence.PersistentDataType
 */
package org.axial.prisonsCore.cell;

import org.axial.prisonsCore.Keys;
import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.cell.Cell;
import org.axial.prisonsCore.cell.CellDoorType;
import org.axial.prisonsCore.cell.CellService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class CellToolListener
implements Listener {
    private final PrisonsCore plugin;
    private final CellService cellService;

    public CellToolListener(PrisonsCore plugin, CellService cellService) {
        this.plugin = plugin;
        this.cellService = cellService;
    }

    @EventHandler(ignoreCancelled=true)
    public void onWandUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        ItemStack item = event.getItem();
        if (!this.cellService.isWand(item)) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.hasPermission("cells.admin")) {
            return;
        }
        Block clicked = event.getClickedBlock();
        if (clicked == null) {
            return;
        }
        switch (event.getAction()) {
            case LEFT_CLICK_BLOCK: {
                this.cellService.setPos1(player.getUniqueId(), clicked.getLocation());
                player.sendMessage(Text.color("&aSet pos1 to &f" + this.fmt(clicked)));
                break;
            }
            case RIGHT_CLICK_BLOCK: {
                this.cellService.setPos2(player.getUniqueId(), clicked.getLocation());
                player.sendMessage(Text.color("&aSet pos2 to &f" + this.fmt(clicked)));
                break;
            }
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onDoorPlace(BlockPlaceEvent event) {
        CellDoorType doorType;
        ItemStack stack = event.getItemInHand();
        if (!this.cellService.isDoorItem(stack)) {
            return;
        }
        Player player = event.getPlayer();
        ItemMeta meta = stack.getItemMeta();
        String typeName = (String)meta.getPersistentDataContainer().get(Keys.cellDoorType(this.plugin), PersistentDataType.STRING);
        if (typeName == null) {
            return;
        }
        try {
            doorType = CellDoorType.valueOf(typeName);
        }
        catch (IllegalArgumentException e) {
            player.sendMessage(Text.color("&cInvalid door type data on this item."));
            event.setCancelled(true);
            return;
        }
        Cell cell = this.cellService.getCellAt(event.getBlockPlaced().getLocation());
        if (cell == null) {
            player.sendMessage(Text.color("&cPlace the door inside a defined cell region."));
            event.setCancelled(true);
            return;
        }
        this.cellService.setDoor(cell.getId(), event.getBlockPlaced().getLocation(), doorType);
        player.sendMessage(Text.color("&aDoor set for cell &f" + cell.getId() + " &7as &f" + doorType.name()));
    }

    private String fmt(Block block) {
        return block.getWorld().getName() + " " + block.getX() + "," + block.getY() + "," + block.getZ();
    }
}

