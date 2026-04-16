package org.axial.prisonsCore.gui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.service.EconomyService;
import org.axial.prisonsCore.service.PlayerLevelService;
import org.axial.prisonsCore.service.SeasonService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class LevelCapMenu implements Listener {
    private static final int[] DAY_SLOTS = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
    private static final List<Material> DISPLAY_ORES = List.of(Material.COAL_ORE, Material.IRON_ORE, Material.LAPIS_ORE, Material.GOLD_ORE, Material.REDSTONE_ORE, Material.DIAMOND_ORE, Material.EMERALD_ORE);
    private final DecimalFormat xpFormat = new DecimalFormat("#,###");
    private final PrisonsCore plugin;

    public LevelCapMenu(PrisonsCore plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        LevelCapHolder holder = new LevelCapHolder();
        Inventory inventory = Bukkit.createInventory(holder, 36, Text.color("&8Level Caps"));
        holder.setInventory(inventory);
        this.fillBackground(inventory);
        for (int day = 1; day <= DAY_SLOTS.length; ++day) {
            inventory.setItem(DAY_SLOTS[day - 1], this.createDayItem(day));
        }
        player.openInventory(inventory);
    }

    private ItemStack createDayItem(int day) {
        SeasonService seasonService = this.plugin.getSeasonService();
        PlayerLevelService playerLevelService = this.plugin.getPlayerLevelService();
        EconomyService economyService = this.plugin.getEconomyService();
        if (seasonService == null || playerLevelService == null || economyService == null) {
            ItemStack item = new ItemStack(Material.BARRIER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(Text.color("&cLevel cap data unavailable"));
                meta.setLore(List.of(Text.color("&7Try again after the plugin finishes loading.")));
                item.setItemMeta(meta);
            }
            return item;
        }
        SeasonService.DayDefinition definition = seasonService.getDayDefinition(day);
        SeasonService.DayStatus status = seasonService.getStatusForDay(day);
        ItemStack item = new ItemStack(this.materialFor(status));
        ItemMeta meta = item.getItemMeta();
        if (meta == null || definition == null) {
            return item;
        }
        meta.setDisplayName(Text.color(this.nameFor(day, status)));
        List<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add(Text.color(this.statusLine(status)));
        if (status == SeasonService.DayStatus.LOCKED) {
            lore.add(Text.color("&7" + seasonService.getTimeUntilUnlock(day)));
        }
        lore.add("");
        lore.add(Text.color("&cMax Level: &f" + definition.levelCap()));
        lore.add(Text.color("&7(&f" + this.xpFormat.format(playerLevelService.getTotalExpRequiredForLevel(definition.levelCap())) + "&7)"));
        lore.add("");
        lore.add(Text.color("&a&lUnlocks"));
        if (definition.unlocks().isEmpty()) {
            lore.add(Text.color("&7No new unlocks"));
        } else {
            definition.unlocks().forEach(entry -> lore.add(Text.color("&7- &f" + entry)));
        }
        lore.add("");
        lore.add(Text.color("&6&lOre Worth"));
        for (Material ore : DISPLAY_ORES) {
            double worth = economyService.getWorth(ore, day);
            lore.add(Text.color("&7" + this.oreName(ore) + ": &f" + economyService.format(worth)));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private String oreName(Material ore) {
        return switch (ore) {
            case COAL_ORE -> "Coal Ore";
            case IRON_ORE -> "Iron Ore";
            case LAPIS_ORE -> "Lapis Ore";
            case GOLD_ORE -> "Gold Ore";
            case REDSTONE_ORE -> "Redstone Ore";
            case DIAMOND_ORE -> "Diamond Ore";
            case EMERALD_ORE -> "Emerald Ore";
            default -> ore.name();
        };
    }

    private String nameFor(int day, SeasonService.DayStatus status) {
        return switch (status) {
            case UNLOCKED -> "&a&lDay &f&l" + day;
            case CURRENT -> "&e&lDay &f&l" + day + " &7(Current)";
            case LOCKED -> "&c&lDay &f&l" + day;
        };
    }

    private String statusLine(SeasonService.DayStatus status) {
        return switch (status) {
            case UNLOCKED -> "&a&lUNLOCKED";
            case CURRENT -> "&e&lLOCKED";
            case LOCKED -> "&c&lLOCKED";
        };
    }

    private Material materialFor(SeasonService.DayStatus status) {
        return switch (status) {
            case UNLOCKED -> Material.LIME_STAINED_GLASS_PANE;
            case CURRENT -> Material.YELLOW_STAINED_GLASS_PANE;
            case LOCKED -> Material.RED_STAINED_GLASS_PANE;
        };
    }

    private void fillBackground(Inventory inventory) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
        }
        for (int i = 0; i < inventory.getSize(); ++i) {
            inventory.setItem(i, filler);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof LevelCapHolder) {
            event.setCancelled(true);
        }
    }

    private static class LevelCapHolder implements InventoryHolder {
        private Inventory inventory;

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public @NotNull Inventory getInventory() {
            return this.inventory;
        }
    }
}
