/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package org.axial.prisonsCore.command;

import java.util.ArrayList;
import java.util.List;
import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.service.EnergyItemService;
import org.axial.prisonsCore.util.Lang;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EnergyCommand
implements CommandExecutor,
TabCompleter {
    private final EnergyItemService energyItemService;
    private final PrisonsCore plugin;

    public EnergyCommand(PrisonsCore plugin, EnergyItemService energyItemService) {
        this.energyItemService = energyItemService;
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Lang.msg("energy.usage", "&cUsage: /energy give <amount> <player> | /energy extractor <player> [amount]"));
            return true;
        }
        if (args[0].equalsIgnoreCase("give")) {
            int amount;
            if (args.length != 3) {
                sender.sendMessage(Lang.msg("energy.usage", "&cUsage: /energy give <amount> <player>"));
                return true;
            }
            try {
                amount = Integer.parseInt(args[1]);
            }
            catch (NumberFormatException e) {
                sender.sendMessage(Lang.msg("energy.invalid-amount", "&cInvalid amount."));
                return true;
            }
            if (amount <= 0) {
                sender.sendMessage(Lang.msg("energy.invalid-amount", "&cAmount must be positive."));
                return true;
            }
            Player target = Bukkit.getPlayerExact((String)args[2]);
            if (target == null) {
                sender.sendMessage(Lang.msg("common.player-not-found", "&cPlayer not found."));
                return true;
            }
            this.energyItemService.giveEnergyItem(target, amount);
            sender.sendMessage(Lang.msg("energy.given", "&aGave {amount} charge to {player}").replace("{amount}", String.valueOf(amount)).replace("{player}", target.getName()));
            return true;
        }
        if (args[0].equalsIgnoreCase("extractor")) {
            Player target;
            String playerName;
            if (args.length < 2) {
                sender.sendMessage(Lang.msg("energy.usage-extractor", "&cUsage: /energy extractor <player> [amount]"));
                return true;
            }
            int count = 1;
            if (args.length == 3) {
                try {
                    count = Integer.parseInt(args[2]);
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
                playerName = args[1];
            } else {
                playerName = args[1];
            }
            if (count <= 0) {
                count = 1;
            }
            if ((target = Bukkit.getPlayerExact((String)playerName)) == null) {
                sender.sendMessage(Lang.msg("common.player-not-found", "&cPlayer not found."));
                return true;
            }
            ItemStack item = this.energyItemService.createExtractorItem();
            item.setAmount(count);
            target.getInventory().addItem(new ItemStack[]{item});
            sender.sendMessage(Lang.msg("energy.extractor.given", "&aGave {count} charge extractor(s) to {player}").replace("{count}", String.valueOf(count)).replace("{player}", target.getName()));
            return true;
        }
        sender.sendMessage(Lang.msg("energy.usage", "&cUsage: /energy give <amount> <player> | /energy extractor <player> [amount]"));
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> list;
        block3: {
            block4: {
                block2: {
                    list = new ArrayList<String>();
                    if (args.length != 1) break block2;
                    list.add("give");
                    list.add("extractor");
                    break block3;
                }
                if (args.length != 3 || !args[0].equalsIgnoreCase("give")) break block4;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    list.add(p.getName());
                }
                break block3;
            }
            if (args.length != 2 || !args[0].equalsIgnoreCase("extractor")) break block3;
            for (Player p : Bukkit.getOnlinePlayers()) {
                list.add(p.getName());
            }
        }
        return list;
    }
}
