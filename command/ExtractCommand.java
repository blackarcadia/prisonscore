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

import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.service.EnergyItemService;
import org.axial.prisonsCore.service.PickaxeManager;
import org.axial.prisonsCore.util.Lang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ExtractCommand
implements CommandExecutor {
    private final PickaxeManager pickaxeManager;
    private final EnergyItemService energyItemService;
    private final PrisonsCore plugin;

    public ExtractCommand(PrisonsCore plugin, PickaxeManager pickaxeManager, EnergyItemService energyItemService) {
        this.pickaxeManager = pickaxeManager;
        this.energyItemService = energyItemService;
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int amount;
        if (!(sender instanceof Player)) {
            sender.sendMessage(Lang.msg("common.players-only", "Only players can use this command."));
            return true;
        }
        Player player = (Player)sender;
        if (args.length != 1) {
            sender.sendMessage(Lang.msg("extract.usage", "&cUsage: /" + label + " <amount|all>"));
            return true;
        }
        ItemStack held = player.getInventory().getItemInMainHand();
        if (!this.pickaxeManager.isPrisonPickaxe(held)) {
            sender.sendMessage(Lang.msg("pickaxe.not-a-pickaxe", "&cYou must hold a prison pickaxe to do that."));
            return true;
        }
        int current = this.pickaxeManager.getEnergy(held);
        if (args[0].equalsIgnoreCase("all")) {
            amount = current;
        } else {
            try {
                amount = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException ex) {
                sender.sendMessage(Lang.msg("extract.invalid-amount", "&cInvalid amount."));
                return true;
            }
            if (amount <= 0) {
                sender.sendMessage(Lang.msg("extract.invalid-amount", "&cAmount must be positive."));
                return true;
            }
        }
        if (current < amount) {
            sender.sendMessage(Lang.msg("extract.not-enough-energy", "&cNot enough charge to extract."));
            return true;
        }
        if (amount <= 0) {
            sender.sendMessage(Lang.msg("extract.not-enough-energy", "&cNot enough charge to extract."));
            return true;
        }
        double taxRate = 0.25;
        if (player.hasPermission("extract.no")) {
            taxRate = 0.0;
        } else if (player.hasPermission("extract.5")) {
            taxRate = 0.05;
        } else if (player.hasPermission("extract.10")) {
            taxRate = 0.1;
        } else if (player.hasPermission("extract.15")) {
            taxRate = 0.15;
        } else if (player.hasPermission("extract.20")) {
            taxRate = 0.2;
        }
        int netAmount = (int)Math.floor((double)amount * (1.0 - taxRate));
        netAmount = Math.max(0, netAmount);
        this.pickaxeManager.setEnergyOverflow(held, current - amount);
        if (netAmount > 0) {
            this.energyItemService.giveEnergyItem(player, netAmount);
        }
        sender.sendMessage(Lang.msg("extract.extracted", "&aExtracted {amount} charge (tax {tax}%)").replace("{amount}", String.valueOf(netAmount)).replace("{tax}", String.valueOf((int)(taxRate * 100.0))));
        return true;
    }
}
