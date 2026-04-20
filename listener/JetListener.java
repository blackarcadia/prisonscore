/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.event.player.PlayerToggleSneakEvent
 */
package org.axial.prisonsCore.listener;

import org.axial.prisonsCore.service.JetService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class JetListener
implements Listener {
    private final JetService jetService;

    public JetListener(JetService jetService) {
        this.jetService = jetService;
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        this.jetService.handleSneak(player.getUniqueId(), event.isSneaking());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.jetService.stop(event.getPlayer().getUniqueId(), false);
    }
}

