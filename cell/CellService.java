/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.Sound
 *  org.bukkit.World
 *  org.bukkit.block.Block
 *  org.bukkit.block.BlockState
 *  org.bukkit.block.Sign
 *  org.bukkit.block.data.Bisected$Half
 *  org.bukkit.block.data.BlockData
 *  org.bukkit.block.data.type.Door
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.EquipmentSlot
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 *  org.bukkit.util.Vector
 */
package org.axial.prisonsCore.cell;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.axial.prisonsCore.Keys;
import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.cell.Cell;
import org.axial.prisonsCore.cell.CellDoorType;
import org.axial.prisonsCore.cell.CellRegion;
import org.axial.prisonsCore.cell.CellTier;
import org.axial.prisonsCore.cell.guard.CellGuardService;
import org.axial.prisonsCore.service.CellBusterManager;
import org.axial.prisonsCore.service.EconomyService;
import org.axial.prisonsCore.service.PlayerLevelService;
import org.axial.prisonsCore.service.TeleportService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class CellService {
    private static final long RENT_DURATION_MILLIS = 604800000L;
    private static final double DISCOUNT = 0.15;
    private final PrisonsCore plugin;
    private final EconomyService economyService;
    private final PlayerLevelService playerLevelService;
    private final TeleportService teleportService;
    private final CellBusterManager cellBusterManager;
    private CellGuardService cellGuardService;
    private final Map<String, Cell> cells = new HashMap<String, Cell>();
    private final Map<UUID, Selection> selections = new HashMap<UUID, Selection>();
    private final Map<String, DoorProgress> doorProgress = new HashMap<String, DoorProgress>();
    private final Map<String, Integer> storedDoorDamage = new HashMap<String, Integer>();
    private File definitionsFile;
    private File dataFile;
    private YamlConfiguration definitionsCfg;
    private YamlConfiguration dataCfg;
    private BukkitTask expiryTask;

    private boolean initialized = false;

    public CellService(PrisonsCore plugin, EconomyService economyService, PlayerLevelService playerLevelService, TeleportService teleportService, CellBusterManager cellBusterManager) {
        this.plugin = plugin;
        this.economyService = economyService;
        this.playerLevelService = playerLevelService;
        this.teleportService = teleportService;
        this.cellBusterManager = cellBusterManager;
        this.initFiles();
        this.loadDefinitions();
        this.loadData();
        this.initialized = true;
        this.startExpiryTask();
    }

    private void initFiles() {
        this.definitionsFile = new File(this.plugin.getDataFolder(), "cells.yml");
        if (!this.definitionsFile.exists()) {
            this.plugin.saveResource("cells.yml", false);
        }
        this.dataFile = new File(this.plugin.getDataFolder(), "cells-data.yml");
        if (!this.dataFile.exists()) {
            try {
                this.dataFile.getParentFile().mkdirs();
                this.dataFile.createNewFile();
                YamlConfiguration tmp = new YamlConfiguration();
                tmp.set("cells", new HashMap());
                tmp.save(this.dataFile);
            }
            catch (IOException e) {
                this.plugin.getLogger().warning("Failed creating cells-data.yml: " + e.getMessage());
            }
        }
        this.definitionsCfg = YamlConfiguration.loadConfiguration((File)this.definitionsFile);
        this.dataCfg = YamlConfiguration.loadConfiguration((File)this.dataFile);
    }

    private void loadDefinitions() {
        this.cells.clear();
        ConfigurationSection sec = this.definitionsCfg.getConfigurationSection("cells");
        if (sec == null) {
            return;
        }
        for (String id : sec.getKeys(false)) {
            CellTier tier;
            ConfigurationSection cSec = sec.getConfigurationSection(id);
            if (cSec == null) continue;
            String tierName = cSec.getString("tier");
            String world = cSec.getString("world");
            Vector min = this.readVector(cSec.getConfigurationSection("min"));
            Vector max = this.readVector(cSec.getConfigurationSection("max"));
            if (tierName == null || world == null || min == null || max == null) continue;
            try {
                tier = CellTier.valueOf(tierName.toUpperCase(Locale.ROOT));
            }
            catch (IllegalArgumentException e) {
                continue;
            }
            CellRegion region = new CellRegion(world, min, max);
            Cell cell = new Cell(id, tier, region);
            if (cSec.isConfigurationSection("door")) {
                World w;
                ConfigurationSection dSec = cSec.getConfigurationSection("door");
                String dworld = dSec.getString("world");
                Double x = dSec.getDouble("x");
                Double y = dSec.getDouble("y");
                Double z = dSec.getDouble("z");
                String type = dSec.getString("type");
                if (dworld != null && type != null && (w = Bukkit.getWorld((String)dworld)) != null) {
                    cell.setDoorLocation(new Location(w, x.doubleValue(), y.doubleValue(), z.doubleValue()));
                    try {
                        cell.setDoorType(CellDoorType.valueOf(type.toUpperCase(Locale.ROOT)));
                    }
                    catch (IllegalArgumentException illegalArgumentException) {
                        // empty catch block
                    }
                }
            }
            this.cells.put(id.toLowerCase(Locale.ROOT), cell);
        }
    }

    private Vector readVector(ConfigurationSection sec) {
        if (sec == null) {
            return null;
        }
        Double x = sec.getDouble("x");
        Double y = sec.getDouble("y");
        Double z = sec.getDouble("z");
        return new Vector(x.doubleValue(), y.doubleValue(), z.doubleValue());
    }

    private void loadData() {
        ConfigurationSection sec = this.dataCfg.getConfigurationSection("cells");
        if (sec == null) {
            return;
        }
        for (String id : sec.getKeys(false)) {
            float pitch;
            String sworld;
            Cell cell = this.cells.get(id.toLowerCase(Locale.ROOT));
            if (cell == null) continue;
            ConfigurationSection cSec = sec.getConfigurationSection(id);
            String owner = cSec.getString("owner");
            if (owner != null && !owner.isEmpty()) {
                try {
                    cell.setOwner(UUID.fromString(owner));
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    // empty catch block
                }
            }
            cell.setRentExpiry(cSec.getLong("rent-expiry", 0L));
            cell.getAccess().clear();
            List list = cSec.getStringList("access");
            for (Object s : list) {
                try {
                    cell.getAccess().add(UUID.fromString((String) s));
                }
                catch (IllegalArgumentException illegalArgumentException) {}
            }
            int dmg = cSec.getInt("door-damage", 0);
            if (dmg > 0) {
                this.storedDoorDamage.put(cell.getId(), dmg);
            }
            if (cSec.isConfigurationSection("sign")) {
                World sw;
                ConfigurationSection signSec = cSec.getConfigurationSection("sign");
                sworld = signSec.getString("world");
                double sx = signSec.getDouble("x");
                double sy = signSec.getDouble("y");
                double sz = signSec.getDouble("z");
                World world = sw = sworld == null ? null : Bukkit.getWorld((String)sworld);
                if (sw != null) {
                    Location loc = new Location(sw, sx, sy, sz);
                    cell.setSignLocation(loc);
                }
            }
            if (cSec.isConfigurationSection("spawn")) {
                World sw;
                ConfigurationSection spawnSec = cSec.getConfigurationSection("spawn");
                sworld = spawnSec.getString("world");
                World world = sw = sworld == null ? null : Bukkit.getWorld((String)sworld);
                if (sw != null) {
                    double sx = spawnSec.getDouble("x");
                    double sy = spawnSec.getDouble("y");
                    double sz = spawnSec.getDouble("z");
                    float yaw = (float)spawnSec.getDouble("yaw");
                    pitch = (float)spawnSec.getDouble("pitch");
                    cell.setSpawnLocation(new Location(sw, sx, sy, sz, yaw, pitch));
                }
            }
            if (cSec.isConfigurationSection("home")) {
                World hw;
                ConfigurationSection homeSec = cSec.getConfigurationSection("home");
                String hworld = homeSec.getString("world");
                World world = hw = hworld == null ? null : Bukkit.getWorld((String)hworld);
                if (hw != null) {
                    double hx = homeSec.getDouble("x");
                    double hy = homeSec.getDouble("y");
                    double hz = homeSec.getDouble("z");
                    float yaw = (float)homeSec.getDouble("yaw");
                    pitch = (float)homeSec.getDouble("pitch");
                    cell.setHomeLocation(new Location(hw, hx, hy, hz, yaw, pitch));
                }
            }
            this.updateCellSign(cell);
        }
    }

    public void saveDefinitions() {
        if (!this.initialized || this.definitionsCfg == null) {
            return;
        }
        this.definitionsCfg.set("cells", null);
        for (Cell cell : this.cells.values()) {
            String path = "cells." + cell.getId();
            this.definitionsCfg.set(path + ".tier", (Object)cell.getTier().name());
            this.definitionsCfg.set(path + ".world", (Object)cell.getRegion().getWorldName());
            this.writeVector(this.definitionsCfg, path + ".min", cell.getRegion().getMin());
            this.writeVector(this.definitionsCfg, path + ".max", cell.getRegion().getMax());
            if (cell.getDoorLocation() == null || cell.getDoorType() == null || cell.getDoorLocation().getWorld() == null) continue;
            Location l = cell.getDoorLocation();
            this.definitionsCfg.set(path + ".door.world", (Object)l.getWorld().getName());
            this.definitionsCfg.set(path + ".door.x", (Object)l.getBlockX());
            this.definitionsCfg.set(path + ".door.y", (Object)l.getBlockY());
            this.definitionsCfg.set(path + ".door.z", (Object)l.getBlockZ());
            this.definitionsCfg.set(path + ".door.type", (Object)cell.getDoorType().name());
        }
        try {
            this.definitionsCfg.save(this.definitionsFile);
        }
        catch (IOException e) {
            this.plugin.getLogger().warning("Failed to save cells.yml: " + e.getMessage());
        }
    }

    public void saveData() {
        if (!this.initialized || this.dataCfg == null) {
            return;
        }
        this.dataCfg.set("cells", null);
        for (Cell cell : this.cells.values()) {
            Location s;
            int dmg;
            String path = "cells." + cell.getId();
            if (cell.getOwner() != null) {
                this.dataCfg.set(path + ".owner", (Object)cell.getOwner().toString());
            }
            this.dataCfg.set(path + ".rent-expiry", (Object)cell.getRentExpiry());
            if (!cell.getAccess().isEmpty()) {
                ArrayList<String> access = new ArrayList<String>();
                for (UUID id : cell.getAccess()) {
                    access.add(id.toString());
                }
                this.dataCfg.set(path + ".access", access);
            }
            if ((dmg = this.storedDoorDamage.getOrDefault(cell.getId(), 0).intValue()) > 0) {
                this.dataCfg.set(path + ".door-damage", (Object)dmg);
            }
            if (cell.getSignLocation() != null && cell.getSignLocation().getWorld() != null) {
                s = cell.getSignLocation();
                this.dataCfg.set(path + ".sign.world", (Object)s.getWorld().getName());
                this.dataCfg.set(path + ".sign.x", (Object)s.getBlockX());
                this.dataCfg.set(path + ".sign.y", (Object)s.getBlockY());
                this.dataCfg.set(path + ".sign.z", (Object)s.getBlockZ());
            }
            if (cell.getSpawnLocation() != null && cell.getSpawnLocation().getWorld() != null) {
                s = cell.getSpawnLocation();
                this.dataCfg.set(path + ".spawn.world", (Object)s.getWorld().getName());
                this.dataCfg.set(path + ".spawn.x", (Object)s.getX());
                this.dataCfg.set(path + ".spawn.y", (Object)s.getY());
                this.dataCfg.set(path + ".spawn.z", (Object)s.getZ());
                this.dataCfg.set(path + ".spawn.yaw", (Object)Float.valueOf(s.getYaw()));
                this.dataCfg.set(path + ".spawn.pitch", (Object)Float.valueOf(s.getPitch()));
            }
            if (cell.getHomeLocation() != null && cell.getHomeLocation().getWorld() != null) {
                Location h = cell.getHomeLocation();
                this.dataCfg.set(path + ".home.world", (Object)h.getWorld().getName());
                this.dataCfg.set(path + ".home.x", (Object)h.getX());
                this.dataCfg.set(path + ".home.y", (Object)h.getY());
                this.dataCfg.set(path + ".home.z", (Object)h.getZ());
                this.dataCfg.set(path + ".home.yaw", (Object)Float.valueOf(h.getYaw()));
                this.dataCfg.set(path + ".home.pitch", (Object)Float.valueOf(h.getPitch()));
                continue;
            }
            this.dataCfg.set(path + ".home", null);
        }
        try {
            this.dataCfg.save(this.dataFile);
        }
        catch (IOException e) {
            this.plugin.getLogger().warning("Failed to save cells-data.yml: " + e.getMessage());
        }
    }

    private void writeVector(YamlConfiguration cfg, String path, Vector v) {
        cfg.set(path + ".x", (Object)v.getX());
        cfg.set(path + ".y", (Object)v.getY());
        cfg.set(path + ".z", (Object)v.getZ());
    }

    public Selection getSelection(UUID uuid) {
        return this.selections.computeIfAbsent(uuid, k -> new Selection());
    }

    public void setPos1(UUID uuid, Location loc) {
        this.getSelection(uuid).setPos1(loc);
    }

    public void setPos2(UUID uuid, Location loc) {
        this.getSelection(uuid).setPos2(loc);
    }

    public Cell createCell(String id, CellTier tier, Selection selection) {
        CellRegion region;
        if (selection == null || !selection.isComplete()) {
            return null;
        }
        try {
            region = new CellRegion(selection.getPos1(), selection.getPos2());
        }
        catch (IllegalArgumentException e) {
            return null;
        }
        Cell cell = new Cell(id, tier, region);
        this.cells.put(id.toLowerCase(Locale.ROOT), cell);
        this.saveDefinitions();
        return cell;
    }

    public boolean deleteCell(String id) {
        Cell removed = this.cells.remove(id.toLowerCase(Locale.ROOT));
        if (removed != null) {
            if (this.cellGuardService != null) {
                this.cellGuardService.removeAllForCell(removed.getId());
            }
            this.saveDefinitions();
            this.saveData();
            return true;
        }
        return false;
    }

    public boolean setDoor(String id, Location loc, CellDoorType type) {
        Cell cell = this.cells.get(id.toLowerCase(Locale.ROOT));
        if (cell == null || loc == null || type == null) {
            return false;
        }
        Location base = this.ensureBottomDoor(loc);
        cell.setDoorLocation(base);
        cell.setDoorType(type);
        Block block = base.getBlock();
        if (block.getType() != type.getMaterial()) {
            block.setType(type.getMaterial(), false);
        }
        this.saveDefinitions();
        return true;
    }

    private Location ensureBottomDoor(Location loc) {
        Door door;
        Block block = loc.getBlock();
        BlockData data = block.getBlockData();
        if (data instanceof Door && (door = (Door)data).getHalf() == Bisected.Half.TOP) {
            return block.getLocation().subtract(0.0, 1.0, 0.0);
        }
        return block.getLocation();
    }

    public Collection<Cell> getCells() {
        return Collections.unmodifiableCollection(this.cells.values());
    }

    public Cell getCell(String id) {
        return this.cells.get(id.toLowerCase(Locale.ROOT));
    }

    public Cell getCellAt(Location location) {
        for (Cell cell : this.cells.values()) {
            if (!cell.isInside(location)) continue;
            return cell;
        }
        return null;
    }

    public Cell getOwnedCell(UUID playerId) {
        if (playerId == null) {
            return null;
        }
        for (Cell cell : this.cells.values()) {
            if (!playerId.equals(cell.getOwner())) continue;
            return cell;
        }
        return null;
    }

    public boolean purchaseCell(Player player, Cell cell) {
        if (cell == null || player == null) {
            return false;
        }
        if (cell.isOwned()) {
            player.sendMessage(Text.color("&cThat cell is already owned."));
            return false;
        }
        if (this.getOwnedCell(player.getUniqueId()) != null) {
            player.sendMessage(Text.color("&cYou already own a cell."));
            return false;
        }
        int level = this.playerLevelService.getLevel(player);
        if (level < cell.getTier().getRequiredMiningLevel()) {
            player.sendMessage(Text.color("&cYou need mining level " + cell.getTier().getRequiredMiningLevel() + " to purchase this cell."));
            return false;
        }
        var unlock = this.plugin.getSeasonService() == null ? null : this.plugin.getSeasonService().getFeature(cell.getTier());
        if (this.plugin.getSeasonService() != null && !this.plugin.getSeasonService().isFeatureUnlocked(unlock)) {
            player.sendMessage(Text.color("&cThis cell tier unlocks on Day " + this.plugin.getSeasonService().getUnlockDay(unlock) + "."));
            return false;
        }
        double price = this.getPrice(cell.getTier(), player);
        if (!this.economyService.withdraw(player, price)) {
            player.sendMessage(Text.color("&cYou need " + this.economyService.format(price) + " to rent this cell for 7 days."));
            return false;
        }
        cell.setOwner(player.getUniqueId());
        cell.setHomeLocation(null);
        cell.setRentExpiry(System.currentTimeMillis() + 604800000L);
        this.saveData();
        this.updateCellSign(cell);
        player.sendMessage(Text.color("&aYou rented cell &f" + cell.getId() + " &afor 7 days. Expires: &f" + this.expiryString(cell)));
        return true;
    }

    public boolean renewCell(Player player, Cell cell) {
        if (cell == null || player == null || !player.getUniqueId().equals(cell.getOwner())) {
            return false;
        }
        var unlock = this.plugin.getSeasonService() == null ? null : this.plugin.getSeasonService().getFeature(cell.getTier());
        if (this.plugin.getSeasonService() != null && !this.plugin.getSeasonService().isFeatureUnlocked(unlock)) {
            player.sendMessage(Text.color("&cThis cell tier unlocks on Day " + this.plugin.getSeasonService().getUnlockDay(unlock) + "."));
            return false;
        }
        double price = this.getPrice(cell.getTier(), player);
        if (!this.economyService.withdraw(player, price)) {
            player.sendMessage(Text.color("&cYou need " + this.economyService.format(price) + " to renew this cell."));
            return false;
        }
        long base = Math.max(System.currentTimeMillis(), cell.getRentExpiry());
        cell.setRentExpiry(base + 604800000L);
        this.saveData();
        this.updateCellSign(cell);
        player.sendMessage(Text.color("&aCell rent renewed. New expiry: &f" + this.expiryString(cell)));
        return true;
    }

    public double getPrice(CellTier tier, Player player) {
        double price = tier.getWeeklyPrice();
        if (this.plugin.getSeasonService() != null) {
            price = this.plugin.getSeasonService().scalePriceForCurrentDay(price);
        }
        if (player != null && player.hasPermission("cells.discount")) {
            price *= 0.85;
        }
        return price;
    }

    public boolean toggleAccess(Player owner, OfflinePlayer target) {
        boolean added;
        if (owner == null || target == null) {
            return false;
        }
        Cell cell = this.getOwnedCell(owner.getUniqueId());
        if (cell == null) {
            return false;
        }
        UUID tId = target.getUniqueId();
        if (cell.getAccess().contains(tId)) {
            cell.getAccess().remove(tId);
            added = false;
        } else {
            cell.getAccess().add(tId);
            added = true;
        }
        this.saveData();
        return added;
    }

    public boolean transferOwnership(Cell cell, OfflinePlayer target) {
        if (cell == null || target == null || target.getUniqueId() == null) {
            return false;
        }
        if (this.getOwnedCell(target.getUniqueId()) != null) {
            return false;
        }
        cell.getAccess().clear();
        cell.setOwner(target.getUniqueId());
        cell.setHomeLocation(null);
        this.saveData();
        this.updateCellSign(cell);
        return true;
    }

    public boolean disbandCell(Cell cell) {
        if (cell == null) {
            return false;
        }
        if (this.cellGuardService != null) {
            this.cellGuardService.removeAllForCell(cell.getId());
        }
        this.clearCellBlocks(cell);
        cell.getAccess().clear();
        cell.setOwner(null);
        cell.setHomeLocation(null);
        cell.setRentExpiry(0L);
        this.storedDoorDamage.remove(cell.getId());
        this.saveData();
        this.updateCellSign(cell);
        return true;
    }

    private void clearCellBlocks(Cell cell) {
        if (cell == null || cell.getRegion() == null) {
            return;
        }
        World world = Bukkit.getWorld((String)cell.getRegion().getWorldName());
        if (world == null) {
            return;
        }
        Vector min = cell.getRegion().getMin();
        Vector max = cell.getRegion().getMax();
        int minX = (int)Math.floor(min.getX());
        int minY = (int)Math.floor(min.getY());
        int minZ = (int)Math.floor(min.getZ());
        int maxX = (int)Math.ceil(max.getX());
        int maxY = (int)Math.ceil(max.getY());
        int maxZ = (int)Math.ceil(max.getZ());
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
    }

    public void updateCellSign(Cell cell) {
        if (cell == null) {
            return;
        }
        Location loc = cell.getSignLocation();
        if (loc == null || loc.getWorld() == null) {
            return;
        }
        Block block = loc.getBlock();
        BlockState blockState = block.getState();
        if (!(blockState instanceof Sign)) {
            return;
        }
        Sign sign = (Sign)blockState;
        String cellName = Text.color("&0&l" + cell.getId());
        sign.setLine(0, cellName);
        if (cell.isOwned()) {
            sign.setLine(1, Text.color("&a&lOwned By"));
            sign.setLine(2, Text.color("&0" + cell.ownerName()));
            sign.setLine(3, "");
        } else {
            sign.setLine(1, Text.color("&c&lFor Rent"));
            sign.setLine(2, Text.color("&7/cells to rent"));
            sign.setLine(3, "");
        }
        sign.update(true);
    }

    public boolean setCellSpawn(Cell cell, Location location) {
        if (cell == null || location == null || location.getWorld() == null) {
            return false;
        }
        cell.setSpawnLocation(location.clone());
        this.saveData();
        return true;
    }

    public boolean setCellHome(Cell cell, Player owner) {
        if (cell == null || owner == null) {
            return false;
        }
        if (cell.getOwner() == null || !cell.getOwner().equals(owner.getUniqueId())) {
            return false;
        }
        cell.setHomeLocation(owner.getLocation().clone());
        this.saveData();
        return true;
    }

    public boolean teleportToCell(Player player, Cell cell) {
        if (player == null || cell == null) {
            return false;
        }
        Location target = null;
        if (cell.getHomeLocation() != null && cell.getOwner() != null && cell.getOwner().equals(player.getUniqueId())) {
            target = cell.getHomeLocation();
        } else if (cell.getSpawnLocation() != null) {
            target = cell.getSpawnLocation();
        } else if (cell.getDoorLocation() != null && cell.getDoorLocation().getWorld() != null) {
            target = cell.getDoorLocation().clone().add(0.5, 0.0, 0.5);
        } else {
            World world = Bukkit.getWorld((String)cell.getRegion().getWorldName());
            if (world != null) {
                Vector min = cell.getRegion().getMin();
                Vector max = cell.getRegion().getMax();
                double x = (min.getX() + max.getX()) / 2.0 + 0.5;
                double y = max.getY() + 1.0;
                double z = (min.getZ() + max.getZ()) / 2.0 + 0.5;
                target = new Location(world, x, y, z);
            }
        }
        if (target == null || target.getWorld() == null) {
            return false;
        }
        this.teleportService.scheduleTeleport(player, target, "cell " + cell.getId(), null);
        return true;
    }

    public boolean hasAccess(Cell cell, UUID playerId) {
        if (cell == null) {
            return false;
        }
        if (playerId == null) {
            return false;
        }
        if (playerId.equals(cell.getOwner())) {
            return true;
        }
        return cell.getAccess().contains(playerId);
    }

    private void startExpiryTask() {
        if (this.expiryTask != null) {
            this.expiryTask.cancel();
        }
        this.expiryTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, this::evictExpired, 1200L, 6000L);
    }

    private void evictExpired() {
        long now = System.currentTimeMillis();
        for (Cell cell : this.cells.values()) {
            Player online;
            if (!cell.isOwned() || cell.getRentExpiry() > now) continue;
            UUID owner = cell.getOwner();
            cell.setOwner(null);
            cell.getAccess().clear();
            cell.setHomeLocation(null);
            cell.setRentExpiry(0L);
            this.saveData();
            this.updateCellSign(cell);
            if (owner == null || (online = Bukkit.getPlayer((UUID)owner)) == null) continue;
            online.sendMessage(Text.color("&cYour rent for cell &f" + cell.getId() + " &chas expired."));
        }
    }

    public boolean isCellDoor(Block block) {
        if (block == null) {
            return false;
        }
        Location loc = block.getLocation();
        for (Cell cell : this.cells.values()) {
            Location base;
            if (!cell.hasDoorConfigured() || (base = cell.getDoorLocation()) == null || !this.sameBlock(base, loc) && !this.sameBlock(base.clone().add(0.0, 1.0, 0.0), loc)) continue;
            return true;
        }
        return false;
    }

    public Cell getCellByDoor(Block block) {
        if (block == null) {
            return null;
        }
        Location loc = block.getLocation();
        for (Cell cell : this.cells.values()) {
            Location base;
            if (!cell.hasDoorConfigured() || (base = cell.getDoorLocation()) == null || !this.sameBlock(base, loc) && !this.sameBlock(base.clone().add(0.0, 1.0, 0.0), loc)) continue;
            return cell;
        }
        return null;
    }

    private boolean sameBlock(Location a, Location b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.getWorld() == null || b.getWorld() == null) {
            return false;
        }
        return a.getWorld().getName().equals(b.getWorld().getName()) && a.getBlockX() == b.getBlockX() && a.getBlockY() == b.getBlockY() && a.getBlockZ() == b.getBlockZ();
    }

    public boolean isCellBuster(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return false;
        }
        Byte marker = (Byte)meta.getPersistentDataContainer().get(Keys.cellBuster(this.plugin), PersistentDataType.BYTE);
        return marker != null && marker == 1;
    }

    public ItemStack createCellBuster() {
        return this.cellBusterManager.createBuster();
    }

    private int getBusterEfficiency(Player player) {
        ItemStack hand = player.getInventory().getItem(EquipmentSlot.HAND);
        if (!this.isCellBuster(hand)) {
            hand = player.getInventory().getItem(EquipmentSlot.OFF_HAND);
        }
        if (!this.isCellBuster(hand)) {
            return 0;
        }
        Map<String, Integer> ench = this.cellBusterManager.getEnchants(hand);
        return ench.getOrDefault("buster_efficiency", 0);
    }

    public ItemStack createWand() {
        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(Text.color("&eCell Wand"));
        meta.setLore(List.of(Text.color("&7Left click: set Pos1"), Text.color("&7Right click: set Pos2")));
        meta.getPersistentDataContainer().set(Keys.cellWand(this.plugin), PersistentDataType.BYTE, (byte)1);
        wand.setItemMeta(meta);
        return wand;
    }

    public boolean isWand(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return false;
        }
        Byte flag = (Byte)meta.getPersistentDataContainer().get(Keys.cellWand(this.plugin), PersistentDataType.BYTE);
        return flag != null && flag == 1;
    }

    public ItemStack createDoorItem(CellDoorType type, int amount) {
        ItemStack door = new ItemStack(type.getMaterial());
        door.setAmount(Math.max(1, amount));
        ItemMeta meta = door.getItemMeta();
        String displayName = switch (type) {
            case BASIC -> "&f&lBasic Cell Door &7(Place)";
            case ELITE -> "&e&lElite Cell Door &7(Place)";
            case LEGENDARY -> "&6&lLegendary Cell Door &7(Place)";
        };
        meta.setDisplayName(Text.color(displayName));
        meta.setLore(List.of(Text.color("&7Place inside a cell to bind its door.")));
        meta.getPersistentDataContainer().set(Keys.cellDoorType(this.plugin), PersistentDataType.STRING, type.name());
        door.setItemMeta(meta);
        return door;
    }

    public boolean isDoorItem(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(Keys.cellDoorType(this.plugin), PersistentDataType.STRING);
    }

    public DoorBreakResult handleDoorHit(Player player, Cell cell, Block block) {
        if (cell == null || block == null || player == null) {
            return DoorBreakResult.IGNORED;
        }
        if (!this.isCellBuster(player.getInventory().getItem(EquipmentSlot.HAND)) && !this.isCellBuster(player.getInventory().getItem(EquipmentSlot.OFF_HAND))) {
            return DoorBreakResult.WRONG_TOOL;
        }
        if (!cell.hasDoorConfigured()) {
            return DoorBreakResult.NO_DOOR;
        }
        DoorProgress progress = this.doorProgress.computeIfAbsent(cell.getId(), k -> new DoorProgress());
        if (progress.hits == 0) {
            progress.hits = this.storedDoorDamage.getOrDefault(cell.getId(), 0);
        }
        long now = System.currentTimeMillis();
        if (!progress.started) {
            progress.started = true;
        }
        progress.lastHit = now;
        player.playSound(block.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 1.0f, 1.0f);
        player.sendTitle(Text.color("&c&lRaid Initiated"), Text.color("&7Continue to break this door to raid cell"), 5, 20, 5);
        int bonusLevel = this.getBusterEfficiency(player);
        int applied = Math.max(1, 1 + bonusLevel);
        progress.hits += applied;
        this.storedDoorDamage.put(cell.getId(), progress.hits);
        this.saveData();
        int remaining = cell.getDoorType().getHitsToBreak() - progress.hits;
        if (remaining > 0) {
            player.sendMessage(Text.color("&c(&7" + remaining + "&c)"));
        }
        if (progress.hits >= cell.getDoorType().getHitsToBreak()) {
            this.breakDoorBlocks(block);
            this.doorProgress.remove(cell.getId());
            this.storedDoorDamage.remove(cell.getId());
            player.sendMessage(Text.color("&c(&70&c)"));
            this.saveData();
            return DoorBreakResult.BROKEN;
        }
        return DoorBreakResult.DAMAGE_TICK;
    }

    private void breakDoorBlocks(Block block) {
        Door door;
        Block base = block;
        BlockData data = base.getBlockData();
        if (data instanceof Door && (door = (Door)data).getHalf() == Bisected.Half.TOP) {
            base = base.getRelative(0, -1, 0);
        }
        base.setType(Material.AIR);
        base.getRelative(0, 1, 0).setType(Material.AIR);
    }

    public String expiryString(Cell cell) {
        if (cell == null) {
            return "N/A";
        }
        long exp = cell.getRentExpiry();
        if (exp <= 0L) {
            return "Not rented";
        }
        Instant instant = Instant.ofEpochMilli(exp);
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime dt = LocalDateTime.ofInstant(instant, zone);
        return dt.toLocalDate().toString() + " " + String.valueOf(dt.toLocalTime().withNano(0));
    }

    public void setCellGuardService(CellGuardService cellGuardService) {
        this.cellGuardService = cellGuardService;
    }

    public static class Selection {
        private Location pos1;
        private Location pos2;

        public Location getPos1() {
            return this.pos1;
        }

        public Location getPos2() {
            return this.pos2;
        }

        public void setPos1(Location loc) {
            this.pos1 = loc;
        }

        public void setPos2(Location loc) {
            this.pos2 = loc;
        }

        public boolean isComplete() {
            return this.pos1 != null && this.pos2 != null;
        }
    }

    public static enum DoorBreakResult {
        IGNORED,
        WRONG_TOOL,
        NO_DOOR,
        DAMAGE_TICK,
        BROKEN;

    }

    private static class DoorProgress {
        int hits = 0;
        long lastHit = 0L;
        boolean started = false;

        private DoorProgress() {
        }
    }
}
