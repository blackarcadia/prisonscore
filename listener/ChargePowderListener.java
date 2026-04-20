/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.inventory.EquipmentSlot
 *  org.bukkit.inventory.ItemStack
 */
package org.axial.prisonsCore.listener;

import java.util.Map;
import org.axial.prisonsCore.service.EnchantDustService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ChargePowderListener
implements Listener {
    private final EnchantDustService dustService;

    public ChargePowderListener(EnchantDustService dustService) {
        this.dustService = dustService;
    }

    @EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=false)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || this.dustService == null) {
            return;
        }
        if (!this.dustService.isPowder(item)) {
            return;
        }
        Player player = event.getPlayer();
        event.setCancelled(true);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
        ItemStack dust = this.dustService.convertPowderToDust(item);
        if (dust == null) {
            return;
        }
        if (item.getAmount() <= 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            item.setAmount(item.getAmount() - 1);
            player.getInventory().setItemInMainHand(item);
        }
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack[]{dust});
        if (!leftover.isEmpty()) {
            leftover.values().forEach(it -> player.getWorld().dropItem(player.getLocation(), it));
        }
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.1f);
        player.sendMessage(Text.color("&aYour Charge Powder crystallizes into " + dust.getItemMeta().getDisplayName()));
    }
}
