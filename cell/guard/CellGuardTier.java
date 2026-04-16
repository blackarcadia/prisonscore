/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.inventory.ItemStack
 */
package org.axial.prisonsCore.cell.guard;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum CellGuardTier {
    BASIC("&f&lBasic Guard", 5, 500.0, Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, Material.STONE_SWORD),
    UNIQUE("&a&lUnique Guard", 6, 900.0, Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS, Material.STONE_SWORD),
    RARE("&9&lRare Guard", 7, 1000.0, Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS, Material.IRON_SWORD),
    ELITE("&e&lElite Guard", 8, 1500.0, Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS, Material.GOLDEN_SWORD),
    LEGENDARY("&6&lLegendary Guard", 10, 2000.0, Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS, Material.DIAMOND_SWORD);

    private final String displayName;
    private final int radius;
    private final double health;
    private final Material helmet;
    private final Material chest;
    private final Material legs;
    private final Material boots;
    private final Material weapon;

    private CellGuardTier(String displayName, int radius, double health, Material helmet, Material chest, Material legs, Material boots, Material weapon) {
        this.displayName = displayName;
        this.radius = radius;
        this.health = health;
        this.helmet = helmet;
        this.chest = chest;
        this.legs = legs;
        this.boots = boots;
        this.weapon = weapon;
    }

    public String displayName() {
        return this.displayName;
    }

    public int radius() {
        return this.radius;
    }

    public double health() {
        return this.health;
    }

    public ItemStack helmet() {
        return new ItemStack(this.helmet);
    }

    public ItemStack chest() {
        return new ItemStack(this.chest);
    }

    public ItemStack legs() {
        return new ItemStack(this.legs);
    }

    public ItemStack boots() {
        return new ItemStack(this.boots);
    }

    public ItemStack weapon() {
        return new ItemStack(this.weapon);
    }
}

