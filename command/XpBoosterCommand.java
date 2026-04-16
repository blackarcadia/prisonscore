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
 */
package org.axial.prisonsCore.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.axial.prisonsCore.service.BoosterService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class XpBoosterCommand
implements CommandExecutor,
TabCompleter {
    private final BoosterService boosterService;

    public XpBoosterCommand(BoosterService boosterService) {
        this.boosterService = boosterService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        double multiplier;
        if (args.length < 4 || !args[0].equalsIgnoreCase("give")) {
            sender.sendMessage(Text.color("&cUsage: /xpbooster give <player> <duration> <multiplier>"));
            return true;
        }
        Player target = Bukkit.getPlayer((String)args[1]);
        if (target == null) {
            sender.sendMessage(Text.color("&cPlayer not found."));
            return true;
        }
        int durationSeconds = this.parseDuration(args[2]);
        if (durationSeconds <= 0) {
            sender.sendMessage(Text.color("&cInvalid duration. Use formats like 30m, 45s, 1h."));
            return true;
        }
        try {
            multiplier = Double.parseDouble(args[3]);
        }
        catch (NumberFormatException e) {
            sender.sendMessage(Text.color("&cInvalid multiplier. Example: 2 or 1.5"));
            return true;
        }
        if (multiplier <= 0.0) {
            sender.sendMessage(Text.color("&cMultiplier must be > 0."));
            return true;
        }
        ItemStack item = this.boosterService.createBoosterItem(BoosterService.BoosterType.XP, durationSeconds, multiplier, 1);
        target.getInventory().addItem(new ItemStack[]{item});
        sender.sendMessage(Text.color("&aGave XP booster to &f" + target.getName()));
        return true;
    }

    private int parseDuration(String raw) {
        int value;
        if ((raw = raw.toLowerCase(Locale.ROOT).trim()).isEmpty()) {
            return 0;
        }
        char suffix = raw.charAt(raw.length() - 1);
        String numberPart = raw;
        int multiplier = 1;
        if (Character.isLetter(suffix)) {
            numberPart = raw.substring(0, raw.length() - 1);
            switch (suffix) {
                case 's': {
                    multiplier = 1;
                    break;
                }
                case 'm': {
                    multiplier = 60;
                    break;
                }
                case 'h': {
                    multiplier = 3600;
                    break;
                }
                default: {
                    multiplier = 0;
                }
            }
        }
        try {
            value = Integer.parseInt(numberPart);
        }
        catch (NumberFormatException e) {
            return 0;
        }
        if (value <= 0 || multiplier == 0) {
            return 0;
        }
        return value * multiplier;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> out = new ArrayList<String>();
        if (args.length == 1) {
            out.add("give");
        } else if (args.length == 2) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                out.add(p.getName());
            }
        } else if (args.length == 3) {
            out.add("30m");
            out.add("1h");
            out.add("45s");
        } else if (args.length == 4) {
            out.add("2");
            out.add("1.5");
        }
        return out;
    }
}

