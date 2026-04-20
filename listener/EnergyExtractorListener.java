/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Sound
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.inventory.ItemStack
 */
package org.axial.prisonsCore.listener;

import org.axial.prisonsCore.service.EnergyItemService;
import org.axial.prisonsCore.service.PickaxeManager;
import org.axial.prisonsCore.util.Lang;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class EnergyExtractorListener
implements Listener {
    private final EnergyItemService energyItemService;
    private final PickaxeManager pickaxeManager;

    public EnergyExtractorListener(EnergyItemService energyItemService, PickaxeManager pickaxeManager) {
        this.energyItemService = energyItemService;
        this.pickaxeManager = pickaxeManager;
    }

    @EventHandler(ignoreCancelled=true)
    public void onApplyExtractor(InventoryClickEvent event) {
        Player p;
        ItemStack cursor = event.getCursor();
        ItemStack target = event.getCurrentItem();
        if (!this.energyItemService.isExtractor(cursor)) {
            return;
        }
        if (!this.pickaxeManager.isPrisonPickaxe(target)) {
            return;
        }
        HumanEntity humanEntity = event.getWhoClicked();
        Player player = humanEntity instanceof Player ? (p = (Player)humanEntity) : null;
        int currentEnergy = this.pickaxeManager.getEnergy(target);
        if (currentEnergy <= 0) {
            if (player != null) {
                player.sendMessage(Lang.msg("extractor.no-energy", "&cThis pickaxe has no charge to extract."));
            }
            return;
        }
        this.pickaxeManager.setEnergy(target, 0);
        this.energyItemService.giveEnergyItem(player, currentEnergy);
        this.consumeOne(event, cursor);
        event.setCancelled(true);
        if (player != null) {
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.4f);
            player.sendMessage(Lang.msg("extractor.extracted", "&aExtracted &e{amount} &acharge without tax.").replace("{amount}", String.valueOf(currentEnergy)));
        }
    }

    private void consumeOne(InventoryClickEvent event, ItemStack cursor) {
        if (cursor.getAmount() <= 1) {
            event.setCursor(null);
        } else {
            cursor.setAmount(cursor.getAmount() - 1);
            event.setCursor(cursor);
        }
    }
}
