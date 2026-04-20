package org.axial.prisonsCore.command;

import org.axial.prisonsCore.service.GlobalBoosterService;
import org.axial.prisonsCore.service.GlobalBoosterService.BoosterType;
import org.axial.prisonsCore.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GlobalBoosterCommand implements CommandExecutor {
    private static final String PERMISSION = "prisons.globalbooster.admin";
    private final GlobalBoosterService boosterService;

    public GlobalBoosterCommand(GlobalBoosterService boosterService) {
        this.boosterService = boosterService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(Text.color("&cYou do not have permission to use this command."));
            return true;
        }
        if (args.length == 0) {
            this.sendUsage(sender, label);
            return true;
        }

        String action = args[0].toLowerCase();
        switch (action) {
            case "set" -> {
                if (args.length < 3) {
                    this.sendUsage(sender, label);
                    return true;
                }
                BoosterType type = BoosterType.parse(args[1]);
                Double multiplier = parseDouble(args[2]);
                if (type == null || multiplier == null || multiplier <= 0.0) {
                    sender.sendMessage(Text.color("&cUsage: /" + label + " set <xp|money|charge|shard> <multiplier>"));
                    return true;
                }
                this.boosterService.setMultiplier(type, multiplier);
                sender.sendMessage(Text.color("&aSet &f" + type.displayName() + " &abooster multiplier to &f" + multiplier + "x&a."));
                return true;
            }
            case "start" -> {
                if (args.length < 3) {
                    this.sendUsage(sender, label);
                    return true;
                }
                BoosterType type = BoosterType.parse(args[1]);
                if (type == null) {
                    sender.sendMessage(Text.color("&cUnknown booster type."));
                    return true;
                }
                Long duration = parseDurationSeconds(args[2]);
                if (duration == null || duration <= 0L) {
                    sender.sendMessage(Text.color("&cDuration must be a positive number like 30m, 2h, or 600s."));
                    return true;
                }
                if (args.length >= 4) {
                    Double multiplier = parseDouble(args[3]);
                    if (multiplier == null || multiplier <= 0.0) {
                        sender.sendMessage(Text.color("&cMultiplier must be a positive number."));
                        return true;
                    }
                    this.boosterService.startBooster(type, multiplier, duration);
                } else {
                    this.boosterService.startBooster(type, duration);
                }
                sender.sendMessage(Text.color("&aStarted &f" + type.displayName() + " &abooster."));
                return true;
            }
            case "stop" -> {
                if (args.length < 2) {
                    this.sendUsage(sender, label);
                    return true;
                }
                BoosterType type = BoosterType.parse(args[1]);
                if (type == null) {
                    sender.sendMessage(Text.color("&cUnknown booster type."));
                    return true;
                }
                this.boosterService.stopBooster(type);
                sender.sendMessage(Text.color("&cStopped &f" + type.displayName() + " &cbooster."));
                return true;
            }
            case "status" -> {
                if (args.length == 1) {
                    for (BoosterType type : BoosterType.values()) {
                        sender.sendMessage(this.boosterService.getStatusLine(type));
                    }
                    return true;
                }
                BoosterType type = BoosterType.parse(args[1]);
                if (type == null) {
                    sender.sendMessage(Text.color("&cUnknown booster type."));
                    return true;
                }
                sender.sendMessage(this.boosterService.getStatusLine(type));
                return true;
            }
            default -> {
                this.sendUsage(sender, label);
                return true;
            }
        }
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(Text.color("&7/" + label + " set <xp|money|charge|shard> <multiplier>"));
        sender.sendMessage(Text.color("&7/" + label + " start <xp|money|charge|shard> <duration> [multiplier]"));
        sender.sendMessage(Text.color("&7/" + label + " stop <xp|money|charge|shard>"));
        sender.sendMessage(Text.color("&7/" + label + " status [type]"));
    }

    private static Double parseDouble(String input) {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Long parseDurationSeconds(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String s = input.trim().toLowerCase();
        long multiplier = 1L;
        if (s.endsWith("s")) {
            s = s.substring(0, s.length() - 1);
        } else if (s.endsWith("m")) {
            multiplier = 60L;
            s = s.substring(0, s.length() - 1);
        } else if (s.endsWith("h")) {
            multiplier = 3600L;
            s = s.substring(0, s.length() - 1);
        } else if (s.endsWith("d")) {
            multiplier = 86400L;
            s = s.substring(0, s.length() - 1);
        }
        try {
            return Long.parseLong(s) * multiplier;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
