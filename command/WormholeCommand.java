/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package org.axial.prisonsCore.command;

import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.listener.WormholeEnchanter;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WormholeCommand
implements CommandExecutor {
    private final WormholeEnchanter wormhole;

    public WormholeCommand(PrisonsCore plugin, WormholeEnchanter wormhole) {
        this.wormhole = wormhole;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub;
        if (!sender.hasPermission("prisons.wormhole")) {
            sender.sendMessage(Text.color("&cYou don't have permission."));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(Text.color("&eUsage: /wormhole <on|info|setcenter|setradius|setoptioncount|setheight|reload>"));
            return true;
        }
        switch (sub = args[0].toLowerCase()) {
            case "on": {
                this.wormhole.setEnabled(true);
                sender.sendMessage(Text.color("&aWormhole enchanter enabled."));
                break;
            }
            case "setcenter": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Text.color("&cOnly players can use this."));
                    return true;
                }
                Player player = (Player)sender;
                Location l = player.getLocation();
                this.wormhole.setCenter(l);
                sender.sendMessage(Text.color("&aWormhole center set to &f" + l.getWorld().getName() + " " + String.format("%.2f %.2f %.2f", l.getX(), l.getY(), l.getZ())));
                break;
            }
            case "setradius": {
                double r;
                if (args.length < 2) {
                    sender.sendMessage(Text.color("&cUsage: /wormhole setradius <number>"));
                    return true;
                }
                try {
                    r = Double.parseDouble(args[1]);
                }
                catch (NumberFormatException ex) {
                    sender.sendMessage(Text.color("&cInvalid radius."));
                    return true;
                }
                this.wormhole.setRadius(r);
                sender.sendMessage(Text.color("&aWormhole radius set to &f" + r));
                break;
            }
            case "setoptioncount": {
                int count;
                if (args.length < 2) {
                    sender.sendMessage(Text.color("&cUsage: /wormhole setoptioncount <1-5>"));
                    return true;
                }
                try {
                    count = Integer.parseInt(args[1]);
                }
                catch (NumberFormatException ex) {
                    sender.sendMessage(Text.color("&cInvalid number."));
                    return true;
                }
                count = Math.max(1, Math.min(5, count));
                this.wormhole.setOptionCount(count);
                sender.sendMessage(Text.color("&aOption count set to &f" + count));
                break;
            }
            case "setheight": {
                double h;
                if (args.length < 2) {
                    sender.sendMessage(Text.color("&cUsage: /wormhole setheight <number>"));
                    return true;
                }
                try {
                    h = Double.parseDouble(args[1]);
                }
                catch (NumberFormatException ex) {
                    sender.sendMessage(Text.color("&cInvalid height."));
                    return true;
                }
                this.wormhole.setDisplayHeight(h);
                sender.sendMessage(Text.color("&aDisplay height set to &f" + h));
                break;
            }
            case "info": {
                sender.sendMessage(Text.color(this.wormhole.describe()));
                break;
            }
            case "reload": {
                this.wormhole.reload();
                sender.sendMessage(Text.color("&aWormhole settings reloaded from config."));
                break;
            }
            default: {
                sender.sendMessage(Text.color("&eUsage: /wormhole <on|info|setcenter|setradius|setoptioncount|setheight|reload>"));
            }
        }
        return true;
    }
}
