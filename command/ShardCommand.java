/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package org.axial.prisonsCore.command;

import org.axial.prisonsCore.service.ShardService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShardCommand
implements CommandExecutor {
    private final ShardService shardService;

    public ShardCommand(ShardService shardService) {
        this.shardService = shardService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub;
        if (args.length == 0) {
            sender.sendMessage(Text.color("&eUsage: /fragment give <tier> [player] [amount] | /fragment addreward <tier>"));
            return true;
        }
        switch (sub = args[0].toLowerCase()) {
            case "give": {
                Player p;
                Player target;
                int amount = 1;
                if (!this.hasGive(sender)) {
                    sender.sendMessage(Text.color("&cNo permission."));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(Text.color("&cUsage: /fragment give <tier> [player] [amount]"));
                    return true;
                }
                ShardService.Tier tier = this.parseTier(args[1]);
                if (tier == null) {
                    sender.sendMessage(Text.color("&cInvalid tier."));
                    return true;
                }
                if (args.length >= 3) {
                    target = Bukkit.getPlayer((String)args[2]);
                } else if (sender instanceof Player) {
                    target = p = (Player)sender;
                } else {
                    target = null;
                }
                if (target == null) {
                    sender.sendMessage(Text.color("&cPlayer not found."));
                    return true;
                }
                if (args.length >= 4) {
                    try {
                        amount = Math.max(1, Integer.parseInt(args[3]));
                    }
                    catch (NumberFormatException e) {
                        sender.sendMessage(Text.color("&cInvalid amount."));
                        return true;
                    }
                }
                int remaining = amount;
                while (remaining > 0) {
                    ItemStack shard = this.shardService.createShard(tier);
                    shard.setAmount(Math.min(remaining, shard.getMaxStackSize()));
                    if (this.shardService.getPlugin().getStashService() != null) {
                        this.shardService.getPlugin().getStashService().giveOrStash(target, shard);
                    } else {
                        target.getInventory().addItem(new ItemStack[]{shard}).values().forEach(stack -> target.getWorld().dropItemNaturally(target.getLocation(), stack));
                    }
                    remaining -= shard.getAmount();
                }
                sender.sendMessage(Text.color("&aGiven " + amount + " fragment(s) to " + target.getName()));
                break;
            }
            case "addreward": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Text.color("&cPlayers only."));
                    return true;
                }
                Player player = (Player)sender;
                if (!this.hasEdit((CommandSender)player)) {
                    sender.sendMessage(Text.color("&cNo permission."));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(Text.color("&cUsage: /fragment addreward <tier>"));
                    return true;
                }
                ShardService.Tier tier = this.parseTier(args[1]);
                if (tier == null) {
                    sender.sendMessage(Text.color("&cInvalid tier."));
                    return true;
                }
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand == null || hand.getType().isAir()) {
                    sender.sendMessage(Text.color("&cHold an item to add."));
                    return true;
                }
                this.shardService.addReward(tier, hand);
                sender.sendMessage(Text.color("&aAdded reward to " + tier.name()));
                break;
            }
            default: {
                sender.sendMessage(Text.color("&cUnknown subcommand."));
            }
        }
        return true;
    }

    private ShardService.Tier parseTier(String s) {
        try {
            return ShardService.Tier.valueOf(s.toUpperCase());
        }
        catch (Exception e) {
            return null;
        }
    }

    private boolean hasGive(CommandSender sender) {
        return sender.hasPermission("prisons.fragment.give") || sender.hasPermission("prisons.shard.give");
    }

    private boolean hasEdit(CommandSender sender) {
        return sender.hasPermission("prisons.fragment.edit") || sender.hasPermission("prisons.shard.edit");
    }
}
