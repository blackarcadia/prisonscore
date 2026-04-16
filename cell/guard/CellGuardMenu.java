/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.InventoryHolder
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 */
package org.axial.prisonsCore.cell.guard;

import java.util.List;
import java.util.UUID;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CellGuardMenu
implements InventoryHolder {
    private final UUID placementId;

    public CellGuardMenu(UUID placementId) {
        this.placementId = placementId;
    }

    public UUID getPlacementId() {
        return this.placementId;
    }

    public Inventory getInventory() {
        Inventory inv = Bukkit.createInventory((InventoryHolder)this, (int)9, (String)Text.color("&8Cell Guard"));
        ItemStack remove = new ItemStack(Material.BARRIER);
        ItemMeta meta = remove.getItemMeta();
        meta.setDisplayName(Text.color("&cRemove Guard"));
        meta.setLore(List.of(Text.color("&7Click to remove this guard"), Text.color("&7and receive the anchor back.")));
        remove.setItemMeta(meta);
        inv.setItem(4, remove);
        return inv;
    }

    public static void open(Player player, UUID placementId) {
        player.openInventory(new CellGuardMenu(placementId).getInventory());
    }
}

