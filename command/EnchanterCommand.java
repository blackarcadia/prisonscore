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

import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.gui.EnchanterMenu;
import org.axial.prisonsCore.service.FeatureToggleService;
import org.axial.prisonsCore.service.PickaxeManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EnchanterCommand
implements CommandExecutor {
    private final PickaxeManager pickaxeManager;
    private final EnchanterMenu enchanterMenu;
    private final PrisonsCore plugin;
    private final FeatureToggleService featureToggleService;

    public EnchanterCommand(PrisonsCore plugin, PickaxeManager pickaxeManager, EnchanterMenu enchanterMenu, FeatureToggleService featureToggleService) {
        this.pickaxeManager = pickaxeManager;
        this.enchanterMenu = enchanterMenu;
        this.plugin = plugin;
        this.featureToggleService = featureToggleService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player)sender;
        if (!this.featureToggleService.checkEnabled(player, FeatureToggleService.Feature.ENCHANTER)) {
            return true;
        }
        this.enchanterMenu.open(player);
        return true;
    }
}
