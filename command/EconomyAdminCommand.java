/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 */
package org.axial.prisonsCore.command;

import org.axial.prisonsCore.service.EconomyService;
import org.axial.prisonsCore.util.Lang;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EconomyAdminCommand
implements CommandExecutor {
    private final EconomyService economyService;

    public EconomyAdminCommand(EconomyService economyService) {
        this.economyService = economyService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("prisons.economy.admin")) {
            sender.sendMessage(Text.color("&cNo permission."));
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(Text.color("&cUsage: /" + label + " <add|set|remove|reset> <player> [amount]"));
            return true;
        }
        String action = args[0].toLowerCase();
        OfflinePlayer target = Bukkit.getOfflinePlayer((String)args[1]);
        if (target == null) {
            sender.sendMessage(Text.color("&cPlayer not found."));
            return true;
        }
        switch (action) {
            case "reset": {
                this.economyService.setBalance(target.getUniqueId(), this.economyService.getDefaultBalance());
                sender.sendMessage(Text.color("&aReset " + target.getName() + " to " + this.economyService.format(this.economyService.getDefaultBalance())));
                return true;
            }
            case "add": 
            case "set": 
            case "remove": {
                double current;
                if (args.length < 3) {
                    sender.sendMessage(Text.color("&cAmount required."));
                    return true;
                }
                Double parsed = this.economyService.parseAmount(args[2]);
                if (parsed == null) {
                    sender.sendMessage(Text.color("&cInvalid amount."));
                    return true;
                }
                double amount = parsed;
                if (amount < 0.0) {
                    sender.sendMessage(Text.color("&cAmount must be non-negative."));
                    return true;
                }
                double newBal = current = this.economyService.getBalance(target.getUniqueId());
                switch (action) {
                    case "add": {
                        newBal = current + amount;
                        break;
                    }
                    case "remove": {
                        newBal = Math.max(0.0, current - amount);
                        break;
                    }
                    case "set": {
                        newBal = amount;
                    }
                }
                this.economyService.setBalance(target.getUniqueId(), newBal);
                sender.sendMessage(Lang.msg("economy.admin." + action, "&aUpdated balance.").replace("{player}", target.getName()).replace("{amount}", this.economyService.format(amount)).replace("{balance}", this.economyService.format(newBal)));
                return true;
            }
        }
        sender.sendMessage(Text.color("&cUnknown action."));
        return true;
    }
}

