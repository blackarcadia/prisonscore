/*
 * Decompiled with CFR 0.152.
 */
package org.axial.prisonsCore.config;

public class LevelConfig {
    private final int startLevel;
    private final double percentPerLevel;

    public LevelConfig(int startLevel, double percentPerLevel) {
        this.startLevel = startLevel;
        this.percentPerLevel = percentPerLevel;
    }

    public int getStartLevel() {
        return this.startLevel;
    }

    public double getPercentPerLevel() {
        return this.percentPerLevel;
    }
}

