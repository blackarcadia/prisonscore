package org.axial.prisonsCore.hologram;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class Hologram {
    private final String id;
    private String worldName;
    private double x;
    private double y;
    private double z;
    private List<String> lines;
    private UUID entityId;

    public Hologram(String id, Location location, List<String> lines) {
        this(id, location == null || location.getWorld() == null ? null : location.getWorld().getName(), location == null ? 0.0 : location.getX(), location == null ? 0.0 : location.getY(), location == null ? 0.0 : location.getZ(), lines);
    }

    public Hologram(String id, String worldName, Location location, List<String> lines) {
        this(id, worldName, location == null ? 0.0 : location.getX(), location == null ? 0.0 : location.getY(), location == null ? 0.0 : location.getZ(), lines);
    }

    public Hologram(String id, String worldName, double x, double y, double z, List<String> lines) {
        this.id = id;
        this.worldName = worldName;
        this.lines = new ArrayList<>(lines);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getId() {
        return this.id;
    }

    public Location getLocation() {
        World world = this.worldName == null ? null : Bukkit.getWorld(this.worldName);
        return new Location(world, this.x, this.y, this.z);
    }

    public void setLocation(Location location) {
        if (location == null) {
            return;
        }
        this.worldName = location.getWorld() == null ? this.worldName : location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
    }

    public String getWorldName() {
        return this.worldName;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public List<String> getLines() {
        return this.lines;
    }

    public void setLines(List<String> lines) {
        this.lines = new ArrayList<>(lines);
    }

    public UUID getEntityId() {
        return this.entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }
}
