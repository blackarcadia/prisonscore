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
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package org.axial.prisonsCore.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.axial.prisonsCore.service.GeneratorService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RefinedCommand
implements CommandExecutor,
TabCompleter {
    private final GeneratorService generatorService;

    public RefinedCommand(GeneratorService generatorService) {
        this.generatorService = generatorService;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player p;
        Player target = null;
        if (!sender.hasPermission("prisons.refined.give")) {
            sender.sendMessage(Text.color("&cNo permission."));
            return true;
        }
        if (args.length < 2 || !args[0].equalsIgnoreCase("give")) {
            sender.sendMessage(Text.color("&eUsage: /refined give <tier> [player]"));
            return true;
        }
        GeneratorService.RefinedTier tier = this.parseRefinedTier(args[1]);
        if (tier == null) {
            sender.sendMessage(Text.color("&cInvalid refined tier."));
            return true;
        }
        Object object = args.length >= 3 ? Bukkit.getPlayer((String)args[2]) : (target = sender instanceof Player ? (p = (Player)sender) : null);
        if (target == null) {
            sender.sendMessage(Text.color("&cPlayer not found."));
            return true;
        }
        target.getInventory().addItem(new ItemStack[]{this.generatorService.createRefinedItem(tier)});
        sender.sendMessage(Text.color("&aGave " + tier.name().toLowerCase(Locale.ROOT) + " refined ore to &f" + target.getName()));
        return true;
    }

    @Nullable
    private GeneratorService.RefinedTier parseRefinedTier(String input) {
        try {
            return GeneratorService.RefinedTier.valueOf(input.toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @NotNull
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("give");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            ArrayList<String> tiers = new ArrayList<String>();
            Arrays.stream(GeneratorService.RefinedTier.values()).forEach(t -> tiers.add(t.name().toLowerCase(Locale.ROOT)));
            return tiers;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return null;
        }
        return List.of();
    }
}

