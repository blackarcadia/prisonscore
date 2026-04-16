/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Keyed
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Tag
 *  org.bukkit.World
 *  org.bukkit.attribute.Attribute
 *  org.bukkit.attribute.AttributeInstance
 *  org.bukkit.block.Block
 *  org.bukkit.block.data.Bisected$Half
 *  org.bukkit.block.data.BlockData
 *  org.bukkit.block.data.type.Door
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.ArmorStand
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.EntityEquipment
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.persistence.PersistentDataContainer
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 *  org.bukkit.util.Vector
 */
package org.axial.prisonsCore.cell.guard;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.axial.prisonsCore.Keys;
import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.cell.Cell;
import org.axial.prisonsCore.cell.CellService;
import org.axial.prisonsCore.cell.guard.CellGuardTier;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class CellGuardService {
    private static final String CELL_GUARD_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTczODE5Njg1NDM0NSwKICAicHJvZmlsZUlkIiA6ICJmMzNlZGMyNTRmNDk0NWY2YTg5ZjFjM2JhZmNkZjIwNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJGVV9CYWJ5IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Q4ZDc1YWJmYzAyYTZmNjU2YjUyNDI2ODA0YWFlNDk4ZTljM2NhOTRhOTdkN2RiYzM2OTczYTM2MGJkZjNlNzAiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==";
    private static final String CELL_GUARD_SIGNATURE = "Jr7s1UxNgwnnJqajNNJVq/r+Q4TaI0PY4k7ZCZbMtxFPfRV85wop6XoIvEftUxL7IdX+lLZrt5zNgn6BZ77EL1WYrUsAgEjt4ce/h8yvqxE50bsTLSmVZmPqM1u7r1rq7LAsYtKl+pQn3U+dHAfTAoHhJf1daEL9kfw/ctK4MOxSGSrkObqXWXC6XEvpBA8maOTE93lw6l1OYWFmqnX8T1quaI9v3f6iAiEQqV420wiXFrbDI2ctb3r2WF7yd6EkJEmpf7b7bpIYtYeybdZCRffWfAyYiQJ7F/DLvEg2qhZldYolNv7yKM+KbHN+zoJmV23SzI5jzn4bn/R4jQzPUW6S+JOib2oZR5pkGnW0zxgiUmUQWWaI4M06U9+dpVRbuphO2dd1AaoRQJgElHEbJuAznI5KzezS4yKrYPNufw4m8uPI80RlHCxOVsvHFriUGxWyof66U6+cP/POkNgBiWF7pI1cQhSZH6mv50rmd22yuovttLHVJgPOanMSM7YZhnbfiG5uUDCoxOcO+aZ2FeyqtZwd9kuux1NN/3YBk8Bb3sD9dYS24S53PtqhvQUJJj/sR3U8zq+fprqXM1aPyru7z4MfeGx4pGOm+V+3sjyuYWtvr1GcL4shTUbK4YzdNNY4X+McdPGzilHPHPk8z3Ym5h5+T90rhK0bPvKNHvo=";
    private final PrisonsCore plugin;
    private final CellService cellService;
    private final File dataFile;
    private YamlConfiguration dataCfg;
    private final Map<UUID, Placement> placements = new HashMap<UUID, Placement>();
    private final Map<String, Set<UUID>> placementsByCell = new HashMap<String, Set<UUID>>();
    private final Map<UUID, UUID> npcToPlacement = new HashMap<UUID, UUID>();
    private final Map<UUID, UUID> guardTargets = new HashMap<UUID, UUID>();
    private final Map<UUID, String> activeRaiders = new HashMap<UUID, String>();
    private final Map<UUID, UUID> healthStands = new HashMap<UUID, UUID>();
    private final Map<UUID, Long> guardEquipReadyAt = new HashMap<UUID, Long>();
    private final Map<UUID, Double> guardPendingHealth = new HashMap<UUID, Double>();
    private final Map<UUID, Long> guardHealthReadyAt = new HashMap<UUID, Long>();
    private final Map<UUID, Long> guardPhaseReadyAt = new HashMap<UUID, Long>();
    private BukkitTask monitorTask;
    private boolean skinWarningLogged = false;

    public CellGuardService(PrisonsCore plugin, CellService cellService) {
        this.plugin = plugin;
        this.cellService = cellService;
        this.dataFile = new File(plugin.getDataFolder(), "cell-guards.yml");
        this.initFile();
        this.load();
        this.startMonitor();
    }

    public PrisonsCore getPlugin() {
        return this.plugin;
    }

    public void shutdown() {
        if (this.monitorTask != null) {
            this.monitorTask.cancel();
        }
    }

    public Placement getPlacement(UUID placementId) {
        return this.placements.get(placementId);
    }

    public UUID getPlacementIdByNpc(UUID npcId) {
        return this.npcToPlacement.get(npcId);
    }

    public ItemStack createGuardAnchor(CellGuardTier tier, int amount) {
        ItemStack item = new ItemStack(Material.ARMOR_STAND, Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Text.color("&6Cell Guard Anchor &7(" + tier.name() + ")"));
        meta.setLore(List.of(Text.color("&7Place inside your cell to spawn"), Text.color("&7a " + tier.displayName()), Text.color("&7The placed block is the guard's home.")));
        meta.getPersistentDataContainer().set(Keys.cellGuardTier(this.plugin), PersistentDataType.STRING, tier.name());
        item.setItemMeta(meta);
        return item;
    }

    public boolean isGuardAnchor(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(Keys.cellGuardTier(this.plugin), PersistentDataType.STRING);
    }

    public CellGuardTier tierFromItem(ItemStack stack) {
        if (stack == null) {
            return null;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return null;
        }
        String name = (String)meta.getPersistentDataContainer().get(Keys.cellGuardTier(this.plugin), PersistentDataType.STRING);
        if (name == null) {
            return null;
        }
        try {
            return CellGuardTier.valueOf(name);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void removeAllForCell(String cellId) {
        if (cellId == null) {
            return;
        }
        String key = cellId.toLowerCase(Locale.ROOT);
        Set<UUID> ids = this.placementsByCell.remove(key);
        if (ids == null) {
            return;
        }
        for (UUID pid : ids) {
            this.removePlacementInternal(pid, null, false);
        }
        this.save();
    }

    public boolean placeGuard(Cell cell, CellGuardTier tier, Location blockLocation, Player owner) {
        World world;
        if (cell == null || tier == null || blockLocation == null || owner == null) {
            return false;
        }
        if (cell.getOwner() == null || !cell.getOwner().equals(owner.getUniqueId())) {
            return false;
        }
        Location home = blockLocation.getBlock().getLocation().add(0.5, 0.0, 0.5);
        home.setYaw(owner.getLocation().getYaw());
        home.setPitch(owner.getLocation().getPitch());
        Location entrance = cell.getSpawnLocation();
        if (entrance == null) {
            entrance = cell.getDoorLocation();
        }
        if (entrance == null && cell.getRegion() != null && (world = Bukkit.getWorld((String)cell.getRegion().getWorldName())) != null) {
            Vector min = cell.getRegion().getMin();
            Vector max = cell.getRegion().getMax();
            entrance = new Location(world, (min.getX() + max.getX()) / 2.0 + 0.5, max.getY() + 0.5, (min.getZ() + max.getZ()) / 2.0 + 0.5);
        }
        if (entrance == null) {
            owner.sendMessage(Text.color("&cCould not determine a cell entrance for pathing."));
            return false;
        }
        if (!this.hasClearPath(home, entrance)) {
            owner.sendMessage(Text.color("&cCell guard needs a clear path to the cell spawn point."));
            return false;
        }
        UUID id = UUID.randomUUID();
        Placement placement = new Placement(id, cell.getId(), tier, home);
        this.placements.put(id, placement);
        this.placementsByCell.computeIfAbsent(cell.getId().toLowerCase(Locale.ROOT), k -> new HashSet()).add(id);
        this.spawnGuardForPlacement(placement);
        this.save();
        owner.sendMessage(Text.color("&aSpawned a " + tier.name() + " cell guard."));
        return true;
    }

    public void handleDoorRaid(Cell cell, Player attacker) {
        if (cell == null || attacker == null) {
            return;
        }
        if (cell.getOwner() == null) {
            return;
        }
        if (this.cellService.hasAccess(cell, attacker.getUniqueId())) {
            return;
        }
        if (!this.cellService.isCellBuster(attacker.getInventory().getItemInMainHand()) && !this.cellService.isCellBuster(attacker.getInventory().getItemInOffHand())) {
            return;
        }
        String cellId = cell.getId().toLowerCase(Locale.ROOT);
        this.markRaider(attacker.getUniqueId(), cellId);
        this.triggerGuards(cellId, attacker, attacker.getLocation());
    }

    public void handleOccupantAttack(Player attacker, Player victim, Cell cell) {
        if (attacker == null || victim == null || cell == null) {
            return;
        }
        if (cell.getOwner() == null) {
            return;
        }
        if (!cell.isInside(victim.getLocation())) {
            return;
        }
        if (!victim.getUniqueId().equals(cell.getOwner()) && !cell.getAccess().contains(victim.getUniqueId())) {
            return;
        }
        if (this.cellService.hasAccess(cell, attacker.getUniqueId())) {
            return;
        }
        String cellId = cell.getId().toLowerCase(Locale.ROOT);
        this.markRaider(attacker.getUniqueId(), cellId);
        this.triggerGuards(cellId, attacker, victim.getLocation());
    }

    public void handlePlayerMove(Player player, Location to) {
        boolean farFromDoor;
        if (player == null || to == null) {
            return;
        }
        String cellId = this.activeRaiders.get(player.getUniqueId());
        if (cellId == null) {
            return;
        }
        Cell cell = this.cellService.getCell(cellId);
        Location door = cell != null ? cell.getDoorLocation() : null;
        boolean bl = farFromDoor = door == null || door.getWorld() == null || to.getWorld() == null || !door.getWorld().equals((Object)to.getWorld()) || door.distanceSquared(to) > 2500.0;
        if (cell == null || !cell.isInside(to) && farFromDoor) {
            this.forgive(player.getUniqueId(), cellId);
        }
    }

    public void handlePlayerQuit(Player player) {
        if (player == null) {
            return;
        }
        String cellId = this.activeRaiders.remove(player.getUniqueId());
        if (cellId != null) {
            this.sendHomeForPlayer(player.getUniqueId());
        }
    }

    public void handlePlayerDeath(Player player) {
        if (player == null) {
            return;
        }
        String cellId = this.activeRaiders.remove(player.getUniqueId());
        if (cellId != null) {
            this.sendHomeForPlayer(player.getUniqueId());
        }
    }

    public void handleGuardDeath(LivingEntity guard) {
        if (guard == null) {
            return;
        }
        UUID id = guard.getUniqueId();
        UUID placementId = this.npcToPlacement.remove(id);
        this.guardTargets.remove(id);
        this.guardEquipReadyAt.remove(id);
        this.guardPendingHealth.remove(id);
        this.guardHealthReadyAt.remove(id);
        if (placementId == null) {
            return;
        }
        Placement placement = this.placements.get(placementId);
        if (placement == null) {
            return;
        }
        placement.setNpcId(null);
        this.removeHealthStand(id);
    }

    public boolean isCellGuard(Entity entity) {
        if (entity == null) {
            return false;
        }
        if (this.npcToPlacement.containsKey(entity.getUniqueId())) {
            return true;
        }
        if (!(entity instanceof LivingEntity)) {
            return false;
        }
        LivingEntity living = (LivingEntity)entity;
        PersistentDataContainer pdc = living.getPersistentDataContainer();
        return pdc.has(Keys.cellGuardPlacement(this.plugin), PersistentDataType.STRING);
    }

    public boolean isTrackedRaider(UUID playerId) {
        return this.activeRaiders.containsKey(playerId);
    }

    private void triggerGuards(String cellId, Player target, Location trigger) {
        Set<UUID> ids = this.placementsByCell.get(cellId.toLowerCase(Locale.ROOT));
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (UUID pid : ids) {
            LivingEntity living;
            UUID guardId;
            Entity guard;
            Placement placement = this.placements.get(pid);
            if (placement == null || !((guard = (guardId = placement.npc()) == null ? null : Bukkit.getEntity((UUID)guardId)) instanceof LivingEntity) || (living = (LivingEntity)guard).isDead()) continue;
            this.commandAttack(living, target);
        }
    }

    public void commandAttack(LivingEntity guard, Player target) {
        if (guard == null || target == null) {
            return;
        }
        this.guardTargets.put(guard.getUniqueId(), target.getUniqueId());
        try {
            Class<?> citizensApi = Class.forName("net.citizensnpcs.api.CitizensAPI");
            Object registry = citizensApi.getMethod("getNPCRegistry", new Class[0]).invoke(null, new Object[0]);
            Object npc = registry.getClass().getMethod("getNPC", Entity.class).invoke(registry, guard);
            if (npc != null) {
                Object navigator = npc.getClass().getMethod("getNavigator", new Class[0]).invoke(npc, new Object[0]);
                Class<?> navigatorClass = Class.forName("net.citizensnpcs.api.ai.Navigator");
                navigatorClass.getMethod("setTarget", Entity.class, Boolean.TYPE).invoke(navigator, target, true);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void markRaider(UUID playerId, String cellId) {
        if (playerId == null || cellId == null) {
            return;
        }
        this.activeRaiders.put(playerId, cellId.toLowerCase(Locale.ROOT));
    }

    private void sendGuardHome(Placement placement, Entity guard) {
        Location home;
        block6: {
            if (placement == null || guard == null) {
                return;
            }
            this.guardTargets.remove(guard.getUniqueId());
            home = placement.home();
            try {
                Class<?> citizensApi = Class.forName("net.citizensnpcs.api.CitizensAPI");
                Object registry = citizensApi.getMethod("getNPCRegistry", new Class[0]).invoke(null, new Object[0]);
                Object npc = registry.getClass().getMethod("getNPC", Entity.class).invoke(registry, guard);
                if (npc == null) break block6;
                Object navigator = npc.getClass().getMethod("getNavigator", new Class[0]).invoke(npc, new Object[0]);
                Class<?> navigatorClass = Class.forName("net.citizensnpcs.api.ai.Navigator");
                try {
                    navigatorClass.getMethod("cancelNavigation", new Class[0]).invoke(navigator, new Object[0]);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                navigatorClass.getMethod("setTarget", Location.class, Boolean.TYPE).invoke(navigator, home, true);
                if (guard.getLocation().distanceSquared(home) <= 4.0) {
                    return;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        guard.teleport(home);
    }

    private void sendHomeForPlayer(UUID playerId) {
        Map<UUID, UUID> snapshot = new HashMap<UUID, UUID>(this.guardTargets);
        for (Map.Entry<UUID, UUID> entry : snapshot.entrySet()) {
            if (!entry.getValue().equals(playerId)) continue;
            UUID guardId = entry.getKey();
            UUID placementId = this.npcToPlacement.get(guardId);
            Placement placement = placementId == null ? null : this.placements.get(placementId);
            Entity guard = Bukkit.getEntity((UUID)guardId);
            this.sendGuardHome(placement, guard);
        }
        this.activeRaiders.remove(playerId);
    }

    private void forgive(UUID playerId, String cellId) {
        this.sendHomeForPlayer(playerId);
        this.activeRaiders.remove(playerId);
    }

    private void startMonitor() {
        if (this.monitorTask != null) {
            this.monitorTask.cancel();
        }
        this.monitorTask = this.plugin.getServer().getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            for (UUID guardId : new HashSet<UUID>(this.npcToPlacement.keySet())) {
                Player reacquire;
                LivingEntity living;
                Placement placement;
                Entity ent = Bukkit.getEntity((UUID)guardId);
                UUID placementId = this.npcToPlacement.get(guardId);
                Placement placement2 = placement = placementId == null ? null : this.placements.get(placementId);
                if (!(ent instanceof LivingEntity) || (living = (LivingEntity)ent).isDead()) {
                    if (placement != null) {
                        placement.setNpcId(null);
                    }
                    this.npcToPlacement.remove(guardId);
                    this.guardTargets.remove(guardId);
                    this.removeHealthStand(guardId);
                    this.guardEquipReadyAt.remove(guardId);
                    this.guardPendingHealth.remove(guardId);
                    this.guardHealthReadyAt.remove(guardId);
                    continue;
                }
                if (placement != null) {
                    this.applyHealthIfReady(guardId);
                    double max = living.getAttribute(Attribute.MAX_HEALTH) != null ? living.getAttribute(Attribute.MAX_HEALTH).getBaseValue() : living.getMaxHealth();
                    this.updateHealthStand(living, placement.tier(), living.getHealth(), max);
                    this.ensureUnprotected((Entity)living);
                    this.ensureEquipment(living, placement.tier());
                } else {
                    this.stopNavigation(ent);
                }
                UUID targetId = this.guardTargets.get(guardId);
                if (targetId == null && placement != null && (reacquire = this.findActiveRaiderForCell(placement.cellId(), living.getLocation(), 100.0)) != null) {
                    this.commandAttack(living, reacquire);
                    targetId = reacquire.getUniqueId();
                }
                if (targetId == null) {
                    this.stopNavigation((Entity)living);
                    continue;
                }
                Player target = Bukkit.getPlayer((UUID)targetId);
                if (target == null || target.isDead() || !target.isOnline()) {
                    this.stopNavigation((Entity)living);
                    this.sendGuardHome(placement, (Entity)living);
                    continue;
                }
                String cellId = this.activeRaiders.get(targetId);
                if (cellId == null || placement == null || !placement.cellId().equalsIgnoreCase(cellId)) {
                    this.stopNavigation((Entity)living);
                    this.sendGuardHome(placement, (Entity)living);
                    continue;
                }
                Cell cell = this.cellService.getCell(cellId);
                if (cell == null) {
                    this.stopNavigation((Entity)living);
                    this.forgive(targetId, cellId);
                    continue;
                }
                if (placement != null && cell != null) {
                    this.phaseGuardThroughDoor(living, target, placement, cell);
                }
                this.commandAttack(living, target);
                double maxChase = Math.max((double)(placement.tier().radius() * placement.tier().radius()) * 4.0, 2500.0);
                if (!(living.getLocation().distanceSquared(target.getLocation()) > maxChase)) continue;
                this.stopNavigation((Entity)living);
                this.sendGuardHome(placement, (Entity)living);
            }
        }, 10L, 1L);
    }

    private Player findActiveRaiderForCell(String cellId, Location from, double maxDistance) {
        if (cellId == null || from == null) {
            return null;
        }
        double best = maxDistance * maxDistance;
        Player bestPlayer = null;
        for (Map.Entry<UUID, String> entry : new HashMap<UUID, String>(this.activeRaiders).entrySet()) {
            double d;
            Player p;
            if (!cellId.equalsIgnoreCase(entry.getValue()) || (p = Bukkit.getPlayer((UUID)entry.getKey())) == null || !p.isOnline() || p.isDead() || !from.getWorld().equals((Object)p.getWorld()) || !((d = from.distanceSquared(p.getLocation())) < best)) continue;
            best = d;
            bestPlayer = p;
        }
        return bestPlayer;
    }

    private boolean isAtCellEntrance(Cell cell, Location loc) {
        if (cell == null || loc == null) {
            return false;
        }
        Location door = cell.getDoorLocation();
        if (door == null || door.getWorld() == null || loc.getWorld() == null) {
            return false;
        }
        if (!door.getWorld().equals((Object)loc.getWorld())) {
            return false;
        }
        return door.distanceSquared(loc) <= 25.0;
    }

    private void initFile() {
        if (this.dataFile.exists()) {
            this.dataCfg = YamlConfiguration.loadConfiguration((File)this.dataFile);
            return;
        }
        try {
            this.dataFile.getParentFile().mkdirs();
            this.dataFile.createNewFile();
            this.dataCfg = new YamlConfiguration();
            this.dataCfg.set("cells", new HashMap());
            this.dataCfg.save(this.dataFile);
        }
        catch (IOException e) {
            this.plugin.getLogger().warning("Failed to create cell-guards.yml: " + e.getMessage());
            this.dataCfg = new YamlConfiguration();
        }
    }

    private void load() {
        this.placements.clear();
        this.placementsByCell.clear();
        this.npcToPlacement.clear();
        this.guardTargets.clear();
        this.activeRaiders.clear();
        ConfigurationSection cellsSec = this.dataCfg.getConfigurationSection("cells");
        if (cellsSec == null) {
            return;
        }
        for (String cellId : cellsSec.getKeys(false)) {
            ConfigurationSection placementsSec = cellsSec.getConfigurationSection(cellId + ".placements");
            if (placementsSec == null) continue;
            for (String idStr : placementsSec.getKeys(false)) {
                UUID placementId;
                CellGuardTier tier;
                ConfigurationSection pSec = placementsSec.getConfigurationSection(idStr);
                if (pSec == null) continue;
                String tierName = pSec.getString("tier");
                try {
                    tier = CellGuardTier.valueOf(tierName);
                }
                catch (Exception e) {
                    continue;
                }
                String worldName = pSec.getString("world");
                World world = Bukkit.getWorld((String)worldName);
                if (world == null) continue;
                double x = pSec.getDouble("x");
                double y = pSec.getDouble("y");
                double z = pSec.getDouble("z");
                float yaw = (float)pSec.getDouble("yaw");
                float pitch = (float)pSec.getDouble("pitch");
                try {
                    placementId = UUID.fromString(idStr);
                }
                catch (IllegalArgumentException e) {
                    placementId = UUID.randomUUID();
                }
                Placement placement = new Placement(placementId, cellId, tier, new Location(world, x, y, z, yaw, pitch));
                this.placements.put(placementId, placement);
                this.placementsByCell.computeIfAbsent(cellId.toLowerCase(Locale.ROOT), k -> new HashSet()).add(placementId);
                this.spawnGuardForPlacement(placement);
            }
        }
    }

    private void save() {
        this.dataCfg.set("cells", null);
        for (Placement placement : this.placements.values()) {
            String base = "cells." + placement.cellId().toLowerCase(Locale.ROOT) + ".placements." + String.valueOf(placement.id());
            this.dataCfg.set(base + ".tier", (Object)placement.tier().name());
            Location h = placement.home();
            if (h.getWorld() != null) {
                this.dataCfg.set(base + ".world", (Object)h.getWorld().getName());
            }
            this.dataCfg.set(base + ".x", (Object)h.getX());
            this.dataCfg.set(base + ".y", (Object)h.getY());
            this.dataCfg.set(base + ".z", (Object)h.getZ());
            this.dataCfg.set(base + ".yaw", (Object)Float.valueOf(h.getYaw()));
            this.dataCfg.set(base + ".pitch", (Object)Float.valueOf(h.getPitch()));
        }
        try {
            this.dataCfg.save(this.dataFile);
        }
        catch (IOException e) {
            this.plugin.getLogger().warning("Failed to save cell-guards.yml: " + e.getMessage());
        }
    }

    private void spawnGuardForPlacement(Placement placement) {
        Entity npc;
        if (placement == null) {
            return;
        }
        if (placement.npc() != null) {
            Entity old = Bukkit.getEntity((UUID)placement.npc());
            this.destroyNpc(old);
            if (old != null) {
                old.remove();
            }
            this.npcToPlacement.remove(placement.npc());
            this.guardTargets.remove(placement.npc());
        }
        if (!((npc = this.createCitizensNPC(placement.home(), Text.color(placement.tier().displayName()))) instanceof LivingEntity)) {
            this.plugin.getLogger().warning("Failed to spawn cell guard for cell " + placement.cellId());
            return;
        }
        LivingEntity living = (LivingEntity)npc;
        this.applyFacing((Entity)living, placement.home());
        living.setRemoveWhenFarAway(false);
        this.ensureUnprotected((Entity)living);
        this.scheduleHealthApply(living, placement.tier());
        this.updateGuardName(living, placement.tier());
        living.getPersistentDataContainer().set(Keys.cellGuardPlacement(this.plugin), PersistentDataType.STRING, placement.id().toString());
        placement.setNpcId(npc.getUniqueId());
        this.npcToPlacement.put(npc.getUniqueId(), placement.id());
        this.scheduleSkinApplications(npc);
        this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> this.equipGuard(living, placement.tier()), 5L);
        this.guardEquipReadyAt.put(living.getUniqueId(), System.currentTimeMillis() + 250L);
    }

    private Entity createCitizensNPC(Location loc, String name) {
        try {
            Class<?> citizensApi = Class.forName("net.citizensnpcs.api.CitizensAPI");
            Object registry = citizensApi.getMethod("getNPCRegistry", new Class[0]).invoke(null, new Object[0]);
            Class<?> npcRegistryClass = Class.forName("net.citizensnpcs.api.npc.NPCRegistry");
            Class<?> npcClass = Class.forName("net.citizensnpcs.api.npc.NPC");
            Class<?> entityTypeClass = Class.forName("org.bukkit.entity.EntityType");
            Object playerType = entityTypeClass.getField("PLAYER").get(null);
            Method create = npcRegistryClass.getMethod("createNPC", entityTypeClass, String.class);
            Object npc = create.invoke(registry, playerType, Text.color(name));
            this.applyPersistentSkin(npc, npcClass);
            Method spawn = npcClass.getMethod("spawn", Location.class);
            spawn.invoke(npc, loc);
            try {
                npcClass.getMethod("setProtected", Boolean.TYPE).invoke(npc, false);
            }
            catch (Exception exception) {
                // empty catch block
            }
            Method getEntity = npcClass.getMethod("getEntity", new Class[0]);
            Entity bukkitEntity = (Entity)getEntity.invoke(npc, new Object[0]);
            this.applyFacing(bukkitEntity, loc);
            return bukkitEntity;
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Citizens not found or failed to create cell guard NPC: " + e.getMessage());
            return null;
        }
    }

    private void applyFacing(Entity entity, Location facing) {
        if (entity == null || facing == null) {
            return;
        }
        Location updated = entity.getLocation();
        updated.setYaw(facing.getYaw());
        updated.setPitch(facing.getPitch());
        entity.teleport(updated);
    }

    private void scheduleSkinApplications(Entity npc) {
        long[] delays;
        for (long delay : delays = new long[]{0L, 20L}) {
            this.applySkinAsync(npc, delay);
        }
    }

    private void applySkinAsync(Entity npc, long delay) {
        if (npc == null) {
            return;
        }
        this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            try {
                Class<?> citizensApi = Class.forName("net.citizensnpcs.api.CitizensAPI");
                Object registry = citizensApi.getMethod("getNPCRegistry", new Class[0]).invoke(null, new Object[0]);
                Object npcHandle = registry.getClass().getMethod("getNPC", Entity.class).invoke(registry, npc);
                if (npcHandle == null) {
                    return;
                }
                Class<?> npcClass = Class.forName("net.citizensnpcs.api.npc.NPC");
                this.applyPersistentSkin(npcHandle, npcClass);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }, delay);
    }

    private void ensureSkin(LivingEntity living) {
        if (living == null) {
            return;
        }
        try {
            Class<?> citizensApi = Class.forName("net.citizensnpcs.api.CitizensAPI");
            Object registry = citizensApi.getMethod("getNPCRegistry", new Class[0]).invoke(null, new Object[0]);
            Object npcHandle = registry.getClass().getMethod("getNPC", Entity.class).invoke(registry, living);
            if (npcHandle == null) {
                return;
            }
            Class<?> npcClass = Class.forName("net.citizensnpcs.api.npc.NPC");
            this.applyPersistentSkin(npcHandle, npcClass);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void applyPersistentSkin(Object npc, Class<?> npcClass) {
        block32: {
            if (npc == null || npcClass == null) {
                return;
            }
            try {
                Class<?> skinTraitClass = this.resolveSkinTraitClass();
                Method getOrAddTrait = npcClass.getMethod("getOrAddTrait", Class.class);
                Object trait = getOrAddTrait.invoke(npc, skinTraitClass);
                if (trait == null) {
                    return;
                }
                try {
                    skinTraitClass.getMethod("setFetchDefaultSkin", Boolean.TYPE).invoke(trait, false);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                try {
                    skinTraitClass.getMethod("setShouldUpdateSkins", Boolean.TYPE).invoke(trait, false);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                try {
                    skinTraitClass.getMethod("setUpdateSkins", Boolean.TYPE).invoke(trait, false);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                try {
                    skinTraitClass.getMethod("setUseCache", Boolean.TYPE).invoke(trait, false);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                try {
                    Class<?> metadataEnum = Class.forName("net.citizensnpcs.api.npc.NPC$Metadata");
                    Object dataStore = npcClass.getMethod("data", new Class[0]).invoke(npc, new Object[0]);
                    Method setPersistent = dataStore.getClass().getMethod("setPersistent", metadataEnum, Object.class);
                    try {
                        Object useLatest = Enum.valueOf((Class)metadataEnum, "PLAYER_SKIN_USE_LATEST");
                        setPersistent.invoke(dataStore, useLatest, false);
                    }
                    catch (Exception useLatest) {
                        // empty catch block
                    }
                    try {
                        Object skinUuid = Enum.valueOf((Class)metadataEnum, "PLAYER_SKIN_UUID_METADATA");
                        setPersistent.invoke(dataStore, skinUuid, "cellguardskin");
                    }
                    catch (Exception exception) {}
                }
                catch (Exception metadataEnum) {
                    // empty catch block
                }
                try {
                    skinTraitClass.getMethod("clearTexture", new Class[0]).invoke(trait, new Object[0]);
                }
                catch (Exception metadataEnum) {
                    // empty catch block
                }
                boolean applied = false;
                try {
                    skinTraitClass.getMethod("setSkinPersistent", String.class, String.class, String.class, Boolean.TYPE).invoke(trait, UUID.randomUUID().toString(), CELL_GUARD_SIGNATURE, CELL_GUARD_TEXTURE, true);
                    applied = true;
                }
                catch (NoSuchMethodException dataStore) {
                }
                catch (Exception e) {
                    this.logSkinError("4-arg setSkinPersistent", e);
                }
                try {
                    skinTraitClass.getMethod("setSkinPersistent", String.class, String.class, String.class).invoke(trait, UUID.randomUUID().toString(), CELL_GUARD_SIGNATURE, CELL_GUARD_TEXTURE);
                    applied = true;
                }
                catch (NoSuchMethodException e) {
                }
                catch (Exception e) {
                    this.logSkinError("3-arg setSkinPersistent", e);
                }
                if (!applied) {
                    try {
                        skinTraitClass.getMethod("setSkinPersistent", String.class, String.class).invoke(trait, CELL_GUARD_SIGNATURE, CELL_GUARD_TEXTURE);
                        applied = true;
                    }
                    catch (Exception e) {
                        this.logSkinError("2-arg setSkinPersistent", e);
                    }
                }
                if (!applied) {
                    if (!this.skinWarningLogged) {
                        this.plugin.getLogger().warning("Failed to apply cell guard skin: setSkinPersistent not available.");
                        this.skinWarningLogged = true;
                    }
                    return;
                }
                this.invokeIfPresent(skinTraitClass, trait, "updateSkin");
                this.invokeIfPresent(skinTraitClass, trait, "applySkin");
                this.invokeIfPresent(skinTraitClass, trait, "updateAndApply");
            }
            catch (Exception e) {
                if (this.skinWarningLogged) break block32;
                String msg = e == null ? "unknown" : e.getClass().getName() + (String)(e.getMessage() == null ? "" : ": " + e.getMessage());
                this.plugin.getLogger().warning("Failed to apply cell guard skin: " + msg);
                if (e != null) {
                    e.printStackTrace();
                }
                this.skinWarningLogged = true;
            }
        }
    }

    private Class<?> resolveSkinTraitClass() throws ClassNotFoundException {
        String[] candidates = new String[]{"net.citizensnpcs.api.trait.SkinTrait", "net.citizensnpcs.api.trait.trait.SkinTrait", "net.citizensnpcs.trait.SkinTrait", "net.citizensnpcs.trait.trait.SkinTrait"};
        ClassNotFoundException last = null;
        for (String name : candidates) {
            try {
                return Class.forName(name);
            }
            catch (ClassNotFoundException e) {
                last = e;
            }
        }
        throw last != null ? last : new ClassNotFoundException("SkinTrait not found");
    }

    private void logSkinError(String step, Exception e) {
        if (!this.skinWarningLogged) {
            String msg = e == null ? "unknown" : e.getClass().getName() + (String)(e.getMessage() == null ? "" : ": " + e.getMessage());
            this.plugin.getLogger().warning("Cell guard skin step failed (" + step + "): " + msg);
            if (e != null) {
                e.printStackTrace();
            }
            this.skinWarningLogged = true;
        }
    }

    private void invokeIfPresent(Class<?> clazz, Object target, String method) {
        try {
            clazz.getMethod(method, new Class[0]).invoke(target, new Object[0]);
        }
        catch (NoSuchMethodException noSuchMethodException) {
        }
        catch (Exception e) {
            this.logSkinError(method, e);
        }
    }

    private void stopNavigation(Entity ent) {
        if (ent == null) {
            return;
        }
        try {
            Class<?> citizensApi = Class.forName("net.citizensnpcs.api.CitizensAPI");
            Object registry = citizensApi.getMethod("getNPCRegistry", new Class[0]).invoke(null, new Object[0]);
            Object npc = registry.getClass().getMethod("getNPC", Entity.class).invoke(registry, ent);
            if (npc == null) {
                return;
            }
            Object navigator = npc.getClass().getMethod("getNavigator", new Class[0]).invoke(npc, new Object[0]);
            Class<?> navigatorClass = Class.forName("net.citizensnpcs.api.ai.Navigator");
            try {
                navigatorClass.getMethod("cancelNavigation", new Class[0]).invoke(navigator, new Object[0]);
            }
            catch (Exception exception) {}
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void ensureUnprotected(Entity ent) {
        try {
            Class<?> citizensApi = Class.forName("net.citizensnpcs.api.CitizensAPI");
            Object registry = citizensApi.getMethod("getNPCRegistry", new Class[0]).invoke(null, new Object[0]);
            Object npc = registry.getClass().getMethod("getNPC", Entity.class).invoke(registry, ent);
            if (npc == null) {
                return;
            }
            Class<?> npcClass = Class.forName("net.citizensnpcs.api.npc.NPC");
            try {
                npcClass.getMethod("setProtected", Boolean.TYPE).invoke(npc, false);
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                Class<?> metadataEnum = Class.forName("net.citizensnpcs.api.npc.NPC$Metadata");
                Object dataStore = npcClass.getMethod("data", new Class[0]).invoke(npc, new Object[0]);
                Object protectedMeta = Enum.valueOf((Class)metadataEnum, "DEFAULT_PROTECTED");
                dataStore.getClass().getMethod("setPersistent", metadataEnum, Object.class).invoke(dataStore, protectedMeta, false);
            }
            catch (Exception exception) {}
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void equipGuard(LivingEntity living, CellGuardTier tier) {
        EntityEquipment equip = living.getEquipment();
        if (equip == null) {
            return;
        }
        equip.setHelmet(tier.helmet(), true);
        equip.setChestplate(tier.chest(), true);
        equip.setLeggings(tier.legs(), true);
        equip.setBoots(tier.boots(), true);
        equip.setItemInMainHand(tier.weapon(), true);
        this.setDropChancesSafe(equip);
        this.setUnbreakable(equip);
        this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            EntityEquipment eq = living.getEquipment();
            if (eq == null) {
                return;
            }
            eq.setHelmet(tier.helmet(), true);
            eq.setChestplate(tier.chest(), true);
            eq.setLeggings(tier.legs(), true);
            eq.setBoots(tier.boots(), true);
            eq.setItemInMainHand(tier.weapon(), true);
            this.setDropChancesSafe(eq);
            this.setUnbreakable(eq);
        }, 20L);
    }

    private void ensureEquipment(LivingEntity living, CellGuardTier tier) {
        EntityEquipment equip;
        if (living == null || tier == null) {
            return;
        }
        Long readyAt = this.guardEquipReadyAt.get(living.getUniqueId());
        if (readyAt != null && System.currentTimeMillis() < readyAt) {
            return;
        }
        if (readyAt != null && System.currentTimeMillis() >= readyAt) {
            this.guardEquipReadyAt.remove(living.getUniqueId());
        }
        if ((equip = living.getEquipment()) == null) {
            return;
        }
        boolean missing = false;
        if (this.isEmpty(equip.getHelmet())) {
            equip.setHelmet(tier.helmet(), true);
            missing = true;
        }
        if (this.isEmpty(equip.getChestplate())) {
            equip.setChestplate(tier.chest(), true);
            missing = true;
        }
        if (this.isEmpty(equip.getLeggings())) {
            equip.setLeggings(tier.legs(), true);
            missing = true;
        }
        if (this.isEmpty(equip.getBoots())) {
            equip.setBoots(tier.boots(), true);
            missing = true;
        }
        if (this.isEmpty(equip.getItemInMainHand())) {
            equip.setItemInMainHand(tier.weapon(), true);
            missing = true;
        }
        if (!missing) {
            return;
        }
        this.setDropChancesSafe(equip);
        this.setUnbreakable(equip);
        this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            EntityEquipment eq = living.getEquipment();
            if (eq == null) {
                return;
            }
            if (this.isEmpty(eq.getHelmet())) {
                eq.setHelmet(tier.helmet(), true);
            }
            if (this.isEmpty(eq.getChestplate())) {
                eq.setChestplate(tier.chest(), true);
            }
            if (this.isEmpty(eq.getLeggings())) {
                eq.setLeggings(tier.legs(), true);
            }
            if (this.isEmpty(eq.getBoots())) {
                eq.setBoots(tier.boots(), true);
            }
            if (this.isEmpty(eq.getItemInMainHand())) {
                eq.setItemInMainHand(tier.weapon(), true);
            }
            this.setDropChancesSafe(eq);
            this.setUnbreakable(eq);
        }, 10L);
    }

    private void setDropChancesSafe(EntityEquipment equip) {
        try {
            equip.setHelmetDropChance(0.0f);
        }
        catch (UnsupportedOperationException unsupportedOperationException) {
            // empty catch block
        }
        try {
            equip.setChestplateDropChance(0.0f);
        }
        catch (UnsupportedOperationException unsupportedOperationException) {
            // empty catch block
        }
        try {
            equip.setLeggingsDropChance(0.0f);
        }
        catch (UnsupportedOperationException unsupportedOperationException) {
            // empty catch block
        }
        try {
            equip.setBootsDropChance(0.0f);
        }
        catch (UnsupportedOperationException unsupportedOperationException) {
            // empty catch block
        }
        try {
            equip.setItemInMainHandDropChance(0.0f);
        }
        catch (UnsupportedOperationException unsupportedOperationException) {
            // empty catch block
        }
    }

    private void setUnbreakable(EntityEquipment equip) {
        this.tryUnbreakable(equip.getHelmet());
        this.tryUnbreakable(equip.getChestplate());
        this.tryUnbreakable(equip.getLeggings());
        this.tryUnbreakable(equip.getBoots());
        this.tryUnbreakable(equip.getItemInMainHand());
    }

    private void tryUnbreakable(ItemStack item) {
        if (item == null) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        meta.setUnbreakable(true);
        item.setItemMeta(meta);
    }

    private boolean isEmpty(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    private void phaseGuardThroughDoor(LivingEntity guard, Player target, Placement placement, Cell cell) {
        Location dest;
        if (guard == null || target == null || placement == null || cell == null) {
            return;
        }
        if (!cell.hasDoorConfigured()) {
            return;
        }
        Location doorLoc = cell.getDoorLocation();
        if (doorLoc == null || doorLoc.getWorld() == null) {
            return;
        }
        if (!doorLoc.getWorld().equals((Object)guard.getWorld())) {
            return;
        }
        double guardToTarget = guard.getLocation().distanceSquared(target.getLocation());
        if (guardToTarget < 4.0) {
            return;
        }
        long now = System.currentTimeMillis();
        Long readyAt = this.guardPhaseReadyAt.get(guard.getUniqueId());
        if (readyAt != null && now < readyAt) {
            return;
        }
        if (!this.doorBetween(guard, target, doorLoc)) {
            return;
        }
        Location snapDest = this.doorSnapDestination(guard.getLocation(), doorLoc, target.getLocation());
        Location location = dest = snapDest != null ? snapDest : this.findPhaseDestination(target, target.getLocation().toVector().subtract(guard.getLocation().toVector()));
        if (dest == null) {
            return;
        }
        guard.teleport(dest);
        this.guardPhaseReadyAt.put(guard.getUniqueId(), now + 1000L);
        this.commandAttack(guard, target);
    }

    private boolean isDoorBlockingPath(LivingEntity guard, Player target, Cell cell) {
        return this.doorBetween(guard, target, cell != null ? cell.getDoorLocation() : null);
    }

    private boolean doorBetween(LivingEntity guard, Player target, Location doorLoc) {
        if (guard == null || target == null || doorLoc == null) {
            return false;
        }
        if (doorLoc.getWorld() == null || !doorLoc.getWorld().equals((Object)guard.getWorld()) || !doorLoc.getWorld().equals((Object)target.getWorld())) {
            return false;
        }
        Vector dir = target.getLocation().toVector().subtract(guard.getLocation().toVector());
        double dist = dir.length();
        if (dist < 0.5) {
            return false;
        }
        dir.normalize();
        Vector step = dir.clone().multiply(0.25);
        Location pos = guard.getLocation().clone().add(0.0, 1.0, 0.0);
        for (double walked = 0.0; walked <= dist; walked += 0.25) {
            Block b = pos.getBlock();
            if (Tag.DOORS.isTagged((Material) b.getType())) {
                return true;
            }
            Block feet = pos.clone().add(0.0, -1.0, 0.0).getBlock();
            if (Tag.DOORS.isTagged((Material) feet.getType())) {
                return true;
            }
            pos.add(step);
        }
        return false;
    }

    private boolean sameBlock(Block a, Block b) {
        if (a == null || b == null) {
            return false;
        }
        return a.getWorld().equals((Object)b.getWorld()) && a.getX() == b.getX() && a.getY() == b.getY() && a.getZ() == b.getZ();
    }

    private Location doorSnapDestination(Location guardLoc, Location doorLoc, Location targetLoc) {
        try {
            BlockData data = doorLoc.getBlock().getBlockData();
            if (!(data instanceof Door)) {
                return null;
            }
            Door door = (Door)data;
            Block base = doorLoc.getBlock();
            if (door.getHalf() == Bisected.Half.TOP) {
                base = base.getRelative(0, -1, 0);
            }
            Vector doorCenter = base.getLocation().add(0.5, 1.0, 0.5).toVector();
            Vector face = new Vector(door.getFacing().getModX(), 0, door.getFacing().getModZ());
            if (face.lengthSquared() == 0.0) {
                return null;
            }
            face.normalize();
            Vector guardVec = guardLoc.toVector();
            double side = guardVec.subtract(doorCenter).dot(face);
            double targetSide = targetLoc.toVector().subtract(doorCenter).dot(face);
            if (side * targetSide > 0.0) {
                return null;
            }
            Vector destVec = doorCenter.clone().add(face.clone().multiply(targetSide >= 0.0 ? 1.5 : -1.5));
            Location dest = new Location(doorLoc.getWorld(), destVec.getX(), doorCenter.getY(), destVec.getZ(), targetLoc.getYaw(), targetLoc.getPitch());
            return dest;
        }
        catch (Exception ignored) {
            return null;
        }
    }

    private Location findPhaseDestination(Player target, Vector fromDirection) {
        Vector dir;
        if (target == null || target.getWorld() == null) {
            return null;
        }
        Location base = target.getLocation().clone();
        Vector vector = dir = fromDirection == null ? new Vector(0, 0, 0) : fromDirection.clone();
        if (dir.lengthSquared() < 1.0E-4) {
            dir = target.getLocation().getDirection();
        }
        if (dir.lengthSquared() < 1.0E-4) {
            dir = new Vector(1, 0, 0);
        }
        dir.setY(0);
        if (dir.lengthSquared() > 0.0) {
            dir.normalize();
        }
        List<Vector> offsets = List.of(dir.clone().multiply(-1.2), dir.clone().multiply(-0.6), new Vector(-dir.getZ(), 0.0, dir.getX()).multiply(1.0), new Vector(dir.getZ(), 0.0, -dir.getX()).multiply(1.0), new Vector(0, 0, 0));
        for (Vector offset : offsets) {
            Location spot = base.clone().add(offset);
            if (!this.isSafeTeleportSpot(spot)) continue;
            spot.setYaw(target.getLocation().getYaw());
            spot.setPitch(target.getLocation().getPitch());
            return spot;
        }
        Location fallback = base.clone();
        fallback.setYaw(target.getLocation().getYaw());
        fallback.setPitch(target.getLocation().getPitch());
        return fallback;
    }

    private boolean isSafeTeleportSpot(Location spot) {
        if (spot == null || spot.getWorld() == null) {
            return false;
        }
        Block feet = spot.getBlock();
        Block head = feet.getRelative(0, 1, 0);
        return this.isPassable(feet) && this.isPassable(head);
    }

    private boolean isPassable(Block block) {
        if (block == null) {
            return false;
        }
        if (Tag.DOORS.isTagged((Material) block.getType())) {
            return true;
        }
        try {
            return block.isPassable();
        }
        catch (NoSuchMethodError ignored) {
            return !block.getType().isSolid();
        }
    }

    private void scheduleHealthApply(LivingEntity living, CellGuardTier tier) {
        if (living == null || tier == null) {
            return;
        }
        double hp = tier.health();
        try {
            AttributeInstance maxAttr = living.getAttribute(Attribute.MAX_HEALTH);
            if (maxAttr != null) {
                maxAttr.setBaseValue(hp);
            }
            living.setHealth(Math.min(hp, living.getMaxHealth()));
        }
        catch (Exception maxAttr) {
            // empty catch block
        }
        long delayTicks = 20L;
        this.guardPendingHealth.put(living.getUniqueId(), hp);
        this.guardHealthReadyAt.put(living.getUniqueId(), System.currentTimeMillis() + delayTicks * 50L);
        this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> this.applyHealthIfReady(living.getUniqueId()), delayTicks);
    }

    private void applyHealthIfReady(UUID guardId) {
        Entity ent = Bukkit.getEntity((UUID)guardId);
        if (!(ent instanceof LivingEntity)) {
            return;
        }
        LivingEntity living = (LivingEntity)ent;
        Long readyAt = this.guardHealthReadyAt.get(guardId);
        Double target = this.guardPendingHealth.get(guardId);
        if (readyAt == null || target == null) {
            return;
        }
        if (System.currentTimeMillis() < readyAt) {
            return;
        }
        try {
            AttributeInstance maxAttr = living.getAttribute(Attribute.MAX_HEALTH);
            if (maxAttr != null) {
                maxAttr.setBaseValue(target.doubleValue());
            }
            living.setHealth(Math.min(target, living.getMaxHealth()));
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.guardPendingHealth.remove(guardId);
        this.guardHealthReadyAt.remove(guardId);
    }

    private boolean hasClearPath(Location from, Location to) {
        if (from == null || to == null) {
            return false;
        }
        if (from.getWorld() == null || to.getWorld() == null) {
            return false;
        }
        if (!from.getWorld().getName().equals(to.getWorld().getName())) {
            return false;
        }
        Vector dir = to.toVector().subtract(from.toVector());
        double length = dir.length();
        if (length == 0.0) {
            return true;
        }
        dir.normalize();
        double step = 0.5;
        Location head = from.clone().add(0.0, 1.0, 0.0);
        Vector stepVec = dir.clone().multiply(step);
        for (double walked = 0.0; walked <= length; walked += step) {
            if (this.isBlocking(head.getBlock())) {
                return false;
            }
            if (this.isBlocking(head.clone().add(0.0, -1.0, 0.0).getBlock())) {
                return false;
            }
            head.add(stepVec);
        }
        return true;
    }

    private boolean isBlocking(Block block) {
        if (block == null) {
            return false;
        }
        if (Tag.DOORS.isTagged((Material) block.getType())) {
            return false;
        }
        try {
            return !block.isPassable();
        }
        catch (NoSuchMethodError ignored) {
            return block.getType().isSolid();
        }
    }

    private void dropGuard(Placement placement) {
        if (placement.npc() != null) {
            Entity npc = Bukkit.getEntity((UUID)placement.npc());
            this.destroyNpc(npc);
            if (npc != null) {
                npc.remove();
            }
            this.npcToPlacement.remove(placement.npc());
            this.guardTargets.remove(placement.npc());
            this.removeHealthStand(placement.npc());
            this.guardEquipReadyAt.remove(placement.npc());
            this.guardPendingHealth.remove(placement.npc());
            this.guardHealthReadyAt.remove(placement.npc());
        }
    }

    private void destroyNpc(Entity entity) {
        if (entity == null) {
            return;
        }
        try {
            Class<?> citizensApi = Class.forName("net.citizensnpcs.api.CitizensAPI");
            Object registry = citizensApi.getMethod("getNPCRegistry", new Class[0]).invoke(null, new Object[0]);
            Object npc = registry.getClass().getMethod("getNPC", Entity.class).invoke(registry, entity);
            if (npc != null) {
                npc.getClass().getMethod("destroy", new Class[0]).invoke(npc, new Object[0]);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void spawnHealthStand(LivingEntity guard, CellGuardTier tier) {
        this.removeHealthStand(guard.getUniqueId());
        ArmorStand stand = (ArmorStand)guard.getWorld().spawn(guard.getLocation().add(0.0, 1.9, 0.0), ArmorStand.class, as -> {
            as.setVisible(false);
            as.setMarker(true);
            as.setGravity(false);
            as.setSmall(true);
            as.setCustomNameVisible(true);
        });
        this.healthStands.put(guard.getUniqueId(), stand.getUniqueId());
        double max = guard.getAttribute(Attribute.MAX_HEALTH) != null ? guard.getAttribute(Attribute.MAX_HEALTH).getBaseValue() : guard.getHealth();
        double current = guard.getHealth();
        stand.setCustomName(Text.color("&f" + (int)Math.ceil(current) + " &c\u2764"));
        guard.addPassenger((Entity)stand);
        stand.teleport(guard.getLocation().add(0.0, 1.9, 0.0));
    }

    private void updateHealthStand(LivingEntity guard, CellGuardTier tier, double current, double max) {
        Entity ent;
        UUID standId = this.healthStands.get(guard.getUniqueId());
        ArmorStand stand = null;
        if (standId != null && (ent = Bukkit.getEntity((UUID)standId)) instanceof ArmorStand) {
            ArmorStand as;
            stand = as = (ArmorStand)ent;
        }
        if (stand == null || stand.isDead()) {
            Entity ent2;
            this.spawnHealthStand(guard, tier);
            UUID newStandId = this.healthStands.get(guard.getUniqueId());
            Entity entity = ent2 = newStandId != null ? Bukkit.getEntity((UUID)newStandId) : null;
            if (ent2 instanceof ArmorStand) {
                ArmorStand as;
                stand = as = (ArmorStand)ent2;
            } else {
                return;
            }
        }
        if (!guard.getPassengers().contains(stand)) {
            guard.addPassenger((Entity)stand);
        }
        stand.setCustomName(Text.color("&f" + (int)Math.ceil(current) + " &c\u2764"));
        stand.teleport(guard.getLocation().add(0.0, 1.9, 0.0));
    }

    private void removeHealthStand(UUID guardId) {
        UUID standId = this.healthStands.remove(guardId);
        if (standId == null) {
            return;
        }
        Entity ent = Bukkit.getEntity((UUID)standId);
        if (ent != null) {
            ent.remove();
        }
    }

    public void updateGuardName(LivingEntity guard) {
        if (guard == null) {
            return;
        }
        UUID pid = this.npcToPlacement.get(guard.getUniqueId());
        if (pid == null) {
            return;
        }
        Placement placement = this.placements.get(pid);
        if (placement == null) {
            return;
        }
        this.updateGuardName(guard, placement.tier());
    }

    private void updateGuardName(LivingEntity guard, CellGuardTier tier) {
        if (guard == null || tier == null) {
            return;
        }
        double max = guard.getAttribute(Attribute.MAX_HEALTH) != null ? guard.getAttribute(Attribute.MAX_HEALTH).getBaseValue() : guard.getMaxHealth();
        double current = guard.getHealth();
        String base = tier.displayName();
        guard.setCustomName(Text.color(base));
        guard.setCustomNameVisible(true);
        this.updateHealthStand(guard, tier, current, max);
    }

    public boolean removePlacement(UUID placementId, Player receiver, boolean giveBack) {
        boolean removed = this.removePlacementInternal(placementId, receiver, giveBack);
        if (removed) {
            this.save();
        }
        return removed;
    }

    private boolean removePlacementInternal(UUID placementId, Player receiver, boolean giveBack) {
        Placement placement = this.placements.remove(placementId);
        if (placement == null) {
            return false;
        }
        this.dropGuard(placement);
        this.placementsByCell.computeIfPresent(placement.cellId().toLowerCase(Locale.ROOT), (k, v) -> {
            v.remove(placementId);
            return v.isEmpty() ? null : v;
        });
        this.npcToPlacement.values().removeIf(id -> id.equals(placementId));
        this.guardTargets.keySet().removeIf(id -> id.equals(placement.npc()));
        if (giveBack && receiver != null) {
            Map<Integer, ItemStack> leftovers = receiver.getInventory().addItem(new ItemStack[]{this.createGuardAnchor(placement.tier(), 1)});
            leftovers.values().forEach(item -> receiver.getWorld().dropItemNaturally(receiver.getLocation(), item));
        }
        return true;
    }

    public static class Placement {
        private final UUID id;
        private final String cellId;
        private final CellGuardTier tier;
        private final Location home;
        private UUID npcId;

        Placement(UUID id, String cellId, CellGuardTier tier, Location home) {
            this.id = id;
            this.cellId = cellId;
            this.tier = tier;
            this.home = home;
        }

        public UUID id() {
            return this.id;
        }

        public String cellId() {
            return this.cellId;
        }

        public CellGuardTier tier() {
            return this.tier;
        }

        public Location home() {
            return this.home;
        }

        public UUID npc() {
            return this.npcId;
        }

        public void setNpcId(UUID npcId) {
            this.npcId = npcId;
        }
    }
}
