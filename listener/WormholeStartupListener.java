package org.axial.prisonsCore.listener;

import org.axial.prisonsCore.PrisonsCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;

public class WormholeStartupListener implements Listener {
    private final PrisonsCore plugin;

    public WormholeStartupListener(PrisonsCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        if (this.plugin.getWormholeEnchanter() != null) {
            this.plugin.getWormholeEnchanter().reload();
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (this.plugin.getWormholeEnchanter() != null) {
            this.plugin.getWormholeEnchanter().reload();
        }
    }
}
