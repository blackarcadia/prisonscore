package org.axial.prisonsCore.listener;

import java.util.Iterator;
import java.util.Set;
import org.axial.prisonsCore.service.TutorialService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TutorialListener implements Listener {
    private final TutorialService tutorialService;

    public TutorialListener(TutorialService tutorialService) {
        this.tutorialService = tutorialService;
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        this.tutorialService.handleJoin(event.getPlayer());
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        this.tutorialService.handleRespawn(event.getPlayer(), event);
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onChat(AsyncPlayerChatEvent event) {
        Set<Player> recipients = event.getRecipients();
        Iterator<Player> iterator = recipients.iterator();
        while (iterator.hasNext()) {
            Player recipient = iterator.next();
            if (!recipient.equals((Object)event.getPlayer()) && this.tutorialService.shouldFilterChat(recipient)) {
                iterator.remove();
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (!this.tutorialService.shouldBlockTeleportOut(event.getPlayer(), event.getTo())) {
            return;
        }
        event.setCancelled(true);
        this.tutorialService.notifyTeleportBlocked(event.getPlayer());
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        if (!Text.color("&8Mining Fragments").equals(event.getView().getTitle())) {
            return;
        }
        this.tutorialService.handleShardMenuClosed((Player)event.getPlayer());
    }
}
