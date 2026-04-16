/*
 * Decompiled with CFR 0.152.
 */
package org.axial.prisonsCore.config;

public class EnergyBarConfig {
    private final int length;
    private final String filledChar;
    private final String emptyChar;
    private final String filledColor;
    private final String emptyColor;
    private final String format;

    public EnergyBarConfig(int length, String filledChar, String emptyChar, String filledColor, String emptyColor, String format) {
        this.length = length;
        this.filledChar = filledChar;
        this.emptyChar = emptyChar;
        this.filledColor = filledColor;
        this.emptyColor = emptyColor;
        this.format = format;
    }

    public int getLength() {
        return this.length;
    }

    public String getFilledChar() {
        return this.filledChar;
    }

    public String getEmptyChar() {
        return this.emptyChar;
    }

    public String getFilledColor() {
        return this.filledColor;
    }

    public String getEmptyColor() {
        return this.emptyColor;
    }

    public String getFormat() {
        return this.format;
    }
}

