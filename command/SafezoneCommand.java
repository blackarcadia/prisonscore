package org.axial.prisonsCore.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.axial.prisonsCore.service.SafezoneService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class SafezoneCommand implements CommandExecutor, TabCompleter {
    private final SafezoneService safezoneService;

    public SafezoneCommand(SafezoneService safezoneService) {
        this.safezoneService = safezoneService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("prisons.safezone.admin")) {
            sender.sendMessage(Text.color("&cNo permission."));
            return true;
        }
        if (args.length == 0) {
            this.sendUsage(sender, label);
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "wand":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Text.color("&cOnly players can receive the safezone wand."));
                    return true;
                }
                player.getInventory().addItem(this.safezoneService.createWand());
                player.sendMessage(Text.color("&aGiven a safezone wand. Left click = pos1, right click = pos2."));
                return true;
            case "pos1":
                if (!(sender instanceof Player playerPos1)) {
                    sender.sendMessage(Text.color("&cOnly players can set positions."));
                    return true;
                }
                this.safezoneService.setPos1(playerPos1.getUniqueId(), playerPos1.getLocation());
                sender.sendMessage(Text.color("&aSet safezone pos1 at your location."));
                return true;
            case "pos2":
                if (!(sender instanceof Player playerPos2)) {
                    sender.sendMessage(Text.color("&cOnly players can set positions."));
                    return true;
                }
                this.safezoneService.setPos2(playerPos2.getUniqueId(), playerPos2.getLocation());
                sender.sendMessage(Text.color("&aSet safezone pos2 at your location."));
                return true;
            case "create":
                if (!(sender instanceof Player playerCreate)) {
                    sender.sendMessage(Text.color("&cOnly players can create safezones."));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(Text.color("&cUsage: /" + label + " create <id>"));
                    return true;
                }
                SafezoneService.Selection selection = this.safezoneService.getSelection(playerCreate.getUniqueId());
                if (!selection.isComplete()) {
                    sender.sendMessage(Text.color("&cSet pos1 and pos2 first."));
                    return true;
                }
                if (this.safezoneService.createSafezone(args[1], selection)) {
                    sender.sendMessage(Text.color("&aCreated safezone &f" + args[1] + "&a."));
                } else {
                    sender.sendMessage(Text.color("&cFailed to create safezone."));
                }
                return true;
            case "delete":
            case "remove":
                if (args.length < 2) {
                    sender.sendMessage(Text.color("&cUsage: /" + label + " delete <id>"));
                    return true;
                }
                if (this.safezoneService.deleteSafezone(args[1])) {
                    sender.sendMessage(Text.color("&aDeleted safezone &f" + args[1] + "&a."));
                } else {
                    sender.sendMessage(Text.color("&cSafezone not found."));
                }
                return true;
            case "list":
                if (this.safezoneService.getSafezones().isEmpty()) {
                    sender.sendMessage(Text.color("&7No safezones created."));
                    return true;
                }
                sender.sendMessage(Text.color("&bSafezones: &f" + this.safezoneService.getSafezones().stream()
                        .map(SafezoneService.Safezone::id)
                        .collect(Collectors.joining(", "))));
                return true;
            default:
                this.sendUsage(sender, label);
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("prisons.safezone.admin")) {
            return List.of();
        }
        if (args.length == 1) {
            return this.filter(List.of("wand", "pos1", "pos2", "create", "delete", "list"), args[0]);
        }
        if (args.length == 2 && ("delete".equalsIgnoreCase(args[0]) || "remove".equalsIgnoreCase(args[0]))) {
            return this.filter(this.safezoneService.getSafezones().stream().map(SafezoneService.Safezone::id).collect(Collectors.toList()), args[1]);
        }
        return List.of();
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(Text.color("&e/" + label + " wand"));
        sender.sendMessage(Text.color("&e/" + label + " pos1"));
        sender.sendMessage(Text.color("&e/" + label + " pos2"));
        sender.sendMessage(Text.color("&e/" + label + " create <id>"));
        sender.sendMessage(Text.color("&e/" + label + " delete <id>"));
        sender.sendMessage(Text.color("&e/" + label + " list"));
    }

    private List<String> filter(List<String> input, String partial) {
        String normalized = partial == null ? "" : partial.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String value : input) {
            if (value.toLowerCase(Locale.ROOT).startsWith(normalized)) {
                result.add(value);
            }
        }
        return result;
    }
}
