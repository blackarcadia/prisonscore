package org.axial.prisonsCore.listener;

import org.axial.prisonsCore.service.GuardService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.Event;

public class GuardSpawnerListener implements Listener {
    private final GuardService guardService;

    public GuardSpawnerListener(GuardService guardService) {
        this.guardService = guardService;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Action action = event.getAction();
        if (!action.isRightClick()) {
            return;
        }
        ItemStack item = event.getItem();
        if (!this.guardService.isGuardSpawnerItem(item)) {
            return;
        }
        Player player = event.getPlayer();
        event.setCancelled(true);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
        String type = this.guardService.getGuardSpawnerType(item);
        if (type == null || type.isBlank()) {
            type = "default";
        }
        if (this.guardService.getTemplate(type) == null) {
            player.sendMessage(Text.color("&cUnknown guard spawner type."));
            return;
        }
        if (this.guardService.spawnGuard(this.guardService.getTemplate(type), player.getLocation(), null, player) != null) {
            if (player.getGameMode() != GameMode.CREATIVE) {
                item.setAmount(item.getAmount() - 1);
                player.getInventory().setItemInMainHand(item.getAmount() <= 0 ? null : item);
            }
            player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_REPAIR, 1.0f, 1.1f);
            player.sendMessage(Text.color("&aSpawned a &f" + type + " &aguard."));
        } else {
            player.sendMessage(Text.color("&cFailed to spawn guard."));
        }
    }
}
