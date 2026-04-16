package org.axial.prisonsCore.command;

import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InvseeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player viewer)) {
            sender.sendMessage(Text.color("&cOnly players can use this."));
            return true;
        }
        if (args.length != 1) {
            viewer.sendMessage(Text.color("&cUsage: /invsee <player>"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            viewer.sendMessage(Text.color("&cThat player is not online."));
            return true;
        }
        viewer.openInventory(target.getInventory());
        viewer.sendMessage(Text.color("&aViewing &f" + target.getName() + "&a's inventory."));
        return true;
    }
}
