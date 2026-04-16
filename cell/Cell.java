/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.OfflinePlayer
 */
package org.axial.prisonsCore.cell;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.axial.prisonsCore.cell.CellDoorType;
import org.axial.prisonsCore.cell.CellRegion;
import org.axial.prisonsCore.cell.CellTier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

public class Cell {
    private final String id;
    private final CellTier tier;
    private final CellRegion region;
    private Location doorLocation;
    private Location signLocation;
    private Location spawnLocation;
    private Location homeLocation;
    private CellDoorType doorType;
    private UUID owner;
    private long rentExpiry;
    private final Set<UUID> access = new HashSet<UUID>();

    public Cell(String id, CellTier tier, CellRegion region) {
        this.id = id;
        this.tier = tier;
        this.region = region;
    }

    public String getId() {
        return this.id;
    }

    public CellTier getTier() {
        return this.tier;
    }

    public CellRegion getRegion() {
        return this.region;
    }

    public Location getDoorLocation() {
        return this.doorLocation;
    }

    public void setDoorLocation(Location doorLocation) {
        this.doorLocation = doorLocation;
    }

    public Location getSignLocation() {
        return this.signLocation;
    }

    public void setSignLocation(Location signLocation) {
        this.signLocation = signLocation;
    }

    public Location getSpawnLocation() {
        return this.spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public Location getHomeLocation() {
        return this.homeLocation;
    }

    public void setHomeLocation(Location homeLocation) {
        this.homeLocation = homeLocation;
    }

    public CellDoorType getDoorType() {
        return this.doorType;
    }

    public void setDoorType(CellDoorType doorType) {
        this.doorType = doorType;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public long getRentExpiry() {
        return this.rentExpiry;
    }

    public void setRentExpiry(long rentExpiry) {
        this.rentExpiry = rentExpiry;
    }

    public Set<UUID> getAccess() {
        return this.access;
    }

    public boolean hasDoorConfigured() {
        return this.doorLocation != null && this.doorType != null;
    }

    public boolean isOwned() {
        return this.owner != null;
    }

    public boolean isInside(Location location) {
        return this.region.contains(location);
    }

    public String ownerName() {
        if (this.owner == null) {
            return "None";
        }
        OfflinePlayer offline = Bukkit.getOfflinePlayer((UUID)this.owner);
        return offline.getName() == null ? this.owner.toString() : offline.getName();
    }
}

