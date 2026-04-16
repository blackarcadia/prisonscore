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
 */
package org.axial.prisonsCore.command;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.axial.prisonsCore.service.ReputationService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class ReputationCommand
implements CommandExecutor,
TabCompleter {
    private final ReputationService reputationService;

    public ReputationCommand(ReputationService reputationService) {
        this.reputationService = reputationService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub;
        if (!sender.hasPermission("prisons.reputation")) {
            sender.sendMessage(Text.color("&cNo permission."));
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(Text.color("&eUsage: /reputation get <player> | /reputation set <player> <stage>"));
            return true;
        }
        switch (sub = args[0].toLowerCase(Locale.ROOT)) {
            case "get": {
                if (args.length < 2) {
                    sender.sendMessage(Text.color("&cSpecify a player."));
                    return true;
                }
                Player target = Bukkit.getPlayer((String)args[1]);
                if (target == null) {
                    sender.sendMessage(Text.color("&cPlayer not found or offline."));
                    return true;
                }
                ReputationService.Stage stage = this.reputationService.getStage(target.getUniqueId());
                sender.sendMessage(Text.color("&a" + target.getName() + " reputation: &f" + stage.name()));
                break;
            }
            case "set": {
                if (args.length < 3) {
                    sender.sendMessage(Text.color("&cUsage: /reputation set <player> <stage>"));
                    return true;
                }
                Player target = Bukkit.getPlayer((String)args[1]);
                if (target == null) {
                    sender.sendMessage(Text.color("&cPlayer not found or offline."));
                    return true;
                }
                ReputationService.Stage stage = this.parseStage(args[2]);
                if (stage == null) {
                    sender.sendMessage(Text.color("&cInvalid stage. Options: NEUTRAL, WARNED, WATCHED, WANTED"));
                    return true;
                }
                this.reputationService.setStage(target.getUniqueId(), stage);
                sender.sendMessage(Text.color("&aSet " + target.getName() + " to " + stage.name()));
                target.sendMessage(Text.color("&eYour reputation has been set to &f" + stage.name() + " &eby an admin."));
                break;
            }
            default: {
                sender.sendMessage(Text.color("&cUnknown subcommand."));
            }
        }
        return true;
    }

    private ReputationService.Stage parseStage(String input) {
        try {
            return ReputationService.Stage.valueOf(input.toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("prisons.reputation")) {
            return List.of();
        }
        if (args.length == 1) {
            return List.of("get", "set").stream().filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT))).collect(Collectors.toList());
        }
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n -> n.toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT))).collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            return Arrays.stream(ReputationService.Stage.values()).map(Enum::name).filter(n -> n.toLowerCase(Locale.ROOT).startsWith(args[2].toLowerCase(Locale.ROOT))).collect(Collectors.toList());
        }
        return List.of();
    }
}

