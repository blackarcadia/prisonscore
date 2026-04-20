/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.attribute.Attribute
 *  org.bukkit.attribute.AttributeInstance
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.EntityDamageEvent
 *  org.bukkit.event.entity.EntityDamageEvent$DamageCause
 */
package org.axial.prisonsCore.listener;

import org.axial.prisonsCore.service.CombatLogManager;
import org.axial.prisonsCore.util.Text;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class FallDamageListener
implements Listener {
    private static final String SAVED_MESSAGE = "&e&l(!) &eThe planets gravity saved you from dying of fall damage.";
    private final CombatLogManager combatLogManager;

    public FallDamageListener(CombatLogManager combatLogManager) {
        this.combatLogManager = combatLogManager;
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player)entity;
        if (this.combatLogManager != null && !this.combatLogManager.isInCombat(player)) {
            event.setCancelled(true);
            player.setFallDistance(0.0f);
            return;
        }
        double finalDamage = event.getFinalDamage();
        double totalHealth = player.getHealth() + player.getAbsorptionAmount();
        if (totalHealth - finalDamage > 0.0) {
            return;
        }
        event.setCancelled(true);
        player.setFallDistance(0.0f);
        Attribute maxHealthAttr = this.resolveMaxHealthAttribute();
        AttributeInstance maxHealthAttribute = maxHealthAttr != null ? player.getAttribute(maxHealthAttr) : null;
        double maxHealth = maxHealthAttribute != null ? maxHealthAttribute.getValue() : player.getHealth();
        double targetHealth = Math.min(1.0, maxHealth);
        player.setHealth(targetHealth);
        player.sendMessage(Text.color(SAVED_MESSAGE));
    }

    private Attribute resolveMaxHealthAttribute() {
        try {
            return Attribute.valueOf((String)"GENERIC_MAX_HEALTH");
        }
        catch (IllegalArgumentException ignored) {
            try {
                return Attribute.valueOf((String)"MAX_HEALTH");
            }
            catch (IllegalArgumentException ex) {
                return null;
            }
        }
    }
}

