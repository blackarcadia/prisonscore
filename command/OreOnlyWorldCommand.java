package org.axial.prisonsCore.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.axial.prisonsCore.service.OreOnlyWorldService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OreOnlyWorldCommand implements CommandExecutor, TabCompleter {
    private final OreOnlyWorldService oreOnlyWorldService;

    public OreOnlyWorldCommand(OreOnlyWorldService oreOnlyWorldService) {
        this.oreOnlyWorldService = oreOnlyWorldService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("prisons.oreonly")) {
            sender.sendMessage(Text.color("&cNo permission."));
            return true;
        }
        if (args.length == 0) {
            this.sendUsage(sender);
            return true;
        }
        if (args[0].equalsIgnoreCase("list")) {
            List<String> worlds = this.oreOnlyWorldService.getWorldNames();
            if (worlds.isEmpty()) {
                sender.sendMessage(Text.color("&eNo worlds currently have ore-only mining enabled."));
                return true;
            }
            sender.sendMessage(Text.color("&aOre-only worlds: &f" + String.join("&7, &f", worlds)));
            return true;
        }
        World world = this.resolveWorld(sender, args);
        if (world == null) {
            return true;
        }
        if (args[0].equalsIgnoreCase("add")) {
            if (!this.oreOnlyWorldService.addWorld(world.getName())) {
                sender.sendMessage(Text.color("&eOre-only mining is already enabled in &f" + world.getName() + "&e."));
                return true;
            }
            sender.sendMessage(Text.color("&aOnly ores can now be mined in &f" + world.getName() + "&a."));
            return true;
        }
        if (args[0].equalsIgnoreCase("remove")) {
            if (!this.oreOnlyWorldService.removeWorld(world.getName())) {
                sender.sendMessage(Text.color("&eOre-only mining is not enabled in &f" + world.getName() + "&e."));
                return true;
            }
            sender.sendMessage(Text.color("&aOre-only mining disabled in &f" + world.getName() + "&a."));
            return true;
        }
        this.sendUsage(sender);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return this.filter(List.of("add", "remove", "list"), args[0]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))) {
            List<String> worlds = new ArrayList<String>();
            for (World world : Bukkit.getWorlds()) {
                worlds.add(world.getName());
            }
            return this.filter(worlds, args[1]);
        }
        return List.of();
    }

    private World resolveWorld(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            World world = Bukkit.getWorld(args[1]);
            if (world == null) {
                sender.sendMessage(Text.color("&cWorld not found."));
            }
            return world;
        }
        if (sender instanceof Player player) {
            return player.getWorld();
        }
        sender.sendMessage(Text.color("&cConsole must specify a world."));
        return null;
    }

    private List<String> filter(List<String> options, String input) {
        String needle = input.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<String>();
        for (String option : options) {
            if (!option.toLowerCase(Locale.ROOT).startsWith(needle)) {
                continue;
            }
            matches.add(option);
        }
        return matches;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Text.color("&eUsage: /oreonly add [world]"));
        sender.sendMessage(Text.color("&eUsage: /oreonly remove [world]"));
        sender.sendMessage(Text.color("&eUsage: /oreonly list"));
    }
}
