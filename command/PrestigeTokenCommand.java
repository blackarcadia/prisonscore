/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.persistence.PersistentDataType
 */
package org.axial.prisonsCore.command;

import java.util.List;
import org.axial.prisonsCore.service.PickaxeManager;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PrestigeTokenCommand
implements CommandExecutor {
    private final PickaxeManager pickaxeManager;
    private final NamespacedKey tokenKey;

    public PrestigeTokenCommand(PickaxeManager pickaxeManager, NamespacedKey tokenKey) {
        this.pickaxeManager = pickaxeManager;
        this.tokenKey = tokenKey;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub;
        if (args.length < 1) {
            sender.sendMessage(Text.color("&cUsage: /prestigetoken give <player> [amount] | /prestigetoken apply"));
            return true;
        }
        switch (sub = args[0].toLowerCase()) {
            case "give": {
                if (args.length < 2) {
                    sender.sendMessage(Text.color("&cUsage: /prestigetoken give <player> [amount]"));
                    return true;
                }
                Player target = Bukkit.getPlayer((String)args[1]);
                if (target == null) {
                    sender.sendMessage(Text.color("&cPlayer not found."));
                    return true;
                }
                int amount = 1;
                if (args.length >= 3) {
                    try {
                        amount = Integer.parseInt(args[2]);
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                }
                ItemStack token = this.createToken();
                token.setAmount(Math.max(1, amount));
                target.getInventory().addItem(new ItemStack[]{token});
                target.sendMessage(Text.color("&dYou received " + amount + " prestige token(s)."));
                return true;
            }
            case "apply": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Players only.");
                    return true;
                }
                Player player = (Player)sender;
                ItemStack held = player.getInventory().getItemInMainHand();
                if (!this.pickaxeManager.isPrisonPickaxe(held)) {
                    player.sendMessage(Text.color("&cHold a prison pickaxe to apply the token."));
                    return true;
                }
                int slot = player.getInventory().first(this.createToken());
                if (slot == -1) {
                    player.sendMessage(Text.color("&cYou need a prestige token in your inventory."));
                    return true;
                }
                player.getInventory().setItem(slot, null);
                this.pickaxeManager.setPrestigeToken(held, true);
                player.sendMessage(Text.color("&aPrestige token applied to your pickaxe."));
                return true;
            }
        }
        sender.sendMessage(Text.color("&cUsage: /prestigetoken give <player> [amount] | /prestigetoken apply"));
        return true;
    }

    private ItemStack createToken() {
        ItemStack token = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = token.getItemMeta();
        meta.setDisplayName(Text.color("&dPickaxe Prestige Token"));
        meta.setLore(List.of(Text.color("&7Apply to a pickaxe to unlock prestige")));
        meta.getPersistentDataContainer().set(this.tokenKey, PersistentDataType.BYTE, (byte)1);
        token.setItemMeta(meta);
        return token;
    }
}
