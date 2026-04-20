/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package org.axial.prisonsCore.command;

import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.service.ChargeOrbService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ChargeOrbCommand
implements CommandExecutor {
    private final ChargeOrbService chargeOrbService;
    private final PrisonsCore plugin;

    public ChargeOrbCommand(PrisonsCore plugin, ChargeOrbService chargeOrbService) {
        this.plugin = plugin;
        this.chargeOrbService = chargeOrbService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player target;
        if (args.length < 2 || args.length > 3 || !args[0].equalsIgnoreCase("give") && !args[0].equalsIgnoreCase("slot")) {
            sender.sendMessage(Text.color(command.getUsage()));
            return true;
        }
        if (!sender.hasPermission("prisons.chargeorb.give")) {
            sender.sendMessage(Text.color("&cYou do not have permission to use this command."));
            return true;
        }
        boolean slotToken = args[0].equalsIgnoreCase("slot");
        int percent = 0;
        if (!slotToken) {
            try {
                percent = Integer.parseInt(args[1]);
            }
            catch (NumberFormatException e) {
                sender.sendMessage(Text.color("&cInvalid percent."));
                return true;
            }
        }
        if (args.length == 3) {
            target = Bukkit.getPlayer((String)args[2]);
        } else if (sender instanceof Player) {
            Player p;
            target = p = (Player)sender;
        } else {
            sender.sendMessage(Text.color("&cSpecify a player: /" + label + " give <percent> <player>"));
            return true;
        }
        if (target == null || !target.isOnline()) {
            sender.sendMessage(Text.color("&cPlayer not found."));
            return true;
        }
        if (slotToken) {
            ItemStack token = this.chargeOrbService.createSlotToken();
            if (this.plugin.getStashService() != null) {
                this.plugin.getStashService().giveOrStash(target, token);
            } else {
                target.getInventory().addItem(new ItemStack[]{token}).values().forEach(stack -> target.getWorld().dropItemNaturally(target.getLocation(), stack));
            }
            sender.sendMessage(Text.color("&aGave a charge slot token to " + target.getName()));
        } else {
            ItemStack orb = this.chargeOrbService.createOrb(percent);
            if (this.plugin.getStashService() != null) {
                this.plugin.getStashService().giveOrStash(target, orb);
            } else {
                target.getInventory().addItem(new ItemStack[]{orb}).values().forEach(stack -> target.getWorld().dropItemNaturally(target.getLocation(), stack));
            }
            sender.sendMessage(Text.color("&aGave a " + percent + "% charge orb to " + target.getName()));
        }
        return true;
    }
}
