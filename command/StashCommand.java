package org.axial.prisonsCore.command;

import org.axial.prisonsCore.gui.StashMenu;
import org.axial.prisonsCore.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class StashCommand implements CommandExecutor {
    private final StashMenu stashMenu;

    public StashCommand(StashMenu stashMenu) {
        this.stashMenu = stashMenu;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Text.color("&cOnly players can use this command."));
            return true;
        }
        if (args.length > 0 && !"open".equalsIgnoreCase(args[0])) {
            sender.sendMessage(Text.color("&7/" + label + " [open]"));
            return true;
        }
        this.stashMenu.open(player);
        return true;
    }
}
