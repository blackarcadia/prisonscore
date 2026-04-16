/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package org.axial.prisonsCore.command;

import org.axial.prisonsCore.service.EconomyService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WorthCommand
implements CommandExecutor {
    private final EconomyService economyService;

    public WorthCommand(EconomyService economyService) {
        this.economyService = economyService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Material mat;
        if (!sender.hasPermission("prisons.worth.set")) {
            sender.sendMessage(Text.color("&cYou do not have permission."));
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(Text.color("&cUsage: /" + label + " <material|hand> <price>"));
            return true;
        }
        if (args[0].equalsIgnoreCase("hand") && sender instanceof Player) {
            Player p = (Player)sender;
            mat = p.getInventory().getItemInMainHand().getType();
        } else {
            mat = Material.matchMaterial((String)args[0]);
        }
        if (mat == null || mat == Material.AIR) {
            sender.sendMessage(Text.color("&cUnknown material."));
            return true;
        }
        Double parsed = this.economyService.parseAmount(args[1]);
        if (parsed == null) {
            sender.sendMessage(Text.color("&cInvalid price."));
            return true;
        }
        double price = parsed;
        if (price < 0.0) {
            sender.sendMessage(Text.color("&cPrice must be non-negative."));
            return true;
        }
        this.economyService.setWorth(mat, price);
        sender.sendMessage(Text.color("&aSet worth of " + mat.name() + " to &2" + this.economyService.format(price)));
        return true;
    }
}

