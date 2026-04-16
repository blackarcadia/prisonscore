/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 */
package org.axial.prisonsCore.command;

import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.service.PlayerLevelService;
import org.axial.prisonsCore.service.SeasonService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LevelCapCommand implements CommandExecutor, TabCompleter {
    private final PlayerLevelService playerLevelService;
    private final PrisonsCore plugin;

    public LevelCapCommand(PrisonsCore plugin, PlayerLevelService playerLevelService) {
        this.plugin = plugin;
        this.playerLevelService = playerLevelService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Text.color("&cOnly players can open the level cap menu."));
                return true;
            }
            this.plugin.getLevelCapMenu().open((Player) sender);
            return true;
        }

        if (!sender.hasPermission("prisons.levelcap.admin")) {
            sender.sendMessage(Text.color("&cNo permission to manage the season."));
            return true;
        }

        if (args[0].equalsIgnoreCase("season")) {
            if (args.length < 2) {
                sender.sendMessage(Text.color("&eUsage: /levelcap season <start|stop>"));
                return true;
            }

            SeasonService seasonService = this.plugin.getSeasonService();
            if (args[1].equalsIgnoreCase("start")) {
                seasonService.startSeason();
                sender.sendMessage(Text.color("&aSeason started! Level cap will increase every 24h at midnight GMT."));
            } else if (args[1].equalsIgnoreCase("stop")) {
                seasonService.stopSeason();
                sender.sendMessage(Text.color("&cSeason stopped! Level cap is now disabled."));
            } else {
                sender.sendMessage(Text.color("&eUsage: /levelcap season <start|stop>"));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 2) {
                sender.sendMessage(Text.color("&eUsage: /levelcap set <level>"));
                return true;
            }
            try {
                int cap = Integer.parseInt(args[1]);
                cap = Math.max(1, Math.min(this.playerLevelService.getConfig().getMaxLevel(), cap));
                this.playerLevelService.setGlobalCap(cap);
                sender.sendMessage(Text.color("&aGlobal level cap manually set to " + cap + "."));
            } catch (NumberFormatException e) {
                sender.sendMessage(Text.color("&cInvalid level."));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("day")) {
            SeasonService seasonService = this.plugin.getSeasonService();
            if (args.length < 2) {
                sender.sendMessage(Text.color("&eUsage: /levelcap day <1-" + seasonService.getHighestDay() + "|clear>"));
                return true;
            }
            if (args[1].equalsIgnoreCase("clear")) {
                seasonService.clearManualDayOverride();
                sender.sendMessage(Text.color("&aManual level cap day override cleared."));
                return true;
            }
            try {
                int day = Integer.parseInt(args[1]);
                seasonService.setManualDayOverride(day);
                sender.sendMessage(Text.color("&aManual level cap day set to Day " + seasonService.getManualDayOverride() + "."));
            } catch (NumberFormatException e) {
                sender.sendMessage(Text.color("&cInvalid day."));
            }
            return true;
        }

        sender.sendMessage(Text.color("&eUsage: /levelcap [season <start|stop> | set <level> | day <1-12|clear>]"));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("prisons.levelcap.admin")) {
            return List.of();
        }

        if (args.length == 1) {
            return filter(List.of("season", "set", "day"), args[0]);
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("season")) {
                return filter(List.of("start", "stop"), args[1]);
            }
            if (args[0].equalsIgnoreCase("day")) {
                List<String> options = new ArrayList<>();
                options.add("clear");
                for (int day = 1; day <= this.plugin.getSeasonService().getHighestDay(); ++day) {
                    options.add(String.valueOf(day));
                }
                return filter(options, args[1]);
            }
        }

        return List.of();
    }

    private List<String> filter(List<String> options, String input) {
        String needle = input.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(needle)) {
                matches.add(option);
            }
        }
        return matches;
    }
}
