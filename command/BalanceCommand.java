/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package org.axial.prisonsCore.command;

import org.axial.prisonsCore.service.EconomyService;
import org.axial.prisonsCore.util.Lang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand
implements CommandExecutor {
    private final EconomyService economyService;

    public BalanceCommand(EconomyService economyService) {
        this.economyService = economyService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Lang.msg("common.players-only", "Only players can use this command."));
            return true;
        }
        Player player = (Player)sender;
        double bal = this.economyService.getBalance(player);
        player.sendMessage(Lang.msg("economy.balance", "&aBalance: &2{amount}").replace("{amount}", this.economyService.format(bal)));
        return true;
    }
}

