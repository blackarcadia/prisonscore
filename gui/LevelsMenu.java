/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryCloseEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.InventoryHolder
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package org.axial.prisonsCore.gui;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.service.PlayerLevelService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class LevelsMenu
implements Listener {
    private final PrisonsCore plugin;
    private final PlayerLevelService levelService;
    private List<MenuEntry> entries;
    private List<Integer> progressSlots;
    private String title;
    private Material filler;
    private String progressUnlocked;
    private String progressLocked;
    private final NumberFormat expFormat = new DecimalFormat("#,###");

    public LevelsMenu(PrisonsCore plugin, PlayerLevelService levelService) {
        this.plugin = plugin;
        this.levelService = levelService;
        this.reloadFromConfig();
    }

    public void open(Player player) {
        Inventory inv;
        this.reloadFromConfig();
        Holder holder = new Holder(player.getUniqueId());
        holder.inv = inv = Bukkit.createInventory((InventoryHolder)holder, (int)54, (String)this.title);
        this.fill(inv, this.filler);
        this.placeEntries(inv, this.levelService.getLevel(player));
        this.updateProgress(holder);
        holder.task = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> this.updateProgress(holder), 20L, 20L);
        player.openInventory(inv);
    }

    @EventHandler(ignoreCancelled=true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof Holder)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        InventoryHolder inventoryHolder = event.getInventory().getHolder();
        if (!(inventoryHolder instanceof Holder)) {
            return;
        }
        Holder holder = (Holder)inventoryHolder;
        if (holder.task != null) {
            holder.task.cancel();
        }
    }

    private void fill(Inventory inv, Material mat) {
        Material material = mat == null ? Material.GRAY_STAINED_GLASS_PANE : mat;
        ItemStack pane = this.pane(material, " ");
        for (int i = 0; i < inv.getSize(); ++i) {
            inv.setItem(i, pane);
        }
    }

    private void placeEntries(Inventory inv, int playerLevel) {
        for (MenuEntry entry : this.entries) {
            if (entry.slot() < 0 || entry.slot() >= inv.getSize()) continue;
            ItemStack item = new ItemStack(entry.material());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                boolean unlocked = playerLevel >= entry.level();
                String display = (unlocked ? "&r&A&lLevel &r&f&l" : "&r&C&lLevel &r&f&l") + entry.level();
                meta.displayName(Text.componentNoItalics(display));
                long expNeeded = this.expForLevel(entry.level());
                String formattedExp = this.formatExp(expNeeded);
                String status = unlocked ? "&a&lUNLOCKED" : "&c&lLOCKED";
                String rewardPrefix = unlocked ? "&a&l* " : "&c&l* ";
                ArrayList<Component> lore = new ArrayList<Component>();
                for (String line : entry.lore()) {
                    lore.add(Text.componentNoItalics(line.replace("{level}", String.valueOf(entry.level())).replace("{material}", entry.material().name()).replace("{exp}", formattedExp).replace("{requiredexp}", formattedExp).replace("{status}", status).replace("{rewardprefix}", rewardPrefix)));
                }
                meta.lore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(entry.slot(), item);
        }
    }

    private void updateProgress(Holder holder) {
        if (holder.inv == null) {
            return;
        }
        Player player = Bukkit.getPlayer((UUID)holder.owner);
        if (player == null) {
            if (holder.task != null) {
                holder.task.cancel();
            }
            return;
        }
        int lvl = this.levelService.getLevel(player);
        int count = Math.min(this.entries.size(), this.progressSlots.size());
        for (int i = 0; i < count; ++i) {
            int progressSlot = this.progressSlots.get(i);
            if (progressSlot < 0 || progressSlot >= holder.inv.getSize()) continue;
            MenuEntry entry = this.entries.get(i);
            boolean unlocked = lvl >= entry.level();
            holder.inv.setItem(progressSlot, this.progressPane(unlocked, entry.level()));
        }
    }

    private ItemStack progressPane(boolean unlocked, int level) {
        Material mat = unlocked ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        String name = unlocked ? this.progressUnlocked : this.progressLocked;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Text.component(name));
            long expNeeded = this.expForLevel(level);
            meta.lore(List.of(Text.componentNoItalics("&7(&f" + this.formatExp(expNeeded) + " &7XP)")));
            item.setItemMeta(meta);
        }
        return item;
    }

    private String formatExp(long exp) {
        return this.expFormat.format(Math.max(0L, exp));
    }

    private ItemStack pane(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Text.component(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    private long expForLevel(int level) {
        int base = this.levelService.getConfig().getBaseExp();
        double mult = this.levelService.getConfig().getMultiplier();
        long total = 0L;
        for (int i = 1; i < level; ++i) {
            total += Math.round((double)base * Math.pow(mult, i - 1));
        }
        return Math.max(0L, total);
    }

    private void reloadFromConfig() {
        ConfigurationSection section = this.plugin.getConfig().getConfigurationSection("levels-menu");
        if (section == null) {
            this.entries = this.buildDefaultEntries();
            this.progressSlots = this.buildDefaultProgressSlots();
            this.title = Text.color("&8Mining Levels");
            this.filler = Material.GRAY_STAINED_GLASS_PANE;
            this.progressUnlocked = Text.color("&aUnlocked");
            this.progressLocked = Text.color("&cLocked");
            return;
        }
        this.title = Text.color(section.getString("title", "&8Mining Levels"));
        Material fillerMat = Material.matchMaterial((String)section.getString("filler", "GRAY_STAINED_GLASS_PANE"));
        this.filler = fillerMat != null ? fillerMat : Material.GRAY_STAINED_GLASS_PANE;
        this.progressUnlocked = Text.color(section.getString("progress-unlocked", "&aUnlocked"));
        this.progressLocked = Text.color(section.getString("progress-locked", "&cLocked"));
        List slots = section.getIntegerList("progress-slots");
        this.progressSlots = slots == null || slots.isEmpty() ? this.buildDefaultProgressSlots() : slots;
        this.entries = this.loadEntries(section.getMapList("entries"));
    }

    private List<MenuEntry> loadEntries(List<Map<?, ?>> raw) {
        if (raw == null || raw.isEmpty()) {
            return this.buildDefaultEntries();
        }
        ArrayList<MenuEntry> list = new ArrayList<MenuEntry>();
        for (Map<?, ?> map : raw) {
            Object nameObj;
            int slot = this.safeInt(map.get("slot"), 0);
            int level = this.safeInt(map.get("level"), 1);
            Object materialObj = map.get("material");
            Material mat = Material.matchMaterial((String)(materialObj != null ? materialObj.toString() : "STONE"));
            if (mat == null) {
                mat = Material.STONE;
            }
            String name = (nameObj = map.get("name")) != null ? nameObj.toString() : "&fLevel {level}";
            Object loreObj = map.get("lore");
            ArrayList<String> lore = new ArrayList();
            if (loreObj instanceof List) {
                List listLore = (List)loreObj;
                for (Object line : listLore) {
                    lore.add(String.valueOf(line));
                }
            } else {
                lore = (ArrayList<String>) Collections.singletonList("&7Requirement: Level {level}");
            }
            list.add(new MenuEntry(slot, level, mat, name, lore));
        }
        list.sort(Comparator.comparingInt(MenuEntry::slot));
        return list;
    }

    private int safeInt(Object value, int def) {
        if (value instanceof Number) {
            Number num = (Number)value;
            return num.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        }
        catch (Exception e) {
            return def;
        }
    }

    private List<MenuEntry> buildDefaultEntries() {
        ArrayList<MenuEntry> list = new ArrayList<MenuEntry>();
        list.add(new MenuEntry(0, 1, Material.WOODEN_PICKAXE, "&fLevel {level}", Collections.singletonList("&7Unlocks {material}")));
        list.add(new MenuEntry(1, 5, Material.COAL_ORE, "&fLevel {level}", Collections.singletonList("&7Unlocks {material}")));
        list.add(new MenuEntry(2, 10, Material.COAL_ORE, "&fLevel {level}", Collections.singletonList("&7Unlocks {material}")));
        list.add(new MenuEntry(3, 15, Material.COAL_ORE, "&fLevel {level}", Collections.singletonList("&7Unlocks {material}")));
        list.add(new MenuEntry(4, 20, Material.IRON_ORE, "&fLevel {level}", Collections.singletonList("&7Unlocks {material}")));
        list.add(new MenuEntry(5, 25, Material.IRON_ORE, "&fLevel {level}", Collections.singletonList("&7Unlocks {material}")));
        list.add(new MenuEntry(6, 30, Material.LAPIS_ORE, "&fLevel {level}", Collections.singletonList("&7Unlocks {material}")));
        list.add(new MenuEntry(7, 35, Material.LAPIS_ORE, "&fLevel {level}", Collections.singletonList("&7Unlocks {material}")));
        list.add(new MenuEntry(8, 40, Material.GOLD_ORE, "&fLevel {level}", Collections.singletonList("&7Unlocks {material}")));
        list.add(new MenuEntry(18, 45, Material.GOLD_ORE, "&fLevel {level}", Collections.singletonList("&7Unlocks {material}")));
        list.add(new MenuEntry(19, 50, Material.GOLD_ORE, "&fLevel {level}", Collections.singletonList("&7Unlocks {material}")));
        list.add(new MenuEntry(20, 55, Material.GOLD_ORE, "&fLevel {level}", Collections.singletonList("&7Unlocks {material}")));
        list.add(new MenuEntry(21, 60, Material.REDSTONE_ORE, "&fLevel {level}", Collections.singletonList("&7Unlocks {material}")));
        list.add(new MenuEntry(22, 65, Material.REDSTONE_ORE, "&fLevel {level}", Collections.singletonList("&7Unlocks {material}")));
        list.add(new MenuEntry(23, 70, Material.DIAMOND_ORE, "&fLevel {level}", Collections.singletonList("&7Unlocks {material}")));
        list.add(new MenuEntry(24, 75, Material.DIAMOND_ORE, "&fLevel {level}", Collections.singletonList("&7Unlocks {material}")));
        list.add(new MenuEntry(25, 80, Material.DIAMOND_ORE, "&fLevel {level}", Collections.singletonList("&7Unlocks {material}")));
        list.add(new MenuEntry(26, 85, Material.DIAMOND_ORE, "&fLevel {level}", Collections.singletonList("&7Unlocks {material}")));
        list.add(new MenuEntry(39, 90, Material.EMERALD_ORE, "&fLevel {level}", Collections.singletonList("&7Unlocks {material}")));
        list.add(new MenuEntry(40, 95, Material.EMERALD_ORE, "&fLevel {level}", Collections.singletonList("&7Unlocks {material}")));
        list.add(new MenuEntry(41, 100, Material.EMERALD_ORE, "&fLevel {level}", Collections.singletonList("&7Unlocks {material}")));
        return list;
    }

    private List<Integer> buildDefaultProgressSlots() {
        return new ArrayList<Integer>(List.of(9, 10, 11, 12, 13, 14, 15, 16, 17, 27, 28, 29, 30, 31, 32, 33, 34, 35, 48, 49, 50));
    }

    private static class Holder
    implements InventoryHolder {
        private final UUID owner;
        private Inventory inv;
        private BukkitTask task;

        Holder(UUID owner) {
            this.owner = owner;
        }

        public Inventory getInventory() {
            return this.inv;
        }
    }

    private record MenuEntry(int slot, int level, Material material, String name, List<String> lore) {
    }
}
