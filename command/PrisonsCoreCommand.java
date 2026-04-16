package org.axial.prisonsCore.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.axial.prisonsCore.service.FeatureToggleService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PrisonsCoreCommand implements CommandExecutor, TabCompleter {
    private final FeatureToggleService featureToggleService;

    public PrisonsCoreCommand(FeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("prisons.core")) {
            sender.sendMessage(Text.color("&cNo permission."));
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(Text.color("&eUsage: /prisonscore <enable|disable> <feature>"));
            return true;
        }
        boolean enabled;
        if (args[0].equalsIgnoreCase("enable")) {
            enabled = true;
        } else if (args[0].equalsIgnoreCase("disable")) {
            enabled = false;
        } else {
            sender.sendMessage(Text.color("&eUsage: /prisonscore <enable|disable> <feature>"));
            return true;
        }
        FeatureToggleService.Feature feature = this.featureToggleService.resolve(args[1]);
        if (feature == null) {
            sender.sendMessage(Text.color("&cUnknown feature."));
            return true;
        }
        this.featureToggleService.setEnabled(feature, enabled);
        sender.sendMessage(Text.color((enabled ? "&aEnabled " : "&cDisabled ") + feature.messageName() + "&7."));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return this.filter(List.of("enable", "disable"), args[0]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("disable"))) {
            return this.filter(this.featureToggleService.featureNames(), args[1]);
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
