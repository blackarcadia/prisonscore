package org.axial.prisonsCore.command;

import org.axial.prisonsCore.gui.ToggleMenu;
import org.axial.prisonsCore.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ToggleCommand implements CommandExecutor {
    private final ToggleMenu toggleMenu;

    public ToggleCommand(ToggleMenu toggleMenu) {
        this.toggleMenu = toggleMenu;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Text.color("&cOnly players can use this."));
            return true;
        }
        this.toggleMenu.open(player);
        return true;
    }
}
