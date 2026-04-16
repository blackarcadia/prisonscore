package org.axial.prisonsCore.hologram.command;

import org.axial.prisonsCore.hologram.Hologram;
import org.axial.prisonsCore.hologram.HologramService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HologramCommand implements CommandExecutor, TabCompleter {
    private final HologramService hologramService;

    public HologramCommand(HologramService hologramService) {
        this.hologramService = hologramService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Text.color("&cOnly players can use this command."));
            return true;
        }

        if (!player.hasPermission("prisons.holograms")) {
            player.sendMessage(Text.color("&cNo permission."));
            return true;
        }

        if (args.length < 1) {
            sendUsage(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "create" -> {
                if (args.length < 3) {
                    player.sendMessage(Text.color("&cUsage: /hologram create <id> <text>"));
                    return true;
                }
                String id = args[1];
                if (hologramService.getHologram(id) != null) {
                    player.sendMessage(Text.color("&cA hologram with that ID already exists."));
                    return true;
                }
                String content = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                List<String> lines = new ArrayList<>(List.of(content.split("\\\\n")));
                hologramService.createHologram(id, player.getLocation(), lines);
                player.sendMessage(Text.color("&aHologram created."));
            }
            case "remove" -> {
                if (args.length < 2) {
                    player.sendMessage(Text.color("&cUsage: /hologram remove <id>"));
                    return true;
                }
                String id = args[1];
                if (hologramService.getHologram(id) == null) {
                    player.sendMessage(Text.color("&cUnknown hologram ID."));
                    return true;
                }
                hologramService.removeHologram(id);
                player.sendMessage(Text.color("&aHologram removed."));
            }
            case "edit" -> {
                if (args.length < 3) {
                    player.sendMessage(Text.color("&cUsage: /hologram edit <id> <new text>"));
                    return true;
                }
                String id = args[1];
                if (hologramService.getHologram(id) == null) {
                    player.sendMessage(Text.color("&cUnknown hologram ID."));
                    return true;
                }
                String content = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                List<String> lines = new ArrayList<>(List.of(content.split("\\\\n")));
                hologramService.updateHologram(id, lines);
                player.sendMessage(Text.color("&aHologram updated."));
            }
            case "addline" -> {
                if (args.length < 3) {
                    player.sendMessage(Text.color("&cUsage: /hologram addline <id> <text>"));
                    return true;
                }
                String id = args[1];
                if (hologramService.getHologram(id) == null) {
                    player.sendMessage(Text.color("&cUnknown hologram ID."));
                    return true;
                }
                String line = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                hologramService.addLine(id, line);
                player.sendMessage(Text.color("&aLine added to hologram."));
            }
            case "removeline" -> {
                if (args.length < 3) {
                    player.sendMessage(Text.color("&cUsage: /hologram removeline <id> <index>"));
                    return true;
                }
                String id = args[1];
                Hologram holo = hologramService.getHologram(id);
                if (holo == null) {
                    player.sendMessage(Text.color("&cUnknown hologram ID."));
                    return true;
                }
                try {
                    int index = Integer.parseInt(args[2]);
                    if (index < 0 || index >= holo.getLines().size()) {
                        player.sendMessage(Text.color("&cInvalid line index (0 to " + (holo.getLines().size() - 1) + ")."));
                        return true;
                    }
                    hologramService.removeLine(id, index);
                    player.sendMessage(Text.color("&aLine removed from hologram."));
                } catch (NumberFormatException e) {
                    player.sendMessage(Text.color("&cIndex must be a number."));
                }
            }
            case "list" -> {
                player.sendMessage(Text.color("&d&lHolograms:"));
                hologramService.getHolograms().forEach(h -> 
                    player.sendMessage(Text.color("&8- &f" + h.getId() + " &7(" + h.getLocation().getWorld().getName() + ", " + h.getLocation().getBlockX() + ", " + h.getLocation().getBlockY() + ", " + h.getLocation().getBlockZ() + ")"))
                );
            }
            case "teleport", "tp" -> {
                if (args.length < 2) {
                    player.sendMessage(Text.color("&cUsage: /hologram tp <id>"));
                    return true;
                }
                String id = args[1];
                Hologram holo = hologramService.getHologram(id);
                if (holo == null) {
                    player.sendMessage(Text.color("&cUnknown hologram ID."));
                    return true;
                }
                player.teleport(holo.getLocation());
                player.sendMessage(Text.color("&aTeleported to hologram."));
            }
            default -> sendUsage(player);
        }

        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage(Text.color("&d&lHologram Commands:"));
        player.sendMessage(Text.color("&f/hologram create <id> <text> &7- Create a hologram (use \\n for lines)"));
        player.sendMessage(Text.color("&f/hologram remove <id> &7- Remove a hologram"));
        player.sendMessage(Text.color("&f/hologram edit <id> <text> &7- Edit hologram text"));
        player.sendMessage(Text.color("&f/hologram addline <id> <text> &7- Add a line to a hologram"));
        player.sendMessage(Text.color("&f/hologram removeline <id> <index> &7- Remove a line from a hologram"));
        player.sendMessage(Text.color("&f/hologram list &7- List all holograms"));
        player.sendMessage(Text.color("&f/hologram tp <id> &7- Teleport to a hologram"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("create", "remove", "edit", "addline", "removeline", "list", "tp").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && !args[0].equalsIgnoreCase("create") && !args[0].equalsIgnoreCase("list")) {
            return hologramService.getHolograms().stream()
                    .map(Hologram::getId)
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
