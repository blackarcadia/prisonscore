package org.axial.prisonsCore.listener;

import org.axial.prisonsCore.util.Text;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionMessageListener implements Listener {
    private static final String BORDER = "&d&l&m================&6&l&m================";

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.joinMessage(null);
        String player = event.getPlayer().getName();
        event.getPlayer().sendMessage(Text.color(
                "\n"
                + BORDER + "&r\n"
                + Text.center("&fWelcome &d&l" + player + " &fto &d&lAxial&6&lPrisons") + "\n"
                + Text.center("&7Interstellar Themed Minecraft Prisons Experience!") + "\n"
                + "\n"
                + Text.center("&c&lDISCORD: &fdiscord.gg/axialmc") + "\n"
                + BORDER
        ));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.quitMessage(null);
    }
}
