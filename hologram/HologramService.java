package org.axial.prisonsCore.hologram;

import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HologramService {
    private final PrisonsCore plugin;
    private final Map<String, Hologram> holograms = new ConcurrentHashMap<>();
    private final File file;
    private YamlConfiguration config;

    private boolean initialized = false;

    public HologramService(PrisonsCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "holograms.yml");
        this.load();
    }

    private void load() {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("holograms");
        if (section == null) {
            this.initialized = true;
            return;
        }

        for (String id : section.getKeys(false)) {
            Location loc = section.getLocation(id + ".location");
            List<String> lines = section.getStringList(id + ".lines");
            if (loc != null) {
                Hologram hologram = new Hologram(id, loc, lines);
                holograms.put(id, hologram);
            }
        }
        this.initialized = true;
    }

    public void save() {
        if (!this.initialized || config == null) {
            return;
        }
        config.set("holograms", null);
        for (Hologram holo : holograms.values()) {
            config.set("holograms." + holo.getId() + ".location", holo.getLocation());
            config.set("holograms." + holo.getId() + ".lines", holo.getLines());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void spawnAll() {
        for (Hologram holo : holograms.values()) {
            spawn(holo);
        }
    }

    public void despawnAll() {
        for (Hologram holo : holograms.values()) {
            despawn(holo);
        }
    }

    public void spawn(Hologram holo) {
        despawn(holo);
        Location loc = holo.getLocation();
        if (loc.getWorld() == null) return;

        TextDisplay display = loc.getWorld().spawn(loc, TextDisplay.class, td -> {
            td.text(Text.component(String.join("\n", holo.getLines())));
            td.setBillboard(Display.Billboard.CENTER);
            td.setShadowed(true);
            td.setBrightness(new Display.Brightness(15, 15));
            td.setPersistent(true);
        });
        holo.setEntityId(display.getUniqueId());
    }

    public void despawn(Hologram holo) {
        if (holo.getEntityId() != null) {
            Entity entity = Bukkit.getEntity(holo.getEntityId());
            if (entity != null) {
                entity.remove();
            }
            holo.setEntityId(null);
        }
        // Also cleanup orphaned entities at the location just in case
        Location loc = holo.getLocation();
        if (loc.getWorld() != null) {
            loc.getWorld().getNearbyEntities(loc, 0.1, 0.1, 0.1).stream()
                    .filter(e -> e instanceof TextDisplay)
                    .forEach(Entity::remove);
        }
    }

    public void createHologram(String id, Location location, List<String> lines) {
        Hologram holo = new Hologram(id, location, lines);
        holograms.put(id, holo);
        spawn(holo);
        save();
    }

    public void removeHologram(String id) {
        Hologram holo = holograms.remove(id);
        if (holo != null) {
            despawn(holo);
            save();
        }
    }

    public Hologram getHologram(String id) {
        return holograms.get(id);
    }

    public Collection<Hologram> getHolograms() {
        return holograms.values();
    }

    public void updateHologram(String id, List<String> lines) {
        Hologram holo = holograms.get(id);
        if (holo != null) {
            holo.setLines(new ArrayList<>(lines));
            spawn(holo);
            save();
        }
    }

    public void addLine(String id, String line) {
        Hologram holo = holograms.get(id);
        if (holo != null) {
            List<String> lines = new ArrayList<>(holo.getLines());
            lines.add(line);
            holo.setLines(lines);
            spawn(holo);
            save();
        }
    }

    public void removeLine(String id, int index) {
        Hologram holo = holograms.get(id);
        if (holo != null) {
            List<String> lines = new ArrayList<>(holo.getLines());
            if (index >= 0 && index < lines.size()) {
                lines.remove(index);
                holo.setLines(lines);
                spawn(holo);
                save();
            }
        }
    }
}
