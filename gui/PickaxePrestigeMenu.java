/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.InventoryHolder
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 */
package org.axial.prisonsCore.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.service.PickaxeManager;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PickaxePrestigeMenu
implements Listener {
    private final PrisonsCore plugin;
    private final PickaxeManager pickaxeManager;
    private final Random random = new Random();
    private static final int DRILL_SLOT = 3;
    private static final int XP_SLOT = 5;

    public PickaxePrestigeMenu(PrisonsCore plugin, PickaxeManager pickaxeManager) {
        this.plugin = plugin;
        this.pickaxeManager = pickaxeManager;
    }

    public void open(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (!(this.pickaxeManager.isPrisonPickaxe(held) && this.pickaxeManager.hasPrestigeToken(held) && this.pickaxeManager.canPrestige(held))) {
            player.sendMessage(Text.color("&cHold a prestige-ready pickaxe that meets requirements."));
            return;
        }
        int drill = 10 + this.random.nextInt(21);
        int xp = 10 + this.random.nextInt(31);
        Holder holder = new Holder(held, drill, xp);
        Inventory inv = Bukkit.createInventory((InventoryHolder)holder, (int)9, (String)Text.color("&dPickaxe Prestige"));
        ItemStack drillItem = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta dm = drillItem.getItemMeta();
        dm.setDisplayName(Text.color("&bDrill"));
        dm.setLore(List.of(Text.color("&7Permanent mining speed bonus"), Text.color("&a+" + drill + "% speed"), Text.color("&eClick to select")));
        drillItem.setItemMeta(dm);
        inv.setItem(3, drillItem);
        ItemStack xpItem = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta xm = xpItem.getItemMeta();
        xm.setDisplayName(Text.color("&eXP Mastery"));
        xm.setLore(List.of(Text.color("&7Permanent XP gain bonus"), Text.color("&a+" + xp + "% xp"), Text.color("&eClick to select")));
        xpItem.setItemMeta(xm);
        inv.setItem(5, xpItem);
        player.openInventory(inv);
    }

    @EventHandler(ignoreCancelled=true)
    public void onClick(InventoryClickEvent event) {
        InventoryHolder inventoryHolder = event.getInventory().getHolder();
        if (!(inventoryHolder instanceof Holder)) {
            return;
        }
        Holder holder = (Holder)inventoryHolder;
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot != 3 && slot != 5) {
            return;
        }
        Player player = (Player)event.getWhoClicked();
        ItemStack pickaxe = holder.pickaxe;
        if (!(this.pickaxeManager.isPrisonPickaxe(pickaxe) && this.pickaxeManager.hasPrestigeToken(pickaxe) && this.pickaxeManager.canPrestige(pickaxe))) {
            player.closeInventory();
            return;
        }
        int currentPrestige = this.pickaxeManager.getPrestige(pickaxe);
        this.pickaxeManager.setPrestige(pickaxe, currentPrestige + 1);
        this.pickaxeManager.setPrestigeToken(pickaxe, false);
        if (slot == 3) {
            this.pickaxeManager.setDrillBuff(pickaxe, holder.drill);
            this.pickaxeManager.setXpBuff(pickaxe, 0);
        } else {
            this.pickaxeManager.setXpBuff(pickaxe, holder.xp);
            this.pickaxeManager.setDrillBuff(pickaxe, 0);
        }
        this.pickaxeManager.clearAllEnchantments(pickaxe);
        this.pickaxeManager.setEnchants(pickaxe, new HashMap<String, Integer>());
        this.pickaxeManager.setLevel(pickaxe, 0);
        this.pickaxeManager.setEnergy(pickaxe, 0);
        player.sendMessage(Text.color("&aPickaxe prestiged to &d" + (currentPrestige + 1)));
        player.closeInventory();
    }

    private static class Holder
    implements InventoryHolder {
        private final ItemStack pickaxe;
        private final int drill;
        private final int xp;

        Holder(ItemStack pickaxe, int drill, int xp) {
            this.pickaxe = pickaxe.clone();
            this.drill = drill;
            this.xp = xp;
        }

        public Inventory getInventory() {
            return null;
        }
    }
}

