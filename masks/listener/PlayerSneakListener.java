/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerToggleSneakEvent
 */
package org.axial.prisonsCore.masks.listener;

import org.axial.prisonsCore.masks.MaskModule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class PlayerSneakListener
implements Listener {
    private final MaskModule module;

    public PlayerSneakListener(MaskModule module) {
        this.module = module;
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        this.module.getMaskManager().updatePlayerMaskEffects(player);
        this.module.getMaskManager().updateMovementEffects(player);
    }
}

