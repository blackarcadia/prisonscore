/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.ChatColor
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.inventory.ItemFlag
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.persistence.PersistentDataContainer
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 */
package org.axial.prisonsCore.grappling;

import java.util.ArrayList;
import java.util.List;
import org.axial.prisonsCore.Keys;
import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.config.EnergyBarConfig;
import org.axial.prisonsCore.util.Lang;
import org.axial.prisonsCore.util.Text;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class GrapplingHookModule {
    private final PrisonsCore plugin;
    private final EnergyBarConfig barConfig;
    private NamespacedKey hookKey;
    private NamespacedKey chargeKey;
    private ItemStack grapplingHookItem;
    private long cooldownMillis;
    private double horizontalPower;
    private double verticalPower;
    private int maxCharge;
    private int chargePerUse;
    private String chargeLabel;

    public GrapplingHookModule(PrisonsCore plugin, EnergyBarConfig barConfig) {
        this.plugin = plugin;
        this.barConfig = barConfig;
    }

    public void reload() {
        this.hookKey = new NamespacedKey((Plugin)this.plugin, "grappling_hook");
        this.chargeKey = Keys.grapplingHookCharge(this.plugin);
        this.cooldownMillis = Math.max(0L, Math.round(this.plugin.getConfig().getDouble("grappling-hook.cooldown-seconds", 2.0) * 1000.0));
        this.horizontalPower = Math.max(0.0, this.plugin.getConfig().getDouble("grappling-hook.horizontal-power", 1.0));
        this.verticalPower = Math.max(0.0, this.plugin.getConfig().getDouble("grappling-hook.vertical-power", 1.0));
        this.maxCharge = Math.max(1, this.plugin.getConfig().getInt("grappling-hook.max-charge", 1000));
        this.chargePerUse = Math.max(1, this.plugin.getConfig().getInt("grappling-hook.charge-per-use", 25));
        this.chargeLabel = this.plugin.getConfig().getString("grappling-hook.charge-label", "&d&lGrapple Charge");
        this.grapplingHookItem = this.createGrapplingHookItem();
    }

    public ItemStack getGrapplingHookItem() {
        if (this.grapplingHookItem == null) {
            return new ItemStack(Material.AIR);
        }
        return this.grapplingHookItem.clone();
    }

    public long getCooldownMillis() {
        return this.cooldownMillis;
    }

    public double getHorizontalPower() {
        return this.horizontalPower;
    }

    public double getVerticalPower() {
        return this.verticalPower;
    }

    public boolean isGrapplingHook(ItemStack itemStack) {
        boolean added;
        if (itemStack == null || itemStack.getType() != Material.FISHING_ROD) {
            return false;
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return false;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        boolean isHook = container.has(this.hookKey, PersistentDataType.BYTE);
        if (isHook && (added = this.ensureChargeKey(itemStack, meta))) {
            this.updateLore(itemStack);
        }
        return isHook;
    }

    public int getCharge(ItemStack stack) {
        if (!this.isGrapplingHook(stack)) {
            return 0;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return 0;
        }
        Integer val = (Integer)meta.getPersistentDataContainer().get(this.chargeKey, PersistentDataType.INTEGER);
        return val == null ? 0 : Math.max(0, val);
    }

    public int getMaxCharge() {
        return this.maxCharge;
    }

    public int getChargePerUse() {
        return this.chargePerUse;
    }

    public void setCharge(ItemStack stack, int amount) {
        if (!this.isGrapplingHook(stack)) {
            return;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }
        meta.getPersistentDataContainer().set(this.chargeKey, PersistentDataType.INTEGER, Math.max(0, amount));
        stack.setItemMeta(meta);
        this.updateLore(stack);
    }

    public int addCharge(ItemStack stack, int amount) {
        if (!this.isGrapplingHook(stack) || amount == 0) {
            return this.getCharge(stack);
        }
        int newAmount = Math.max(0, this.getCharge(stack) + amount);
        this.setCharge(stack, newAmount);
        return newAmount;
    }

    public boolean consumeCharge(ItemStack stack, int amount) {
        if (amount <= 0) {
            return true;
        }
        int current = this.getCharge(stack);
        if (current < amount) {
            return false;
        }
        this.setCharge(stack, current - amount);
        return true;
    }

    private ItemStack createGrapplingHookItem() {
        ItemStack itemStack = new ItemStack(Material.FISHING_ROD);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return itemStack;
        }
        String display = Lang.msg("grappling.hook-display-name", "&bGrappling Hook");
        meta.setDisplayName(display.isEmpty() ? "Grappling Hook" : display);
        List<String> lore = Lang.list("grappling.hook-lore", List.of("&7Right-click to throw the hook,", "&7reel to pull yourself toward it."));
        if (!lore.isEmpty()) {
            meta.setLore(lore);
        }
        meta.getPersistentDataContainer().set(this.hookKey, PersistentDataType.BYTE, (byte)1);
        meta.getPersistentDataContainer().set(this.chargeKey, PersistentDataType.INTEGER, 0);
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
        meta.setUnbreakable(true);
        itemStack.setItemMeta(meta);
        this.updateLore(itemStack);
        return itemStack;
    }

    private boolean ensureChargeKey(ItemStack stack, ItemMeta meta) {
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(this.chargeKey, PersistentDataType.INTEGER)) {
            container.set(this.chargeKey, PersistentDataType.INTEGER, 0);
            stack.setItemMeta(meta);
            return true;
        }
        return false;
    }

    public void updateLore(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }
        this.ensureChargeKey(stack, meta);
        int charge = this.getCharge(stack);
        ArrayList<String> lore = new ArrayList<String>(Lang.list("grappling.hook-lore", List.of("&7Right-click to throw the hook,", "&7reel to pull yourself toward it.")));
        lore.add("");
        lore.add(Text.color(this.chargeLabel));
        double percent = Math.min(1.0, Math.max(0.0, (double)charge / (double)this.maxCharge));
        int filled = (int)Math.round(percent * (double)this.barConfig.getLength());
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < this.barConfig.getLength(); ++i) {
            if (i < filled) {
                bar.append(this.barConfig.getFilledColor()).append(this.barConfig.getFilledChar());
                continue;
            }
            bar.append(this.barConfig.getEmptyColor()).append(this.barConfig.getEmptyChar());
        }
        Object barLine = this.barConfig.getFormat().replace("{bar}", ChatColor.translateAlternateColorCodes((char)'&', (String)bar.toString())).replace("{current}", String.valueOf(charge)).replace("{max}", String.valueOf(this.maxCharge));
        double percentDisplay = (double)Math.round(percent * 1000.0) / 10.0;
        barLine = (String)barLine + Text.color(" &f&l" + percentDisplay + "%");
        lore.add(Text.color((String)barLine));
        boolean overflow = charge > this.maxCharge;
        lore.add(Text.color("&7(" + (overflow ? "&d" : "&f") + charge + " &7/ &f" + this.maxCharge + ")"));
        meta.setLore(lore);
        stack.setItemMeta(meta);
    }
}
