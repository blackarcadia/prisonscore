/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.entity.Display$Billboard
 *  org.bukkit.entity.ItemDisplay
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.PlayerDeathEvent
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.util.Transformation
 *  org.bukkit.util.Vector
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 */
package org.axial.prisonsCore.listener;

import org.axial.prisonsCore.PrisonsCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class DeathAnimationListener
implements Listener {
    private final PrisonsCore plugin;

    public DeathAnimationListener(PrisonsCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled=true)
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (!(killer instanceof Player)) {
            return;
        }
        Player killerPlayer = killer;
        ItemStack weapon = killerPlayer.getInventory().getItemInMainHand();
        if (weapon == null || weapon.getType() == Material.AIR) {
            return;
        }
        Location base = victim.getLocation();
        Location spawnLoc = base.clone().add(0.0, 10.0, 0.0);
        ItemDisplay display = (ItemDisplay)victim.getWorld().spawn(spawnLoc, ItemDisplay.class, d -> {
            d.setItemStack(new ItemStack(weapon));
            d.setBillboard(Display.Billboard.CENTER);
            Transformation transform = new Transformation(new Vector3f(), new Quaternionf(), new Vector3f(6.0f, 6.0f, 6.0f), new Quaternionf());
            d.setTransformation(transform);
            d.setRotation(0.0f, 180.0f);
        });
        Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, task -> {
            if (!display.isValid()) {
                task.cancel();
                return;
            }
            Location loc = display.getLocation();
            Vector dir = base.clone().add(0.0, 0.5, 0.0).toVector().subtract(loc.toVector());
            double dist = dir.length();
            if (dist < 0.4) {
                display.remove();
                task.cancel();
                return;
            }
            dir.normalize().multiply(Math.min(0.5, 0.08 + dist * 0.08));
            display.teleport(loc.add(dir));
        }, 0L, 1L);
    }
}

