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

import java.util.List;
import java.util.UUID;
import org.axial.prisonsCore.service.EconomyService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BalTopCommand
implements CommandExecutor {
    private final EconomyService economyService;

    public BalTopCommand(EconomyService economyService) {
        this.economyService = economyService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int limit = 10;
        List<EconomyService.TopEntry> top = this.economyService.getTopBalances(limit);
        sender.sendMessage(Text.color("&6&lTop Balances"));
        int rank = 1;
        for (EconomyService.TopEntry entry : top) {
            OfflinePlayer op = Bukkit.getOfflinePlayer((UUID)entry.playerId());
            String name = op != null && op.getName() != null ? op.getName() : entry.playerId().toString().substring(0, 8);
            sender.sendMessage(Text.color("&e" + rank + ". &f" + name + " &7- &2" + this.economyService.format(entry.balance())));
            ++rank;
        }
        if (top.isEmpty()) {
            sender.sendMessage(Text.color("&7No balances recorded."));
        }
        return true;
    }
}

