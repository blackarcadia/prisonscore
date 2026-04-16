package org.axial.prisonsCore.command;

import org.axial.prisonsCore.gui.ShopMenu;
import org.axial.prisonsCore.service.FeatureToggleService;
import org.axial.prisonsCore.util.Lang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {
    private final ShopMenu shopMenu;
    private final FeatureToggleService featureToggleService;

    public ShopCommand(ShopMenu shopMenu, FeatureToggleService featureToggleService) {
        this.shopMenu = shopMenu;
        this.featureToggleService = featureToggleService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Lang.msg("common.players-only", "Only players can use this command."));
            return true;
        }
        if (!this.featureToggleService.checkEnabled(player, FeatureToggleService.Feature.SHOP)) {
            return true;
        }
        this.shopMenu.open(player);
        return true;
    }
}
