/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.persistence.PersistentDataType
 */
package org.axial.prisonsCore.listener;

import org.axial.prisonsCore.model.Gang;
import org.axial.prisonsCore.service.GangService;
import org.axial.prisonsCore.util.Lang;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class GangPointRedeemListener
implements Listener {
    private final GangService gangService;
    private final NamespacedKey voucherKey;

    public GangPointRedeemListener(GangService gangService, NamespacedKey voucherKey) {
        this.gangService = gangService;
        this.voucherKey = voucherKey;
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
        if (item == null || item.getType() != Material.PAPER || !item.hasItemMeta()) {
            return;
        }
        Integer amount = (Integer)item.getItemMeta().getPersistentDataContainer().get(this.voucherKey, PersistentDataType.INTEGER);
        if (amount == null || amount <= 0) {
            return;
        }
        event.setCancelled(true);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
        Player player = event.getPlayer();
        Gang gang = this.gangService.getGangByPlayer(player.getUniqueId());
        if (gang == null) {
            player.sendMessage(Lang.msg("gangpoints.redeem.no-gang", "&cYou must be in a gang to redeem gang points."));
            return;
        }
        this.gangService.addPoints(gang.getId(), amount);
        player.sendMessage(Lang.msg("gangpoints.redeem.success", "&aAdded &f{amount} &agang points to &f{name}").replace("{amount}", String.valueOf(amount)).replace("{name}", gang.getName()));
        int newAmount = item.getAmount() - 1;
        if (newAmount <= 0) {
            player.getInventory().setItem(event.getHand(), null);
        } else {
            item.setAmount(newAmount);
            player.getInventory().setItem(event.getHand(), item);
        }
    }
}
