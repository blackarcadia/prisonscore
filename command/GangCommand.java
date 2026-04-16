/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 */
package org.axial.prisonsCore.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.axial.prisonsCore.model.Gang;
import org.axial.prisonsCore.model.GangRole;
import org.axial.prisonsCore.service.GangService;
import org.axial.prisonsCore.util.Lang;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class GangCommand
implements CommandExecutor,
TabCompleter {
    private final GangService gangService;

    public GangCommand(GangService gangService) {
        this.gangService = gangService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub;
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        Player player = (Player)sender;
        if (args.length == 0) {
            List<String> usageLines = Lang.list("gang.usage", List.of("&e/gang create <name>", "&e/gang invite <player>", "&e/gang join <name>", "&e/gang leave", "&e/gang rename <name>", "&e/gang promote <player>", "&e/gang demote <player>", "&e/g enemy <gang|player>", "&e/g neutral <gang|player>", "&e/g chat <g|p>", "&e/gang points add <gang> <amount>", "&e/gang top", "&e/gang info"));
            usageLines.forEach(arg_0 -> ((CommandSender)sender).sendMessage(arg_0));
            return true;
        }
        switch (sub = args[0].toLowerCase(Locale.ROOT)) {
            case "top": {
                List<Gang> top = this.gangService.topByPoints(10);
                this.msg(player, Lang.msg("gang.top.title", "&d&lGang Top"));
                int i = 1;
                for (Gang g : top) {
                    this.msg(player, Lang.msg("gang.top.line", "&f{pos}. &b{name} &7- &d{points} pts").replace("{pos}", String.valueOf(i++)).replace("{name}", g.getName()).replace("{points}", String.valueOf(g.getPoints())));
                }
                break;
            }
            case "g": 
            case "info": {
                Gang gang = this.gangService.getGangByPlayer(player.getUniqueId());
                if (gang == null) {
                    this.msg(player, Lang.msg("gang.not-in-gang", "&cYou are not in a gang."));
                    return true;
                }
                this.msg(player, Lang.msg("gang.info.title", "&d&lGang Info:"));
                this.msg(player, Lang.msg("gang.info.name", "&7Name: &b{name}").replace("{name}", gang.getName()));
                this.msg(player, Lang.msg("gang.info.points", "&7Points: &d{points}").replace("{points}", String.valueOf(gang.getPoints())));
                String members = gang.getMembers().entrySet().stream().map(e -> Bukkit.getOfflinePlayer((UUID)((UUID)e.getKey())).getName() + " (" + ((GangRole)((Object)((Object)e.getValue()))).name().toLowerCase() + ")").collect(Collectors.joining(", "));
                this.msg(player, Lang.msg("gang.info.members", "&7Members: &f{members}").replace("{members}", members.isEmpty() ? Lang.msg("gang.info.none", "None") : members));
                this.msg(player, Lang.msg("gang.info.truces", "&7Truces: &a{truces}").replace("{truces}", this.formatList(this.gangService.getRelated(gang.getId(), GangService.Relation.TRUCE), Lang.msg("gang.info.none", "None"))));
                this.msg(player, Lang.msg("gang.info.enemies", "&7Enemies: &c{enemies}").replace("{enemies}", this.formatList(this.gangService.getRelated(gang.getId(), GangService.Relation.ENEMY), Lang.msg("gang.info.none", "None"))));
                break;
            }
            case "create": {
                if (args.length < 2) {
                    this.msg(player, Lang.msg("gang.create.usage", "&cUsage: /gang create <name>"));
                    return true;
                }
                Gang g = this.gangService.createGang(player, String.join((CharSequence)" ", args).substring(args[0].length() + 1));
                if (g == null) {
                    this.msg(player, Lang.msg("gang.create.fail", "&cCould not create gang (name taken or you're already in a gang)."));
                    break;
                }
                this.msg(player, Lang.msg("gang.create.success", "&aCreated gang &f{name}").replace("{name}", g.getName()));
                break;
            }
            case "invite": {
                if (args.length < 2) {
                    this.msg(player, Lang.msg("gang.invite.usage", "&cUsage: /gang invite <player>"));
                    return true;
                }
                Gang gang = this.gangService.getGangByPlayer(player.getUniqueId());
                if (gang == null) {
                    this.msg(player, Lang.msg("gang.not-in-gang", "&cYou are not in a gang."));
                    return true;
                }
                Player target = Bukkit.getPlayer((String)args[1]);
                if (target == null) {
                    this.msg(player, Lang.msg("gang.player-not-found", "&cPlayer not found."));
                    return true;
                }
                if (this.gangService.invite(gang, player, target)) {
                    this.msg(player, Lang.msg("gang.invite.sent", "&aInvited {name}").replace("{name}", target.getName()));
                    break;
                }
                this.msg(player, Lang.msg("gang.invite.fail", "&cCannot invite that player."));
                break;
            }
            case "join": {
                if (args.length < 2) {
                    this.msg(player, Lang.msg("gang.join.usage", "&cUsage: /gang join <name>"));
                    return true;
                }
                Gang gang = this.gangService.getGang(args[1]);
                if (gang == null) {
                    this.msg(player, Lang.msg("gang.not-found", "&cGang not found."));
                    return true;
                }
                if (this.gangService.joinGang(player, gang)) {
                    this.msg(player, Lang.msg("gang.join.success", "&aJoined gang &f{name}").replace("{name}", gang.getName()));
                    break;
                }
                this.msg(player, Lang.msg("gang.join.fail", "&cCannot join gang (maybe already in one?)"));
                break;
            }
            case "leave": {
                if (this.gangService.leaveGang(player)) {
                    this.msg(player, Lang.msg("gang.leave.success", "&aYou left your gang."));
                    break;
                }
                this.msg(player, Lang.msg("gang.not-in-gang", "&cYou are not in a gang."));
                break;
            }
            case "rename": {
                if (args.length < 2) {
                    this.msg(player, Lang.msg("gang.rename.usage", "&cUsage: /gang rename <name>"));
                    return true;
                }
                Gang gang = this.gangService.getGangByPlayer(player.getUniqueId());
                if (gang == null) {
                    this.msg(player, Lang.msg("gang.not-in-gang", "&cYou are not in a gang."));
                    return true;
                }
                if (gang.getRole(player.getUniqueId()) != GangRole.LEADER) {
                    this.msg(player, Lang.msg("gang.rename.no-perm", "&cOnly the leader can rename."));
                    return true;
                }
                this.gangService.renameGang(gang, String.join((CharSequence)" ", args).substring(args[0].length() + 1));
                this.msg(player, Lang.msg("gang.rename.success", "&aGang renamed."));
                break;
            }
            case "promote": {
                if (args.length < 2) {
                    this.msg(player, Lang.msg("gang.promote.usage", "&cUsage: /gang promote <player>"));
                    return true;
                }
                Gang gang = this.gangService.getGangByPlayer(player.getUniqueId());
                if (gang == null) {
                    this.msg(player, Lang.msg("gang.not-in-gang", "&cYou are not in a gang."));
                    return true;
                }
                Player target = Bukkit.getPlayer((String)args[1]);
                if (target == null) {
                    this.msg(player, Lang.msg("gang.player-not-found", "&cPlayer not found."));
                    return true;
                }
                if (!this.gangService.promote(gang, player, target)) {
                    this.msg(player, Lang.msg("gang.promote.fail", "&cCannot promote that player."));
                    break;
                }
                this.msg(player, Lang.msg("gang.promote.success", "&aPromoted {name}").replace("{name}", target.getName()));
                break;
            }
            case "demote": {
                if (args.length < 2) {
                    this.msg(player, Lang.msg("gang.demote.usage", "&cUsage: /gang demote <player>"));
                    return true;
                }
                Gang gang = this.gangService.getGangByPlayer(player.getUniqueId());
                if (gang == null) {
                    this.msg(player, Lang.msg("gang.not-in-gang", "&cYou are not in a gang."));
                    return true;
                }
                Player target = Bukkit.getPlayer((String)args[1]);
                if (target == null) {
                    this.msg(player, Lang.msg("gang.player-not-found", "&cPlayer not found."));
                    return true;
                }
                if (!this.gangService.demote(gang, player, target)) {
                    this.msg(player, Lang.msg("gang.demote.fail", "&cCannot demote that player."));
                    break;
                }
                this.msg(player, Lang.msg("gang.demote.success", "&aDemoted {name}").replace("{name}", target.getName()));
                break;
            }
            case "relation": {
                this.msg(player, Lang.msg("gang.relation.invalid", "&cInvalid relation command."));
                return true;
            }
            case "enemy": 
            case "neutral": {
                this.handleRelation(player, args, sub.equals("enemy") ? GangService.Relation.ENEMY : GangService.Relation.NEUTRAL);
                break;
            }
            case "points": {
                int amt;
                if (!player.hasPermission("prisons.gang.admin")) {
                    this.msg(player, Lang.msg("gang.no-permission", "&cNo permission."));
                    return true;
                }
                if (args.length < 4 || !args[1].equalsIgnoreCase("add")) {
                    this.msg(player, Lang.msg("gang.points.usage", "&cUsage: /gang points add <gang> <amount>"));
                    return true;
                }
                Gang target = this.gangService.getGang(args[2]);
                if (target == null) {
                    this.msg(player, Lang.msg("gang.not-found", "&cGang not found."));
                    return true;
                }
                try {
                    amt = Integer.parseInt(args[3]);
                }
                catch (NumberFormatException ex) {
                    this.msg(player, Lang.msg("gang.points.invalid-amount", "&cInvalid amount."));
                    return true;
                }
                this.gangService.addPoints(target.getId(), amt);
                this.msg(player, Lang.msg("gang.points.added", "&aAdded {amount} points to {gang}").replace("{amount}", String.valueOf(amt)).replace("{gang}", target.getName()));
                break;
            }
            case "chat": {
                if (args.length < 2) {
                    this.msg(player, "&cUsage: /g chat <g|p>");
                    return true;
                }
                if (args[1].equalsIgnoreCase("g")) {
                    this.gangService.setGangChat(player.getUniqueId(), true);
                    this.msg(player, "&aGang chat enabled.");
                    break;
                }
                if (args[1].equalsIgnoreCase("p")) {
                    this.gangService.setGangChat(player.getUniqueId(), false);
                    this.msg(player, "&aPublic chat enabled.");
                    break;
                }
                this.msg(player, "&cUsage: /g chat <g|p>");
                break;
            }
            default: {
                this.msg(player, "&cUnknown subcommand.");
            }
        }
        return true;
    }

    private void msg(Player p, String m) {
        p.sendMessage(Text.color(m));
    }

    private void handleRelation(Player player, String[] args, GangService.Relation relation) {
        Player targetPlayer;
        if (args.length < 2) {
            this.msg(player, Lang.msg("gang.relation.usage", "&cUsage: /g " + (relation == GangService.Relation.ENEMY ? "enemy" : "neutral") + " <gang|player>"));
            return;
        }
        Gang gang = this.gangService.getGangByPlayer(player.getUniqueId());
        if (gang == null) {
            this.msg(player, Lang.msg("gang.not-in-gang", "&cYou are not in a gang."));
            return;
        }
        GangRole role = gang.getRole(player.getUniqueId());
        if (role == null || !role.isHigherOrEqual(GangRole.CO_LEADER)) {
            this.msg(player, Lang.msg("gang.relation.no-perm", "&cInsufficient rank."));
            return;
        }
        Gang targetGang = this.gangService.getGang(args[1]);
        if (targetGang == null && (targetPlayer = Bukkit.getPlayer((String)args[1])) != null) {
            targetGang = this.gangService.getGangByPlayer(targetPlayer.getUniqueId());
        }
        if (targetGang == null) {
            this.msg(player, Lang.msg("gang.not-found", "&cGang not found."));
            return;
        }
        this.gangService.setRelation(gang.getId(), targetGang.getId(), relation);
        this.msg(player, Lang.msg("gang.relation.set", "&aSet relation with {gang} to {rel}").replace("{gang}", targetGang.getName()).replace("{rel}", relation.name().toLowerCase()));
    }

    private String formatList(List<String> list, String none) {
        if (list == null || list.isEmpty()) {
            return Text.color(none);
        }
        return String.join((CharSequence)", ", list);
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            List<String> subs = List.of("create", "invite", "join", "leave", "rename", "promote", "demote", "enemy", "neutral", "points", "top", "info", "g", "chat");
            out = subs.stream().filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT))).collect(Collectors.toList());
        } else if (args.length == 2) {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "invite": 
                case "promote": 
                case "demote": {
                    out = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
                    break;
                }
                case "join": 
                case "enemy": 
                case "neutral": {
                    out = new ArrayList<>(this.gangServiceTopNames());
                    break;
                }
                case "points": {
                    out = new ArrayList<>(List.of("add"));
                    break;
                }
                case "chat": {
                    out = new ArrayList<>(List.of("g", "p"));
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("points")) {
            out = new ArrayList<>(this.gangServiceTopNames());
        }
        String prefix = args[args.length - 1].toLowerCase(Locale.ROOT);
        return out.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(prefix)).collect(Collectors.toList());
    }

    private List<String> gangServiceTopNames() {
        return this.gangService.topByPoints(50).stream().map(Gang::getName).collect(Collectors.toList());
    }
}
