/*
 * Decompiled with CFR 0.152.
 */
package org.axial.prisonsCore.cell;

public enum CellTier {
    INMATE(45, 2500000.0),
    TRUSTEE(60, 5000000.0),
    WARDEN(80, 1.0E7);

    private final int requiredMiningLevel;
    private final double weeklyPrice;

    private CellTier(int requiredMiningLevel, double weeklyPrice) {
        this.requiredMiningLevel = requiredMiningLevel;
        this.weeklyPrice = weeklyPrice;
    }

    public int getRequiredMiningLevel() {
        return this.requiredMiningLevel;
    }

    public double getWeeklyPrice() {
        return this.weeklyPrice;
    }
}
