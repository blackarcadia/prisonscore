/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.block.Block
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.block.BlockBreakEvent
 *  org.bukkit.event.block.BlockPlaceEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 */
package org.axial.prisonsCore.cell;

import org.axial.prisonsCore.cell.Cell;
import org.axial.prisonsCore.cell.CellService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class CellListener
implements Listener {
    private final CellService cellService;

    public CellListener(CellService cellService) {
        this.cellService = cellService;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Cell cell = this.cellService.getCellAt(event.getBlock().getLocation());
        if (cell == null) {
            return;
        }
        if (player.hasPermission("cells.admin")) {
            return;
        }
        if (cell.isOwned()) {
            if (this.cellService.hasAccess(cell, player.getUniqueId())) {
                return;
            }
            event.setCancelled(true);
            player.sendMessage(Text.color("&cYou cannot place blocks in someone else's cell."));
        } else {
            event.setCancelled(true);
            player.sendMessage(Text.color("&cYou cannot build in an unowned cell."));
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (this.cellService.isCellDoor(block)) {
            Cell cell = this.cellService.getCellByDoor(block);
            CellService.DoorBreakResult result = this.cellService.handleDoorHit(player, cell, block);
            switch (result) {
                case WRONG_TOOL: {
                    player.sendMessage(Text.color("&cYou need a Cell Buster to break this door."));
                    break;
                }
                case BROKEN: {
                    player.sendMessage(Text.color("&cCell door broken! Raid the cell."));
                    break;
                }
            }
            event.setCancelled(true);
            return;
        }
        Cell cell = this.cellService.getCellAt(block.getLocation());
        if (cell == null || !cell.isOwned()) {
            return;
        }
        if (player.hasPermission("cells.admin") || this.cellService.hasAccess(cell, player.getUniqueId())) {
            return;
        }
        Material type = block.getType();
        if (type == Material.CHEST || type == Material.TRAPPED_CHEST || type == Material.BARREL) {
            event.setCancelled(true);
            player.sendMessage(Text.color("&cYou cannot break chests in another player's cell, but you can open them."));
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onDoorInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        if (!this.cellService.isCellDoor(event.getClickedBlock())) {
            return;
        }
        Player player = event.getPlayer();
        Cell cell = this.cellService.getCellByDoor(event.getClickedBlock());
        if (cell == null) {
            return;
        }
        if (player.hasPermission("cells.admin")) {
            return;
        }
        if (!this.cellService.hasAccess(cell, player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(Text.color("&cYou cannot open this cell door."));
        }
    }
}

