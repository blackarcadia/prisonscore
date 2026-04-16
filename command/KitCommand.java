package org.axial.prisonsCore.command;

import org.axial.prisonsCore.gui.KitMenu;
import org.axial.prisonsCore.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KitCommand implements CommandExecutor {
    private final KitMenu kitMenu;

    public KitCommand(KitMenu kitMenu) {
        this.kitMenu = kitMenu;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Text.color("&cPlayers only."));
            return true;
        }
        this.kitMenu.open(player);
        return true;
    }
}
