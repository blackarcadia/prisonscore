package org.axial.prisonsCore.command;

import org.axial.prisonsCore.service.PlayerLevelService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MiningTotalExpCommand
implements CommandExecutor {
    private final PlayerLevelService playerLevelService;

    public MiningTotalExpCommand(PlayerLevelService playerLevelService) {
        this.playerLevelService = playerLevelService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("prisons.miningtotalexp")) {
            sender.sendMessage(Text.color("&cNo permission."));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(Text.color("&cUsage: /" + label + " <set|add|remove|reset> <player> [amount]"));
            return true;
        }
        String action = args[0].toLowerCase();
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(Text.color("&cPlayer not found."));
            return true;
        }
        switch (action) {
            case "reset":
                this.playerLevelService.resetTotalExp(target);
                sender.sendMessage(Text.color("&aReset " + target.getName() + "'s mining total exp to 0."));
                return true;
            case "set":
            case "add":
            case "remove":
                if (args.length < 3) {
                    sender.sendMessage(Text.color("&cAmount required."));
                    return true;
                }
                long amount;
                try {
                    amount = Long.parseLong(args[2]);
                }
                catch (NumberFormatException e) {
                    sender.sendMessage(Text.color("&cInvalid amount."));
                    return true;
                }
                if (amount < 0L) {
                    sender.sendMessage(Text.color("&cAmount must be non-negative."));
                    return true;
                }
                long updated = this.playerLevelService.getTotalExp(target);
                if ("set".equals(action)) {
                    this.playerLevelService.setTotalExp(target, amount);
                    updated = this.playerLevelService.getTotalExp(target);
                } else if ("add".equals(action)) {
                    updated = this.playerLevelService.addTotalExp(target, amount);
                } else if ("remove".equals(action)) {
                    updated = this.playerLevelService.removeTotalExp(target, amount);
                }
                sender.sendMessage(Text.color("&aUpdated " + target.getName() + "'s mining total exp to &e" + updated + "&a."));
                return true;
            default:
                sender.sendMessage(Text.color("&cUsage: /" + label + " <set|add|remove|reset> <player> [amount]"));
                return true;
        }
    }
}
