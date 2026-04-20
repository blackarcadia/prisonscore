package org.axial.prisonsCore.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.axial.prisonsCore.service.ContrabandService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ContrabandCommand implements CommandExecutor, TabCompleter {
    private final ContrabandService contrabandService;

    public ContrabandCommand(ContrabandService contrabandService) {
        this.contrabandService = contrabandService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player target;
        if (!sender.hasPermission("prisons.contraband.give")) {
            sender.sendMessage(Text.color("&cNo permission."));
            return true;
        }
        if (args.length < 2 || !args[0].equalsIgnoreCase("give")) {
            sender.sendMessage(Text.color("&cUsage: /contraband give <id> [player] [amount]"));
            return true;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        if (!this.contrabandService.hasTier(id)) {
            sender.sendMessage(Text.color("&cUnknown contraband tier."));
            return true;
        }
        if (args.length >= 3) {
            target = Bukkit.getPlayerExact(args[2]);
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            target = null;
        }
        if (target == null) {
            sender.sendMessage(Text.color("&cPlayer not found."));
            return true;
        }
        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Math.max(1, Integer.parseInt(args[3]));
            }
            catch (NumberFormatException e) {
                sender.sendMessage(Text.color("&cInvalid amount."));
                return true;
            }
        }
        int remaining = amount;
        while (remaining > 0) {
            ItemStack stack = this.contrabandService.createContraband(id);
            if (stack == null) {
                sender.sendMessage(Text.color("&cFailed to create contraband item."));
                return true;
            }
            stack.setAmount(Math.min(remaining, stack.getMaxStackSize()));
            if (this.contrabandService.getPlugin().getStashService() != null) {
                this.contrabandService.getPlugin().getStashService().giveOrStash(target, stack);
            } else {
                target.getInventory().addItem(stack).values().forEach(item -> target.getWorld().dropItemNaturally(target.getLocation(), item));
            }
            remaining -= stack.getAmount();
        }
        sender.sendMessage(Text.color("&aGave " + amount + "x " + id + " to " + target.getName()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> completions = new ArrayList<String>();
        if (args.length == 1) {
            if ("give".startsWith(args[0].toLowerCase(Locale.ENGLISH))) {
                completions.add("give");
            }
            return completions;
        }
        if (args.length == 2) {
            for (String id : this.contrabandService.tierIds()) {
                if (id.startsWith(args[1].toLowerCase(Locale.ENGLISH))) {
                    completions.add(id);
                }
            }
            return completions;
        }
        if (args.length == 3) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase(Locale.ENGLISH).startsWith(args[2].toLowerCase(Locale.ENGLISH))) {
                    completions.add(player.getName());
                }
            }
        }
        return completions;
    }
}
