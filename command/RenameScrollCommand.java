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
import org.axial.prisonsCore.service.RenameScrollService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RenameScrollCommand
implements CommandExecutor,
TabCompleter {
    private final RenameScrollService renameScrollService;

    public RenameScrollCommand(RenameScrollService renameScrollService) {
        this.renameScrollService = renameScrollService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2 || !args[0].equalsIgnoreCase("give")) {
            sender.sendMessage(Text.color("&cUsage: /" + label + " give <player> [amount]"));
            return true;
        }
        Player target = Bukkit.getPlayerExact((String)args[1]);
        if (target == null) {
            sender.sendMessage(Text.color("&cPlayer not found."));
            return true;
        }
        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        if (amount <= 0) {
            amount = 1;
        }
        ItemStack scroll = this.renameScrollService.createScroll();
        scroll.setAmount(amount);
        target.getInventory().addItem(new ItemStack[]{scroll});
        sender.sendMessage(Text.color("&aGave &f" + amount + " &aRename Scroll(s) to &f" + target.getName() + "&a."));
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> list = new ArrayList<String>();
        if (args.length == 1) {
            list.add("give");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                list.add(p.getName());
            }
        }
        return list;
    }
}

