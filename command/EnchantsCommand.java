package org.axial.prisonsCore.command;

import org.axial.prisonsCore.gui.EnchantsMenu;
import org.axial.prisonsCore.service.FeatureToggleService;
import org.axial.prisonsCore.service.TutorialService;
import org.axial.prisonsCore.util.Lang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EnchantsCommand implements CommandExecutor {
    private final EnchantsMenu menu;
    private final TutorialService tutorialService;
    private final FeatureToggleService featureToggleService;

    public EnchantsCommand(EnchantsMenu menu, TutorialService tutorialService, FeatureToggleService featureToggleService) {
        this.menu = menu;
        this.tutorialService = tutorialService;
        this.featureToggleService = featureToggleService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Lang.msg("common.players-only", "Only players can use this command."));
            return true;
        }
        Player player = (Player)sender;
        if (!this.featureToggleService.checkEnabled(player, FeatureToggleService.Feature.ENCHANTS)) {
            return true;
        }
        if (this.tutorialService != null) {
            this.tutorialService.handleEnchantsCommand(player);
        }
        this.menu.open(player);
        return true;
    }
}
