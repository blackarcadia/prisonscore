/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package org.axial.prisonsCore.command;

import org.axial.prisonsCore.service.EconomyService;
import org.axial.prisonsCore.util.Lang;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PayCommand
implements CommandExecutor {
    private final EconomyService economyService;

    public PayCommand(EconomyService economyService) {
        this.economyService = economyService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Lang.msg("common.players-only", "Only players can use this command."));
            return true;
        }
        Player player = (Player)sender;
        if (args.length != 2) {
            sender.sendMessage(Lang.msg("economy.pay-usage", "&cUsage: /" + label + " <player> <amount>"));
            return true;
        }
        Player target = Bukkit.getPlayerExact((String)args[0]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(Lang.msg("economy.pay-no-player", "&cPlayer not found."));
            return true;
        }
        if (target.equals((Object)player)) {
            sender.sendMessage(Lang.msg("economy.pay-self", "&cYou cannot pay yourself."));
            return true;
        }
        Double parsed = this.economyService.parseAmount(args[1]);
        if (parsed == null) {
            sender.sendMessage(Lang.msg("economy.pay-invalid", "&cInvalid amount."));
            return true;
        }
        double amount = parsed;
        if (amount <= 0.0) {
            sender.sendMessage(Lang.msg("economy.pay-invalid", "&cAmount must be positive."));
            return true;
        }
        if (!this.economyService.withdraw(player, amount)) {
            sender.sendMessage(Lang.msg("economy.pay-not-enough", "&cYou don't have enough money."));
            return true;
        }
        this.economyService.deposit(target, amount);
        player.sendMessage(Lang.msg("economy.pay-sent", "&aPaid {player} &2{amount}").replace("{player}", target.getName()).replace("{amount}", this.economyService.format(amount)));
        target.sendMessage(Lang.msg("economy.pay-received", "&aYou received &2{amount} &afrom {player}").replace("{amount}", this.economyService.format(amount)).replace("{player}", player.getName()));
        return true;
    }
}

