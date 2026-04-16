/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package org.axial.prisonsCore.command;

import org.axial.prisonsCore.service.EconomyService;
import org.axial.prisonsCore.service.SatchelManager;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SellCommand
implements CommandExecutor {
    private final EconomyService economyService;
    private final SatchelManager satchelManager;

    public SellCommand(EconomyService economyService, SatchelManager satchelManager) {
        this.economyService = economyService;
        this.satchelManager = satchelManager;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int[] slots;
        if (!(sender instanceof Player)) {
            sender.sendMessage(Text.color("&cOnly players can sell items."));
            return true;
        }
        Player player = (Player)sender;
        boolean handOnly = args.length > 0 && args[0].equalsIgnoreCase("hand");
        int sold = 0;
        double total = 0.0;
        if (handOnly) {
            slots = new int[]{player.getInventory().getHeldItemSlot()};
        } else {
            slots = new int[player.getInventory().getSize()];
            for (int i = 0; i < slots.length; ++i) {
                slots[i] = i;
            }
        }
        for (int slot : slots) {
            double price;
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack == null || stack.getType() == Material.AIR || this.satchelManager.isSatchel(stack) || (price = this.economyService.getWorth(stack.getType())) <= 0.0) continue;
            int amount = stack.getAmount();
            total += price * (double)amount;
            sold += amount;
            player.getInventory().setItem(slot, null);
        }
        if (sold == 0 || total <= 0.0) {
            player.sendMessage(Text.color("&cNothing to sell."));
            return true;
        }
        this.economyService.deposit(player, total);
        player.sendMessage(Text.color("&aSold " + sold + " items for &2" + this.economyService.format(total)));
        return true;
    }
}

