/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.block.Block
 *  org.bukkit.block.Sign
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package org.axial.prisonsCore.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.cell.Cell;
import org.axial.prisonsCore.cell.CellDoorType;
import org.axial.prisonsCore.cell.CellMenu;
import org.axial.prisonsCore.cell.CellService;
import org.axial.prisonsCore.cell.CellTier;
import org.axial.prisonsCore.cell.guard.CellGuardService;
import org.axial.prisonsCore.cell.guard.CellGuardTier;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CellCommand
implements CommandExecutor,
TabCompleter {
    private final CellService cellService;
    private final CellMenu menu;
    private final CellGuardService cellGuardService;
    private final Map<UUID, PendingConfirm> pendingConfirms = new HashMap<UUID, PendingConfirm>();

    public CellCommand(PrisonsCore plugin, CellService cellService, CellMenu menu, CellGuardService cellGuardService) {
        this.cellService = cellService;
        this.menu = menu;
        this.cellGuardService = cellGuardService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can open the cell menu.");
                return true;
            }
            Player player = (Player)sender;
            this.menu.open(player);
            return true;
        }
        if (args[0].equalsIgnoreCase("access")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can manage access.");
                return true;
            }
            Player player = (Player)sender;
            if (args.length < 2) {
                player.sendMessage(Text.color("&cUsage: /cell access <player>"));
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer((String)args[1]);
            Cell owned = this.cellService.getOwnedCell(player.getUniqueId());
            if (owned == null) {
                player.sendMessage(Text.color("&cYou don't own a cell."));
                return true;
            }
            boolean added = this.cellService.toggleAccess(player, target);
            player.sendMessage(Text.color(added ? "&aGranted access to " + target.getName() : "&cRevoked access from " + target.getName()));
            if (target.isOnline()) {
                target.getPlayer().sendMessage(Text.color(added ? "&aYou were granted access to cell " + owned.getId() : "&cYour access to cell " + owned.getId() + " was revoked."));
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("home")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Players only.");
                return true;
            }
            Player player = (Player)sender;
            Cell owned = this.cellService.getOwnedCell(player.getUniqueId());
            if (owned == null) {
                player.sendMessage(Text.color("&cYou don't own a cell."));
                return true;
            }
            if (this.cellService.teleportToCell(player, owned)) {
                player.sendMessage(Text.color("&aTeleporting to your cell..."));
            } else {
                player.sendMessage(Text.color("&cCould not find a teleport location for your cell."));
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("sethome")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Players only.");
                return true;
            }
            Player player = (Player)sender;
            Cell owned = this.cellService.getOwnedCell(player.getUniqueId());
            if (owned == null) {
                player.sendMessage(Text.color("&cYou don't own a cell."));
                return true;
            }
            if (this.cellService.setCellHome(owned, player)) {
                player.sendMessage(Text.color("&aSet your cell home to your current location."));
            } else {
                player.sendMessage(Text.color("&cFailed to set your cell home."));
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("owner")) {
            String targetName;
            if (!(sender instanceof Player)) {
                sender.sendMessage("Players only.");
                return true;
            }
            Player player = (Player)sender;
            if (args.length < 2) {
                player.sendMessage(Text.color("&cUsage: /cell owner <player>"));
                return true;
            }
            Cell owned = this.cellService.getOwnedCell(player.getUniqueId());
            if (owned == null) {
                player.sendMessage(Text.color("&cYou don't own a cell."));
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer((String)args[1]);
            if (target == null || target.getUniqueId() == null) {
                player.sendMessage(Text.color("&cPlayer not found."));
                return true;
            }
            String string = targetName = target.getName() != null ? target.getName() : target.getUniqueId().toString();
            if (this.cellService.getOwnedCell(target.getUniqueId()) != null) {
                player.sendMessage(Text.color("&cThat player already owns a cell."));
                return true;
            }
            if (target.getUniqueId().equals(player.getUniqueId())) {
                player.sendMessage(Text.color("&cYou already own this cell."));
                return true;
            }
            if (this.confirm(player, PendingAction.TRANSFER, target.getUniqueId())) {
                boolean ok = this.cellService.transferOwnership(owned, target);
                if (ok) {
                    player.sendMessage(Text.color("&aTransferred cell &f" + owned.getId() + " &ato " + targetName + "."));
                    if (target.isOnline()) {
                        target.getPlayer().sendMessage(Text.color("&aYou now own cell &f" + owned.getId() + "&a."));
                    }
                } else {
                    player.sendMessage(Text.color("&cTransfer failed."));
                }
            } else {
                player.sendMessage(Text.color("&cThis will transfer cell &f" + owned.getId() + " &cto " + targetName + " permanently."));
                player.sendMessage(Text.color("&cRun &f/cell owner " + targetName + " &cagain to confirm."));
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("disband")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Players only.");
                return true;
            }
            Player player = (Player)sender;
            Cell owned = this.cellService.getOwnedCell(player.getUniqueId());
            if (owned == null) {
                player.sendMessage(Text.color("&cYou don't own a cell."));
                return true;
            }
            if (this.confirm(player, PendingAction.DISBAND, owned.getId())) {
                boolean ok = this.cellService.disbandCell(owned);
                if (ok) {
                    player.sendMessage(Text.color("&aYou forfeited cell &f" + owned.getId() + "&a. It can now be rented again."));
                } else {
                    player.sendMessage(Text.color("&cFailed to forfeit the cell."));
                }
            } else {
                player.sendMessage(Text.color("&cThis will permanently forfeit cell &f" + owned.getId() + " &cand clear its contents."));
                player.sendMessage(Text.color("&cRun &f/cell disband &cagain to confirm."));
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("guard")) {
            CellGuardTier tier;
            if (args.length < 2 || !args[1].equalsIgnoreCase("give")) {
                sender.sendMessage(Text.color("&cUsage: /cell guard give <BASIC|UNIQUE|RARE|ELITE|LEGENDARY> <player> [amount]"));
                return true;
            }
            if (!sender.hasPermission("cells.admin")) {
                sender.sendMessage(Text.color("&cNo permission."));
                return true;
            }
            if (args.length < 4) {
                sender.sendMessage(Text.color("&cUsage: /cell guard give <tier> <player> [amount]"));
                return true;
            }
            try {
                tier = CellGuardTier.valueOf(args[2].toUpperCase(Locale.ROOT));
            }
            catch (IllegalArgumentException e) {
                sender.sendMessage(Text.color("&cInvalid guard tier."));
                return true;
            }
            Player target = Bukkit.getPlayer((String)args[3]);
            if (target == null) {
                sender.sendMessage(Text.color("&cPlayer not found."));
                return true;
            }
            int amount = 1;
            if (args.length >= 5) {
                try {
                    amount = Math.max(1, Integer.parseInt(args[4]));
                }
                catch (NumberFormatException targetName) {
                    // empty catch block
                }
            }
            target.getInventory().addItem(new ItemStack[]{this.cellGuardService.createGuardAnchor(tier, amount)});
            sender.sendMessage(Text.color("&aGave " + amount + " " + tier.name() + " cell guard anchor(s) to " + target.getName()));
            return true;
        }
        if (!sender.hasPermission("cells.admin")) {
            sender.sendMessage(Text.color("&cUnknown subcommand."));
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "pos1": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Players only.");
                    return true;
                }
                Player player = (Player)sender;
                this.cellService.setPos1(player.getUniqueId(), player.getLocation());
                sender.sendMessage(Text.color("&aSet pos1 at your location."));
                break;
            }
            case "pos2": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Players only.");
                    return true;
                }
                Player player = (Player)sender;
                this.cellService.setPos2(player.getUniqueId(), player.getLocation());
                sender.sendMessage(Text.color("&aSet pos2 at your location."));
                break;
            }
            case "create": {
                CellTier tier;
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Players only.");
                    return true;
                }
                Player player = (Player)sender;
                if (args.length < 3) {
                    sender.sendMessage(Text.color("&cUsage: /cell create <id> <tier>"));
                    return true;
                }
                try {
                    tier = CellTier.valueOf(args[2].toUpperCase(Locale.ROOT));
                }
                catch (IllegalArgumentException e) {
                    sender.sendMessage(Text.color("&cInvalid tier. Use INMATE, TRUSTEE, WARDEN."));
                    return true;
                }
                CellService.Selection selection = this.cellService.getSelection(player.getUniqueId());
                if (!selection.isComplete()) {
                    sender.sendMessage(Text.color("&cSet pos1 and pos2 first."));
                    return true;
                }
                Cell created = this.cellService.createCell(args[1], tier, selection);
                if (created == null) {
                    sender.sendMessage(Text.color("&cFailed to create cell. Check positions."));
                    break;
                }
                sender.sendMessage(Text.color("&aCreated cell &f" + args[1] + " &7(" + tier.name() + ")."));
                break;
            }
            case "wand": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Players only.");
                    return true;
                }
                Player player = (Player)sender;
                player.getInventory().addItem(new ItemStack[]{this.cellService.createWand()});
                sender.sendMessage(Text.color("&aGiven a cell wand. Left click = pos1, right click = pos2."));
                break;
            }
            case "door": {
                CellDoorType type;
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Players only.");
                    return true;
                }
                Player player = (Player)sender;
                if (args.length < 4 || !args[1].equalsIgnoreCase("give")) {
                    sender.sendMessage(Text.color("&cUsage: /cell door give <BASIC|ELITE|LEGENDARY> <player> [amount]"));
                    return true;
                }
                try {
                    type = CellDoorType.valueOf(args[2].toUpperCase(Locale.ROOT));
                }
                catch (IllegalArgumentException e) {
                    sender.sendMessage(Text.color("&cInvalid door type."));
                    return true;
                }
                Player target = Bukkit.getPlayer((String)args[3]);
                if (target == null) {
                    sender.sendMessage(Text.color("&cPlayer not found."));
                    return true;
                }
                int amount = 1;
                if (args.length >= 5) {
                    try {
                        amount = Math.max(1, Integer.parseInt(args[4]));
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                }
                target.getInventory().addItem(new ItemStack[]{this.cellService.createDoorItem(type, amount)});
                sender.sendMessage(Text.color("&aGave " + amount + " " + type.name() + " door(s) to " + target.getName() + "."));
                break;
            }
            case "delete": {
                if (args.length < 2) {
                    sender.sendMessage(Text.color("&cUsage: /cell delete <id>"));
                    return true;
                }
                if (this.cellService.deleteCell(args[1])) {
                    sender.sendMessage(Text.color("&aDeleted cell &f" + args[1]));
                    break;
                }
                sender.sendMessage(Text.color("&cCell not found."));
                break;
            }
            case "info": {
                if (args.length < 2) {
                    sender.sendMessage(Text.color("&cUsage: /cell info <id>"));
                    return true;
                }
                Cell cell = this.cellService.getCell(args[1]);
                if (cell == null) {
                    sender.sendMessage(Text.color("&cCell not found."));
                    return true;
                }
                sender.sendMessage(Text.color("&eCell " + cell.getId() + " &7Tier: &f" + cell.getTier().name()));
                sender.sendMessage(Text.color("&7Owner: &f" + cell.ownerName()));
                sender.sendMessage(Text.color("&7Expires: &f" + this.cellService.expiryString(cell)));
                if (cell.getDoorType() == null) break;
                sender.sendMessage(Text.color("&7Door: &f" + cell.getDoorType().name()));
                break;
            }
            case "sign": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Players only.");
                    return true;
                }
                Player player = (Player)sender;
                if (args.length < 2) {
                    sender.sendMessage(Text.color("&cUsage: /cell sign <id> (look at a sign block)"));
                    return true;
                }
                Cell cell = this.cellService.getCell(args[1]);
                if (cell == null) {
                    sender.sendMessage(Text.color("&cCell not found."));
                    return true;
                }
                Block block = player.getTargetBlockExact(6);
                if (block == null || !(block.getState() instanceof Sign)) {
                    player.sendMessage(Text.color("&cLook at a sign block within 6 blocks."));
                    return true;
                }
                cell.setSignLocation(block.getLocation());
                this.cellService.saveData();
                this.cellService.updateCellSign(cell);
                player.sendMessage(Text.color("&aLinked sign to cell &f" + cell.getId() + "&a."));
                break;
            }
            case "setspawn": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Players only.");
                    return true;
                }
                Player player = (Player)sender;
                if (args.length < 2) {
                    sender.sendMessage(Text.color("&cUsage: /cell setspawn <id>"));
                    return true;
                }
                Cell cell = this.cellService.getCell(args[1]);
                if (cell == null) {
                    sender.sendMessage(Text.color("&cCell not found."));
                    return true;
                }
                if (this.cellService.setCellSpawn(cell, player.getLocation())) {
                    sender.sendMessage(Text.color("&aSet default spawn for cell &f" + cell.getId() + "&a."));
                    break;
                }
                sender.sendMessage(Text.color("&cFailed to set cell spawn."));
                break;
            }
            case "givebuster": {
                if (args.length < 2) {
                    sender.sendMessage(Text.color("&cUsage: /cell givebuster <player>"));
                    return true;
                }
                Player target = Bukkit.getPlayer((String)args[1]);
                if (target == null) {
                    sender.sendMessage(Text.color("&cPlayer not found."));
                    return true;
                }
                ItemStack buster = this.cellService.createCellBuster();
                target.getInventory().addItem(new ItemStack[]{buster});
                sender.sendMessage(Text.color("&aGave a Cell Buster to " + target.getName()));
                break;
            }
            default: {
                sender.sendMessage(Text.color("&cUnknown subcommand."));
            }
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            ArrayList<String> base = new ArrayList<String>(List.of("access", "owner", "disband", "home", "sethome"));
            if (sender.hasPermission("cells.admin")) {
                base.addAll(List.of("pos1", "pos2", "create", "door", "delete", "info", "givebuster", "wand", "sign", "setspawn", "guard"));
            }
            return base.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT))).collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("access") && sender instanceof Player) {
            return null;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            return Arrays.stream(CellTier.values()).map(Enum::name).collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("door")) {
            return List.of("give");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("owner")) {
            return null;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("sign")) {
            return this.cellService.getCells().stream().map(Cell::getId).collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("setspawn")) {
            return this.cellService.getCells().stream().map(Cell::getId).collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("door")) {
            return Arrays.stream(CellDoorType.values()).map(Enum::name).collect(Collectors.toList());
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("door")) {
            return null;
        }
        if (args.length == 5 && args[0].equalsIgnoreCase("door")) {
            return List.of("1", "2", "3");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("guard")) {
            return List.of("give");
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("guard")) {
            return Arrays.stream(CellGuardTier.values()).map(Enum::name).collect(Collectors.toList());
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("guard")) {
            return null;
        }
        if (args.length == 5 && args[0].equalsIgnoreCase("guard")) {
            return List.of("1", "2", "3", "4", "5");
        }
        return List.of();
    }

    private boolean confirm(Player player, PendingAction action, Object token) {
        long now = System.currentTimeMillis();
        PendingConfirm pending = this.pendingConfirms.get(player.getUniqueId());
        if (pending != null && pending.action == action && pending.token.equals(token) && now - pending.createdAt < 30000L) {
            this.pendingConfirms.remove(player.getUniqueId());
            return true;
        }
        this.pendingConfirms.put(player.getUniqueId(), new PendingConfirm(action, token, now));
        return false;
    }

    private static enum PendingAction {
        TRANSFER,
        DISBAND;

    }

    private record PendingConfirm(PendingAction action, Object token, long createdAt) {
    }
}

