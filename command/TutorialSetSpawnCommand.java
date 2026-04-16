package org.axial.prisonsCore.command;

import org.axial.prisonsCore.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TutorialSetSpawnCommand implements CommandExecutor {
    public TutorialSetSpawnCommand() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Text.color("&cOnly players can set tutorial spawn."));
            return true;
        }
        Player player = (Player)sender;
        if (!player.hasPermission("prisons.tutorial.setspawn")) {
            player.sendMessage(Text.color("&cNo permission."));
            return true;
        }
        player.sendMessage(Text.color("&eTutorial spawn is hardcoded and cannot be changed here."));
        return true;
    }
}
