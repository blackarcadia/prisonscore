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
import java.util.List;
import java.util.Locale;
import org.axial.prisonsCore.service.CustomBlockService;
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

public class CustomBlockCommand
implements CommandExecutor,
TabCompleter {
    private final CustomBlockService customBlockService;

    public CustomBlockCommand(CustomBlockService customBlockService) {
        this.customBlockService = customBlockService;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player p;
        Player target = null;
        if (!sender.hasPermission("prisons.customblock.give")) {
            sender.sendMessage(Text.color("&cNo permission."));
            return true;
        }
        if (args.length < 2 || !args[0].equalsIgnoreCase("give")) {
            sender.sendMessage(Text.color("&eUsage: /customblock give <id> [player]"));
            return true;
        }
        String id = args[1].toLowerCase(Locale.ROOT);
        if (!this.customBlockService.getIds().contains(id)) {
            sender.sendMessage(Text.color("&cUnknown custom block id."));
            return true;
        }
        Object object = args.length >= 3 ? Bukkit.getPlayer((String)args[2]) : (target = sender instanceof Player ? (p = (Player)sender) : null);
        if (target == null) {
            sender.sendMessage(Text.color("&cPlayer not found."));
            return true;
        }
        ItemStack item = this.customBlockService.createItem(id);
        if (item == null) {
            sender.sendMessage(Text.color("&cFailed to create that custom block item."));
            return true;
        }
        target.getInventory().addItem(new ItemStack[]{item});
        sender.sendMessage(Text.color("&aGave custom block &f" + id + " &ato &f" + target.getName()));
        return true;
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("give");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return new ArrayList<String>(this.customBlockService.getIds());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return null;
        }
        return List.of();
    }
}

