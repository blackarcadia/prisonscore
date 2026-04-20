/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.entity.PlayerDeathEvent
 *  org.bukkit.event.player.PlayerJoinEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 */
package org.axial.prisonsCore.listener;

import org.axial.prisonsCore.service.GuardService;
import org.axial.prisonsCore.service.ReputationService;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ReputationListener
implements Listener {
    private final ReputationService repService;
    private final GuardService guardService;

    public ReputationListener(ReputationService repService, GuardService guardService) {
        this.repService = repService;
        this.guardService = guardService;
    }

    @EventHandler(ignoreCancelled=true)
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        Entity entity = event.getDamager();
        if (!(entity instanceof Player)) {
            return;
        }
        Player damager = (Player)entity;
        if (this.guardService.isGuard(event.getDamager())) {
            return;
        }
        Entity entity2 = event.getEntity();
        if (!(entity2 instanceof Player)) {
            return;
        }
        Player target = (Player)entity2;
        if (this.guardService.isGuard(event.getEntity())) {
            return;
        }
        double dist = this.guardService.nearestGuardDistance(damager.getLocation(), GuardService.GUARD_PROTECTION_RANGE);
        if (dist < 0.0) {
            return;
        }
        ReputationService.Stage stage = this.repService.getStage(damager.getUniqueId());
        switch (stage) {
            case NEUTRAL: {
                this.repService.setStage(damager.getUniqueId(), ReputationService.Stage.WARNED);
                break;
            }
            case WARNED: {
                this.repService.setStage(damager.getUniqueId(), ReputationService.Stage.WATCHED);
                break;
            }
            case WATCHED: {
                this.repService.setStage(damager.getUniqueId(), ReputationService.Stage.WANTED);
                break;
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        ReputationService.Stage stage = this.repService.getStage(player.getUniqueId());
        if (stage == ReputationService.Stage.WANTED || stage == ReputationService.Stage.WATCHED || stage == ReputationService.Stage.WARNED) {
            this.repService.setStage(player.getUniqueId(), ReputationService.Stage.NEUTRAL);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ReputationService.Stage stage = this.repService.getStage(player.getUniqueId());
        if (stage == ReputationService.Stage.WATCHED || stage == ReputationService.Stage.WANTED) {
            this.repService.removeFollower(player.getUniqueId());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ReputationService.Stage stage = this.repService.getStage(player.getUniqueId());
        if (stage == ReputationService.Stage.WATCHED) {
            this.repService.setStage(player.getUniqueId(), ReputationService.Stage.WATCHED);
        } else if (stage == ReputationService.Stage.WANTED) {
            this.repService.setStage(player.getUniqueId(), ReputationService.Stage.WANTED);
        }
    }
}
