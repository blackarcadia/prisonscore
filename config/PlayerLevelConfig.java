/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 */
package org.axial.prisonsCore.config;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;

public class PlayerLevelConfig {
    private final int maxLevel;
    private final int prestigeStart;
    private final int baseExp;
    private final double multiplier;
    private final Map<Material, Integer> oreExp;

    public PlayerLevelConfig(int maxLevel, int prestigeStart, int baseExp, double multiplier, Map<Material, Integer> oreExp) {
        this.maxLevel = maxLevel;
        this.prestigeStart = prestigeStart;
        this.baseExp = baseExp;
        this.multiplier = multiplier;
        this.oreExp = oreExp;
    }

    public int getMaxLevel() {
        return this.maxLevel;
    }

    public int getBaseExp() {
        return this.baseExp;
    }

    public int getPrestigeStart() {
        return this.prestigeStart;
    }

    public double getMultiplier() {
        return this.multiplier;
    }

    public Map<Material, Integer> getOreExp() {
        return new HashMap<Material, Integer>(this.oreExp);
    }
}

