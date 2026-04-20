package org.axial.prisonsCore.listener;

import org.axial.prisonsCore.PrisonsCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;

public class HologramStartupListener implements Listener {
    private final PrisonsCore plugin;

    public HologramStartupListener(PrisonsCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        if (this.plugin.getHologramService() != null) {
            this.plugin.getHologramService().spawnAll();
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (this.plugin.getHologramService() != null) {
            this.plugin.getHologramService().spawnLoadedWorld(event.getWorld());
        }
    }
}
