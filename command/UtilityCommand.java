/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.GameMode
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.Damageable
 *  org.bukkit.inventory.meta.ItemMeta
 */
package org.axial.prisonsCore.command;

import org.axial.prisonsCore.util.Text;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class UtilityCommand
implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name;
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        Player player = (Player)sender;
        switch (name = command.getName().toLowerCase()) {
            case "fix": {
                this.handleFix(player, args);
                break;
            }
            case "feed": {
                this.feed(player);
                break;
            }
            case "gmc": {
                this.setMode(player, GameMode.CREATIVE);
                break;
            }
            case "gms": {
                this.setMode(player, GameMode.SURVIVAL);
                break;
            }
            case "gmsp": {
                this.setMode(player, GameMode.SPECTATOR);
                break;
            }
            case "fly": {
                this.toggleFly(player);
                break;
            }
            case "top": {
                this.teleportTop(player);
                break;
            }
        }
        return true;
    }

    private void handleFix(Player player, String[] args) {
        String sub;
        if (args.length == 0) {
            player.sendMessage(Text.color("&cUsage: /fix <hand|all>"));
            return;
        }
        switch (sub = args[0].toLowerCase()) {
            case "hand": {
                this.fixHand(player);
                break;
            }
            case "all": {
                this.fixAll(player);
                break;
            }
            default: {
                player.sendMessage(Text.color("&cUsage: /fix <hand|all>"));
            }
        }
    }

    private void fixHand(Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!this.isRepairable(hand)) {
            player.sendMessage(Text.color("&cNo repairable item in hand."));
            return;
        }
        this.setDurability(hand, 0);
        player.sendMessage(Text.color("&aRepaired item in hand."));
    }

    private void fixAll(Player player) {
        int repaired = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (!this.isRepairable(item)) continue;
            this.setDurability(item, 0);
            ++repaired;
        }
        player.sendMessage(Text.color("&aRepaired &f" + repaired + " &aitems."));
    }

    private void feed(Player player) {
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        player.sendMessage(Text.color("&aYou feel full and satisfied."));
    }

    private void setMode(Player player, GameMode mode) {
        player.setGameMode(mode);
        player.sendMessage(Text.color("&aGame mode set to &f" + mode.name().toLowerCase() + "."));
    }

    private void toggleFly(Player player) {
        boolean enable = !player.getAllowFlight();
        player.setAllowFlight(enable);
        if (!enable && player.getGameMode() != GameMode.CREATIVE) {
            player.setFlying(false);
        }
        player.sendMessage(Text.color(enable ? "&aFlight enabled." : "&cFlight disabled."));
    }

    private void teleportTop(Player player) {
        org.bukkit.Location current = player.getLocation();
        int highestY = current.getWorld().getHighestBlockYAt(current, org.bukkit.HeightMap.MOTION_BLOCKING);
        org.bukkit.Location destination = new org.bukkit.Location(current.getWorld(), current.getBlockX() + 0.5, highestY + 1.0, current.getBlockZ() + 0.5, current.getYaw(), current.getPitch());
        player.teleport(destination);
        player.sendMessage(Text.color("&aTeleported to the top block."));
    }

    private boolean isRepairable(ItemStack item) {
        return item != null && item.getType().getMaxDurability() > 0 && item.hasItemMeta() && item.getItemMeta() instanceof Damageable;
    }

    private void setDurability(ItemStack item, int damage) {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable) {
            Damageable dmg = (Damageable)meta;
            dmg.setDamage(damage);
            item.setItemMeta(meta);
        }
    }
}
