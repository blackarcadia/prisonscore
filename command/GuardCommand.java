/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 */
package org.axial.prisonsCore.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.axial.prisonsCore.service.GuardService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class GuardCommand
implements CommandExecutor,
TabCompleter {
    private final GuardService guardService;

    public GuardCommand(GuardService guardService) {
        this.guardService = guardService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("prisons.guard")) {
            sender.sendMessage(Text.color("&cNo permission."));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(Text.color("&eUsage: /guard spawn <id> [name]"));
            sender.sendMessage(Text.color("&e       /guard reload"));
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload": {
                this.guardService.reload();
                sender.sendMessage(Text.color("&aGuards config reloaded."));
                return true;
            }
            case "spawn": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can spawn guards.");
                    return true;
                }
                Player player = (Player)sender;
                if (args.length < 2) {
                    sender.sendMessage(Text.color("&cSpecify guard id."));
                    return true;
                }
                GuardService.GuardTemplate template = this.guardService.getTemplate(args[1]);
                if (template == null) {
                    sender.sendMessage(Text.color("&cUnknown guard id."));
                    return true;
                }
                String name = args.length >= 3 ? String.join((CharSequence)" ", Arrays.copyOfRange(args, 2, args.length)) : null;
                Entity npc = this.guardService.spawnGuard(template, player.getLocation(), name, player);
                if (npc != null) {
                    sender.sendMessage(Text.color("&aSpawned guard &f" + template.id()));
                }
                return true;
            }
        }
        sender.sendMessage(Text.color("&cUnknown subcommand."));
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("prisons.guard")) {
            return List.of();
        }
        if (args.length == 1) {
            return List.of("spawn", "reload").stream().filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT))).collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
            return this.guardService.ids().stream().filter(id -> id.startsWith(args[1].toLowerCase(Locale.ROOT))).collect(Collectors.toList());
        }
        return new ArrayList<String>();
    }
}

