/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.util.Vector
 */
package org.axial.prisonsCore.cell;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class CellRegion {
    private final String worldName;
    private final Vector min;
    private final Vector max;

    public CellRegion(Location a, Location b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Locations cannot be null");
        }
        if (!a.getWorld().getName().equals(b.getWorld().getName())) {
            throw new IllegalArgumentException("Locations must be in the same world");
        }
        this.worldName = a.getWorld().getName();
        Vector va = a.toVector();
        Vector vb = b.toVector();
        this.min = new Vector(Math.min(va.getX(), vb.getX()), Math.min(va.getY(), vb.getY()), Math.min(va.getZ(), vb.getZ()));
        this.max = new Vector(Math.max(va.getX(), vb.getX()), Math.max(va.getY(), vb.getY()), Math.max(va.getZ(), vb.getZ()));
    }

    public CellRegion(String worldName, Vector min, Vector max) {
        this.worldName = worldName;
        this.min = min;
        this.max = max;
    }

    public boolean contains(Location loc) {
        if (loc == null) {
            return false;
        }
        World w = loc.getWorld();
        if (w == null || !w.getName().equalsIgnoreCase(this.worldName)) {
            return false;
        }
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        return x >= this.min.getX() && x <= this.max.getX() && y >= this.min.getY() && y <= this.max.getY() && z >= this.min.getZ() && z <= this.max.getZ();
    }

    public String getWorldName() {
        return this.worldName;
    }

    public Vector getMin() {
        return this.min;
    }

    public Vector getMax() {
        return this.max;
    }
}

