/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 */
package org.axial.prisonsCore.command;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.axial.prisonsCore.service.GangService;
import org.axial.prisonsCore.util.Lang;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class GangPointsCommand
implements CommandExecutor,
TabCompleter {
    private final GangService gangService;
    private final NamespacedKey voucherKey;
    private final Plugin plugin;
    private Material voucherMaterial = Material.PAPER;
    private String voucherDisplay = "&bGang Points Voucher";
    private List<String> voucherLore = List.of("&7Right-click to add &f{amount} &7points to your gang.", "&8(Requires you to be in a gang)");

    public GangPointsCommand(Plugin plugin, GangService gangService, NamespacedKey voucherKey) {
        this.plugin = plugin;
        this.gangService = gangService;
        this.voucherKey = voucherKey;
        this.loadConfig();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int amount;
        if (!sender.hasPermission("prisons.gangpoints.give")) {
            sender.sendMessage(Lang.msg("gangpoints.no-permission", "&cNo permission."));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(Lang.msg("gangpoints.usage", "&cUsage: /gangpoints give <player> <amount>"));
            return true;
        }
        if (!args[0].equalsIgnoreCase("give")) {
            sender.sendMessage(Lang.msg("gangpoints.unknown", "&cUnknown subcommand."));
            return true;
        }
        Player target = sender.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Lang.msg("gangpoints.player-not-found", "&cPlayer not found."));
            return true;
        }
        try {
            amount = Integer.parseInt(args.length >= 3 ? args[2] : "0");
        }
        catch (NumberFormatException ex) {
            sender.sendMessage(Lang.msg("gangpoints.invalid-amount", "&cInvalid amount."));
            return true;
        }
        if (amount <= 0) {
            sender.sendMessage(Lang.msg("gangpoints.invalid-amount", "&cAmount must be positive."));
            return true;
        }
        ItemStack voucher = this.createVoucher(amount);
        target.getInventory().addItem(new ItemStack[]{voucher});
        sender.sendMessage(Lang.msg("gangpoints.given", "&aGave {amount} gang points voucher to {player}").replace("{amount}", String.valueOf(amount)).replace("{player}", target.getName()));
        return true;
    }

    private ItemStack createVoucher(int amount) {
        ItemStack paper = new ItemStack(this.voucherMaterial);
        ItemMeta meta = paper.getItemMeta();
        meta.setDisplayName(Text.color(this.voucherDisplay.replace("{amount}", String.valueOf(amount))));
        meta.setLore(this.voucherLore.stream().map(l -> l.replace("{amount}", String.valueOf(amount))).map(Text::color).collect(Collectors.toList()));
        meta.getPersistentDataContainer().set(this.voucherKey, PersistentDataType.INTEGER, amount);
        paper.setItemMeta(meta);
        return paper;
    }

    private void loadConfig() {
        File file = new File(this.plugin.getDataFolder(), "items.yml");
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration((File)file);
        ConfigurationSection sec = cfg.getConfigurationSection("items.gang-points");
        if (sec != null) {
            Material mat = Material.matchMaterial((String)sec.getString("material", "PAPER"));
            if (mat != null) {
                this.voucherMaterial = mat;
            }
            this.voucherDisplay = sec.getString("display-name", this.voucherDisplay);
            List lore = sec.getStringList("lore");
            if (lore != null && !lore.isEmpty()) {
                this.voucherLore = lore;
            }
        }
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("prisons.gangpoints.give")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            return List.of("give").stream().filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT))).collect(Collectors.toList());
        }
        if (args.length == 2) {
            return sender.getServer().getOnlinePlayers().stream().map(Player::getName).filter(n -> n.toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT))).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
