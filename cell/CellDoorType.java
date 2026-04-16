/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 */
package org.axial.prisonsCore.cell;

import org.bukkit.Material;

public enum CellDoorType {
    BASIC(Material.OAK_DOOR, 25),
    ELITE(Material.SPRUCE_DOOR, 50),
    LEGENDARY(Material.DARK_OAK_DOOR, 75);

    private final Material material;
    private final int hitsToBreak;

    private CellDoorType(Material material, int hitsToBreak) {
        this.material = material;
        this.hitsToBreak = hitsToBreak;
    }

    public Material getMaterial() {
        return this.material;
    }

    public int getHitsToBreak() {
        return this.hitsToBreak;
    }
}

