/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerFishEvent
 *  org.bukkit.event.player.PlayerFishEvent$State
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.util.Vector
 */
package org.axial.prisonsCore.grappling;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.axial.prisonsCore.grappling.GrapplingHookModule;
import org.axial.prisonsCore.util.Lang;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class GrapplingListener
implements Listener {
    private final GrapplingHookModule module;
    private final Map<UUID, Long> cooldowns = new HashMap<UUID, Long>();
    private static final EnumSet<PlayerFishEvent.State> PULL_STATES = EnumSet.of(PlayerFishEvent.State.IN_GROUND, PlayerFishEvent.State.CAUGHT_ENTITY, PlayerFishEvent.State.CAUGHT_FISH, PlayerFishEvent.State.REEL_IN);

    public GrapplingListener(GrapplingHookModule module) {
        this.module = module;
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        ItemStack hookItem = null;
        Player player = event.getPlayer();
        ItemStack rodInMainHand = player.getInventory().getItemInMainHand();
        ItemStack rodInOffHand = player.getInventory().getItemInOffHand();
        Object object = this.module.isGrapplingHook(rodInMainHand) ? rodInMainHand : (hookItem = this.module.isGrapplingHook(rodInOffHand) ? rodInOffHand : null);
        if (hookItem == null) {
            return;
        }
        PlayerFishEvent.State state = event.getState();
        if (!PULL_STATES.contains(state)) {
            return;
        }
        long cooldown = this.module.getCooldownMillis();
        if (cooldown > 0L) {
            long last;
            UUID id = player.getUniqueId();
            long now = System.currentTimeMillis();
            long remaining = cooldown - (now - (last = this.cooldowns.getOrDefault(id, 0L).longValue()));
            if (remaining > 0L) {
                double secs = (double)remaining / 1000.0;
                player.sendMessage(Lang.msg("grappling.hook-on-cooldown", "&cGrappling hook is recharging ({time_left}s)", Map.of("time_left", String.format(Locale.US, "%.1f", secs))));
                return;
            }
        }
        Location hookLocation = event.getHook().getLocation();
        Location playerLocation = player.getLocation();
        Vector direction = hookLocation.toVector().subtract(playerLocation.toVector());
        double distance = direction.length();
        if (distance < 0.5) {
            return;
        }
        double dx = direction.getX();
        double dz = direction.getZ();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        Vector velocity = new Vector();
        if (horizontalDistance > 0.01) {
            double horizontalBoost = Math.min(3.0 * this.module.getHorizontalPower(), (horizontalDistance * 0.85 + 1.0) * this.module.getHorizontalPower());
            Vector horizontalDir = new Vector(dx, 0.0, dz).normalize();
            velocity.add(horizontalDir.multiply(horizontalBoost));
        }
        double yBase = Math.min(1.4 * this.module.getVerticalPower(), (0.1 * distance + 0.75) * this.module.getVerticalPower());
        double verticalScale = Math.min(1.0, horizontalDistance / (distance + 1.0E-4) + 0.5);
        double yBoost = yBase * verticalScale;
        double maxY = (1.6 + Math.min(0.7, horizontalDistance * 0.18)) * this.module.getVerticalPower();
        velocity.setY(Math.min(velocity.getY() + yBoost, maxY));
        int cost = this.module.getChargePerUse();
        if (cost > 0 && this.module.getCharge(hookItem) < cost) {
            player.sendMessage(Lang.msg("grappling.out-of-charge", "&cYour grappling hook is out of charge."));
            return;
        }
        if (!this.module.consumeCharge(hookItem, cost)) {
            player.sendMessage(Lang.msg("grappling.out-of-charge", "&cYour grappling hook is out of charge."));
            return;
        }
        if (hookItem == rodInMainHand) {
            player.getInventory().setItemInMainHand(hookItem);
        } else if (hookItem == rodInOffHand) {
            player.getInventory().setItemInOffHand(hookItem);
        }
        this.cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        player.setVelocity(velocity);
        player.setFallDistance(0.0f);
    }
}

