/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
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
import org.axial.prisonsCore.service.SatchelManager;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SatchelCommand
implements CommandExecutor,
TabCompleter {
    private final SatchelManager satchelManager;

    public SatchelCommand(SatchelManager satchelManager) {
        this.satchelManager = satchelManager;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p;
        Player target = null;
        if (args.length < 2 || !args[0].equalsIgnoreCase("give")) {
            sender.sendMessage(Text.color("&cUsage: /satchel give <ore> [player]"));
            return true;
        }
        String typeName = args[1].toUpperCase(Locale.ROOT);
        Material mat = this.satchelManager.getConfiguredMaterial(typeName);
        if (mat == null) {
            sender.sendMessage(Text.color("&cUnknown satchel type."));
            return true;
        }
        Object object = args.length >= 3 ? Bukkit.getPlayerExact((String)args[2]) : (target = sender instanceof Player ? (p = (Player)sender) : null);
        if (target == null) {
            sender.sendMessage(Text.color("&cPlayer not found."));
            return true;
        }
        ItemStack satchel = this.satchelManager.createSatchel(mat);
        target.getInventory().addItem(new ItemStack[]{satchel});
        if (!target.equals((Object)sender)) {
            sender.sendMessage(Text.color("&aGave satchel to " + target.getName()));
        }
        target.sendMessage(Text.color("&aYou received a " + satchel.getItemMeta().getDisplayName()));
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> list;
        block6: {
            block5: {
                list = new ArrayList<String>();
                if (args.length != 1) break block5;
                if (!"give".startsWith(args[0].toLowerCase(Locale.ROOT))) break block6;
                list.add("give");
                break block6;
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
                for (Material m : this.satchelManager.getConfiguredTypes()) {
                    String name = m.name().toLowerCase(Locale.ROOT);
                    if (!name.startsWith(args[1].toLowerCase(Locale.ROOT))) continue;
                    list.add(name);
                }
            } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.getName().toLowerCase(Locale.ROOT).startsWith(args[2].toLowerCase(Locale.ROOT))) continue;
                    list.add(p.getName());
                }
            }
        }
        return list;
    }
}

