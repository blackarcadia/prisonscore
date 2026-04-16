/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package org.axial.prisonsCore.command;

import org.axial.prisonsCore.service.MeteorService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.World;

public class MeteorCommand
implements CommandExecutor {
    private final MeteorService meteorService;

    public MeteorCommand(MeteorService meteorService) {
        this.meteorService = meteorService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("prisons.meteor.give")) {
            sender.sendMessage(Text.color("&cNo permission."));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(Text.color("&eUsage: /meteor flare <player> [amount]"));
            sender.sendMessage(Text.color("&eUsage: /meteor setworld"));
            return true;
        }
        if (args[0].equalsIgnoreCase("setworld")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Text.color("&cOnly players can set the meteor world in-game."));
                return true;
            }
            World world = player.getWorld();
            this.meteorService.setMeteorWorld(world);
            sender.sendMessage(Text.color("&aMeteors will now only spawn in &f" + world.getName() + "&a."));
            return true;
        }
        if (!args[0].equalsIgnoreCase("flare")) {
            sender.sendMessage(Text.color("&eUsage: /meteor flare <player> [amount]"));
            sender.sendMessage(Text.color("&eUsage: /meteor setworld"));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(Text.color("&cSpecify a player."));
            return true;
        }
        Player target = sender.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Text.color("&cPlayer not found."));
            return true;
        }
        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Math.max(1, Integer.parseInt(args[2]));
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        target.getInventory().addItem(new ItemStack[]{this.meteorService.createFlare(amount)});
        sender.sendMessage(Text.color("&aGave &f" + amount + " &aMeteor Flare(s) to &f" + target.getName() + "&a."));
        if (!sender.equals((Object)target)) {
            target.sendMessage(Text.color("&dYou received &f" + amount + " &dMeteor Flare(s)!"));
        }
        return true;
    }
}
