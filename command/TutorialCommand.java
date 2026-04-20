package org.axial.prisonsCore.command;

import org.axial.prisonsCore.service.TutorialService;
import org.axial.prisonsCore.service.PlayerToggleService;
import org.axial.prisonsCore.service.PlayerToggleService.Toggle;
import org.axial.prisonsCore.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TutorialCommand implements CommandExecutor {
    private final TutorialService tutorialService;
    private final PlayerToggleService toggleService;

    public TutorialCommand(TutorialService tutorialService, PlayerToggleService toggleService) {
        this.tutorialService = tutorialService;
        this.toggleService = toggleService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Text.color("&cOnly players can use this."));
            return true;
        }
        Player player = (Player)sender;
        if (args.length == 0) {
            player.sendMessage(Text.color("&eUsage: /tutorial skip"));
            player.sendMessage(Text.color("&e       /tutorial skip confirm"));
            return true;
        }
        if (!args[0].equalsIgnoreCase("skip")) {
            player.sendMessage(Text.color("&cUnknown subcommand."));
            return true;
        }
        if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
            this.tutorialService.skip(player);
            return true;
        }
        if (this.toggleService != null && !this.toggleService.isEnabled(player, Toggle.TUTORIAL_SKIP_CONFIRM)) {
            this.tutorialService.skip(player);
            return true;
        }
        this.tutorialService.requestSkip(player);
        return true;
    }
}
