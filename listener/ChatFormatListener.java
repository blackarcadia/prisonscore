package org.axial.prisonsCore.listener;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.axial.prisonsCore.service.GangService;
import org.axial.prisonsCore.service.PlayerLevelService;
import org.axial.prisonsCore.service.PlayerToggleService;
import org.axial.prisonsCore.service.TutorialService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatFormatListener implements Listener {
    private static final String DEFAULT_FORMAT = "&8[{prison_level_colored}&8] &f&l[&b&lBETA&f&l] &b{playername}&f: &f{message}";
    private static final String HELPER_FORMAT = "&8[{prison_level_colored}&8] &f&l[&5&lHelper&f&l] &5{playername}&f: &a{message}";
    private static final String MOD_FORMAT = "&8[{prison_level_colored}&8] &f&l[&a&lMod&f&l] &a{playername}&f: &2{message}";
    private static final String ADMIN_FORMAT = "&8[{prison_level_colored}&8] &f&l[&4&lAdmin&f&l] &4{playername}&f: &c{message}";
    private static final String DEV_FORMAT = "&8[{prison_level_colored}&8] &f&l[&4&lDev&f&l] &4{playername}&f: &c{message}";

    private final PlayerLevelService playerLevelService;
    private final GangService gangService;
    private final TutorialService tutorialService;
    private final PlayerToggleService toggleService;

    public ChatFormatListener(PlayerLevelService playerLevelService, GangService gangService, TutorialService tutorialService, PlayerToggleService toggleService) {
        this.playerLevelService = playerLevelService;
        this.gangService = gangService;
        this.tutorialService = tutorialService;
        this.toggleService = toggleService;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onLegacyChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (event.isCancelled() || this.gangService.isGangChat(player.getUniqueId())) {
            return;
        }
        event.getRecipients().removeIf(recipient -> recipient instanceof Player target && !target.equals(player) && (!this.canSeeChat(target) || this.shouldFilterChat(target)));
        event.setFormat(this.resolveLegacyFormat(player));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (event.isCancelled() || this.gangService.isGangChat(player.getUniqueId())) {
            return;
        }
        event.viewers().removeIf(viewer -> viewer instanceof Player target && !target.equals(player) && (!this.canSeeChat(target) || this.shouldFilterChat(target)));
        event.renderer(ChatRenderer.viewerUnaware((source, sourceDisplayName, message) -> this.renderModernFormat(player, message)));
    }

    private String resolveLegacyFormat(Player player) {
        return Text.color(this.resolveTemplate(player).replace("{message}", "%2$s"));
    }

    private Component renderModernFormat(Player player, Component message) {
        String resolved = this.resolveTemplate(player);
        int messageAt = resolved.indexOf("{message}");
        if (messageAt < 0) {
            return Text.componentNoItalics(resolved).append(message);
        }
        String prefix = resolved.substring(0, messageAt);
        String suffix = resolved.substring(messageAt + "{message}".length());
        Component built = Text.componentNoItalics(prefix).append(message);
        if (!suffix.isEmpty()) {
            built = built.append(Text.componentNoItalics(suffix));
        }
        return built;
    }

    private String resolveTemplate(Player player) {
        return this.templateFor(player)
                .replace("{prison_level_colored}", this.coloredLevel(player))
                .replace("{playername}", player.getName());
    }

    private String templateFor(Player player) {
        if (player.hasPermission("prisons.dev")) {
            return DEV_FORMAT;
        }
        if (player.hasPermission("prisons.admin")) {
            return ADMIN_FORMAT;
        }
        if (player.hasPermission("prisons.mod") || player.hasPermission("prisons.moderator")) {
            return MOD_FORMAT;
        }
        if (player.hasPermission("prisons.helper")) {
            return HELPER_FORMAT;
        }
        return DEFAULT_FORMAT;
    }

    private String coloredLevel(Player player) {
        int level = this.playerLevelService.getLevel(player);
        return this.colorForLevel(level) + level;
    }

    private String colorForLevel(int level) {
        if (level >= 100) {
            return "\u00a7c";
        }
        if (level >= 60) {
            return "\u00a76";
        }
        if (level >= 40) {
            return "\u00a7b";
        }
        if (level >= 20) {
            return "\u00a79";
        }
        return "\u00a77";
    }

    private boolean canSeeChat(Player player) {
        return this.toggleService == null || this.toggleService.canReceiveChat(player);
    }

    private boolean shouldFilterChat(Player player) {
        return this.tutorialService != null && this.tutorialService.shouldFilterChat(player);
    }
}
