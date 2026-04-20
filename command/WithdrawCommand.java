package org.axial.prisonsCore.command;

import java.util.Map;
import org.axial.prisonsCore.service.BankNoteService;
import org.axial.prisonsCore.service.EconomyService;
import org.axial.prisonsCore.util.Lang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WithdrawCommand implements CommandExecutor {
    private final EconomyService economyService;
    private final BankNoteService bankNoteService;

    public WithdrawCommand(EconomyService economyService, BankNoteService bankNoteService) {
        this.economyService = economyService;
        this.bankNoteService = bankNoteService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Lang.msg("common.players-only", "Only players can use this command."));
            return true;
        }
        if (args.length != 1) {
            player.sendMessage(Lang.msg("economy.withdraw-usage", "&cUsage: /" + label + " <amount>"));
            return true;
        }
        Double parsed = this.economyService.parseAmount(args[0]);
        if (parsed == null || parsed <= 0.0) {
            player.sendMessage(Lang.msg("economy.withdraw-invalid", "&cInvalid amount."));
            return true;
        }
        double amount = parsed;
        if (!this.economyService.withdraw(player, amount)) {
            player.sendMessage(Lang.msg("economy.withdraw-not-enough", "&cYou don't have enough money."));
            return true;
        }
        ItemStack note = this.bankNoteService.createBankNote(amount);
        if (this.economyService.getPlugin().getStashService() != null) {
            this.economyService.getPlugin().getStashService().giveOrStash(player, note);
        } else {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(note);
            for (ItemStack stack : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), stack);
            }
        }
        player.sendMessage(Lang.msg("economy.withdraw-success", "&aWithdrew &2{amount} &ainto a bank note.").replace("{amount}", this.economyService.format(amount)));
        return true;
    }
}
