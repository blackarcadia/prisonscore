package org.axial.prisonsCore.command;

import java.util.ArrayList;
import java.util.List;
import org.axial.prisonsCore.service.KitService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class ResetKitCooldownCommand implements CommandExecutor, TabCompleter {
    private final KitService kitService;

    public ResetKitCooldownCommand(KitService kitService) {
        this.kitService = kitService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Text.color("&cUsage: /resetkitcooldown <player> [kit|all]"));
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            sender.sendMessage(Text.color("&cPlayer not found."));
            return true;
        }
        KitService.KitType kitType = null;
        String mode = args.length >= 2 ? args[1] : "all";
        if (!mode.equalsIgnoreCase("all")) {
            kitType = this.kitService.parseKit(mode);
            if (kitType == null) {
                sender.sendMessage(Text.color("&cInvalid kit. Use starter, beta, or all."));
                return true;
            }
        }
        this.kitService.resetCooldowns(target, kitType);
        if (kitType == null) {
            sender.sendMessage(Text.color("&aReset all kit cooldowns for &f" + target.getName() + "&a."));
            if (target.isOnline() && target.getPlayer() != null) {
                target.getPlayer().sendMessage(Text.color("&aYour kit cooldowns have been reset."));
            }
            return true;
        }
        sender.sendMessage(Text.color("&aReset the &f" + kitType.plainName() + " &akit cooldown for &f" + target.getName() + "&a."));
        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(Text.color("&aYour " + kitType.plainName() + " kit cooldown has been reset."));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            ArrayList<String> completions = new ArrayList<String>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().regionMatches(true, 0, args[0], 0, args[0].length())) {
                    completions.add(player.getName());
                }
            }
            return completions;
        }
        if (args.length == 2) {
            ArrayList<String> completions = new ArrayList<String>();
            for (String option : List.of("all", "starter", "beta")) {
                if (option.regionMatches(true, 0, args[1], 0, args[1].length())) {
                    completions.add(option);
                }
            }
            return completions;
        }
        return List.of();
    }
}
