/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package org.axial.prisonsCore.command;

import org.axial.prisonsCore.gui.LevelsMenu;
import org.axial.prisonsCore.service.FeatureToggleService;
import org.axial.prisonsCore.service.TutorialService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LevelsCommand
implements CommandExecutor {
    private final LevelsMenu menu;
    private final TutorialService tutorialService;
    private final FeatureToggleService featureToggleService;

    public LevelsCommand(LevelsMenu menu, TutorialService tutorialService, FeatureToggleService featureToggleService) {
        this.menu = menu;
        this.tutorialService = tutorialService;
        this.featureToggleService = featureToggleService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        Player player = (Player)sender;
        if (!this.featureToggleService.checkEnabled(player, FeatureToggleService.Feature.LEVELS)) {
            return true;
        }
        if (this.tutorialService != null) {
            this.tutorialService.handleLevelsCommand(player);
        }
        this.menu.open(player);
        return true;
    }
}
