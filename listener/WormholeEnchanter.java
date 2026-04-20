/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Particle
 *  org.bukkit.Sound
 *  org.bukkit.World
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.entity.ArmorStand
 *  org.bukkit.entity.Display$Billboard
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.ItemDisplay
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.player.PlayerDropItemEvent
 *  org.bukkit.event.player.PlayerInteractAtEntityEvent
 *  org.bukkit.event.player.PlayerKickEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.event.player.PlayerTeleportEvent
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 *  org.bukkit.util.Transformation
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package org.axial.prisonsCore.listener;

import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.model.CustomEnchant;
import org.axial.prisonsCore.model.EnchantRarity;
import org.axial.prisonsCore.service.CellBusterManager;
import org.axial.prisonsCore.service.CustomEnchantService;
import org.axial.prisonsCore.service.EnchantDustService;
import org.axial.prisonsCore.service.GearEnchantService;
import org.axial.prisonsCore.service.GearManager;
import org.axial.prisonsCore.service.PickaxeManager;
import org.axial.prisonsCore.service.SatchelManager;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class WormholeEnchanter
implements Listener {
    private final PrisonsCore plugin;
    private final PickaxeManager pickaxeManager;
    private final CustomEnchantService enchantService;
    private final GearManager gearManager;
    private final GearEnchantService gearEnchantService;
    private final SatchelManager satchelManager;
    private final CellBusterManager cellBusterManager;
    private final EnchantDustService dustService;
    private final File configFile;
    private boolean enabled;
    private Location center;
    private double radiusSquared;
    private double displayHeight;
    private double optionRadius;
    private int optionCount;
    private long holdTicks;
    private int spinSpeedTicks = 2;
    private String msgDropIn;
    private String msgNotFull;
    private String msgBusy;
    private String msgInvalid;
    private String msgPickPrompt;
    private String msgSatchelUpgraded;
    private final Map<UUID, Session> sessions = new HashMap<UUID, Session>();
    private final Map<UUID, Option> optionByEntity = new HashMap<UUID, Option>();
    private final Random rng = new Random();

    public WormholeEnchanter(PrisonsCore plugin, PickaxeManager pickaxeManager, CustomEnchantService enchantService, GearManager gearManager, GearEnchantService gearEnchantService, SatchelManager satchelManager, CellBusterManager cellBusterManager, EnchantDustService dustService) {
        this.plugin = plugin;
        this.pickaxeManager = pickaxeManager;
        this.enchantService = enchantService;
        this.gearManager = gearManager;
        this.gearEnchantService = gearEnchantService;
        this.satchelManager = satchelManager;
        this.cellBusterManager = cellBusterManager;
        this.dustService = dustService;
        this.configFile = new File(plugin.getDataFolder(), "wormhole.yml");
        this.reload();
    }

    public void reload() {
        YamlConfiguration cfg = this.loadWormholeConfig();
        if (cfg == null) {
            this.enabled = true;
            return;
        }
        if (!cfg.getBoolean("enabled", true)) {
            cfg.set("enabled", true);
            this.saveWormholeConfig(cfg);
        }
        this.enabled = true;
        String worldName = cfg.getString("world", "world");
        World world = Bukkit.getWorld((String)worldName);
        if (world == null) {
            this.plugin.getLogger().warning("[Wormhole] World '" + worldName + "' not found; wormhole will stay enabled but inactive until the world is available.");
            return;
        }
        double x = cfg.getDouble("center.x", 0.5);
        double y = cfg.getDouble("center.y", 65.0);
        double z = cfg.getDouble("center.z", 0.5);
        this.center = new Location(world, x, y, z);
        double radius = cfg.getDouble("radius", 2.5);
        this.radiusSquared = radius * radius;
        this.displayHeight = cfg.getDouble("display-height", 2.3);
        this.optionRadius = cfg.getDouble("option-radius", 1.25);
        this.optionCount = Math.max(1, Math.min(5, cfg.getInt("option-count", 5)));
        this.holdTicks = Math.max(0L, cfg.getLong("hold-ticks", 60L));
        this.spinSpeedTicks = Math.max(1, cfg.getInt("spin-speed-ticks", 2));
        ConfigurationSection messages = cfg.getConfigurationSection("messages");
        this.msgDropIn = messages != null ? messages.getString("drop-in", "&dDrop your item into the Cosmic Wormhole") : "&dDrop your item into the Cosmic Wormhole";
        this.msgNotFull = messages != null ? messages.getString("not-full", "&cItem must have full energy to enchant.") : "&cItem must have full energy to enchant.";
        this.msgBusy = messages != null ? messages.getString("busy", "&cYou already have an item inside the wormhole.") : "&cYou already have an item inside the wormhole.";
        this.msgInvalid = messages != null ? messages.getString("invalid", "&cThat item cannot be enchanted here.") : "&cThat item cannot be enchanted here.";
        this.msgPickPrompt = messages != null ? messages.getString("pick-prompt", "&eClick an option hologram to apply the enchant.") : "&eClick an option hologram to apply the enchant.";
        this.msgSatchelUpgraded = messages != null ? messages.getString("satchel-upgraded", "&aYour satchel absorbed the energy and upgraded!") : "&aYour satchel absorbed the energy and upgraded!";
    }

    public void setEnabled(boolean enabled) {
        YamlConfiguration cfg = this.loadWormholeConfig();
        if (cfg == null) {
            return;
        }
        cfg.set("enabled", true);
        this.saveWormholeConfig(cfg);
        this.reload();
    }

    public void setCenter(Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        YamlConfiguration cfg = this.loadWormholeConfig();
        if (cfg == null) {
            return;
        }
        cfg.set("world", location.getWorld().getName());
        cfg.set("center.x", location.getX());
        cfg.set("center.y", location.getY());
        cfg.set("center.z", location.getZ());
        this.saveWormholeConfig(cfg);
        this.reload();
    }

    public void setRadius(double radius) {
        YamlConfiguration cfg = this.loadWormholeConfig();
        if (cfg == null) {
            return;
        }
        cfg.set("radius", radius);
        this.saveWormholeConfig(cfg);
        this.reload();
    }

    public void setOptionCount(int count) {
        YamlConfiguration cfg = this.loadWormholeConfig();
        if (cfg == null) {
            return;
        }
        cfg.set("option-count", count);
        this.saveWormholeConfig(cfg);
        this.reload();
    }

    public void setDisplayHeight(double height) {
        YamlConfiguration cfg = this.loadWormholeConfig();
        if (cfg == null) {
            return;
        }
        cfg.set("display-height", height);
        this.saveWormholeConfig(cfg);
        this.reload();
    }

    public String describe() {
        if (!this.enabled || this.center == null) {
            return "&cWormhole: disabled";
        }
        return "&dWormhole: &aenabled &7world=&f" + this.center.getWorld().getName() + " &7center=&f" + this.round(this.center.getX()) + "," + this.round(this.center.getY()) + "," + this.round(this.center.getZ()) + " &7radius=&f" + this.round(Math.sqrt(this.radiusSquared)) + " &7options=&f" + this.optionCount + " &7height=&f" + this.round(this.displayHeight);
    }

    private YamlConfiguration loadWormholeConfig() {
        if (!this.plugin.getDataFolder().exists() && !this.plugin.getDataFolder().mkdirs()) {
            this.plugin.getLogger().warning("[Wormhole] Could not create plugin data folder.");
            return null;
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(this.configFile);
        ConfigurationSection defaults = this.plugin.getConfig().getConfigurationSection("wormhole");
        boolean changed = !this.configFile.exists();
        if (defaults != null) {
            changed |= this.copyMissing(defaults, cfg, "");
        }
        if (changed) {
            this.saveWormholeConfig(cfg);
        }
        return cfg;
    }

    private boolean copyMissing(ConfigurationSection source, YamlConfiguration target, String pathPrefix) {
        boolean changed = false;
        for (String key : source.getKeys(false)) {
            String path = pathPrefix.isEmpty() ? key : pathPrefix + "." + key;
            Object value = source.get(key);
            if (value instanceof ConfigurationSection) {
                changed |= this.copyMissing(source.getConfigurationSection(key), target, path);
                continue;
            }
            if (target.contains(path)) {
                continue;
            }
            target.set(path, value);
            changed = true;
        }
        return changed;
    }

    private void saveWormholeConfig(YamlConfiguration cfg) {
        try {
            cfg.save(this.configFile);
        }
        catch (IOException exception) {
            this.plugin.getLogger().warning("[Wormhole] Failed to save wormhole.yml: " + exception.getMessage());
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onDrop(PlayerDropItemEvent event) {
        if (!this.enabled || this.center == null) {
            return;
        }
        if (!this.isInZone(event.getItemDrop().getLocation())) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack stack = event.getItemDrop().getItemStack();
        Session existing = this.sessions.get(player.getUniqueId());
        if (existing != null) {
            if (this.dustService != null && this.dustService.isDust(stack)) {
                event.setCancelled(true);
                if (this.canAcceptDust(existing, stack)) {
                    event.getItemDrop().remove();
                    this.applyDust(player, existing, stack);
                } else {
                    player.sendMessage(Text.color("&cNo matching options to apply that dust right now."));
                }
                return;
            }
            player.sendMessage(Text.color(this.msgBusy));
            event.setCancelled(true);
            event.getItemDrop().remove();
            return;
        }
        if (!this.isEnchantable(stack)) {
            player.sendMessage(Text.color(this.msgInvalid));
            event.setCancelled(true);
            event.getItemDrop().remove();
            return;
        }
        if (!this.hasFullEnergy(stack)) {
            player.sendMessage(Text.color(this.msgNotFull));
            event.setCancelled(true);
            event.getItemDrop().remove();
            return;
        }
        Location origin = event.getItemDrop().getLocation().clone();
        event.getItemDrop().remove();
        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.8f, 1.2f);
        this.startSession(player, stack, origin);
    }

    @EventHandler(ignoreCancelled=true)
    public void onInteract(PlayerInteractAtEntityEvent event) {
        if (!this.enabled) {
            return;
        }
        Entity entity = event.getRightClicked();
        if (!(entity instanceof ArmorStand)) {
            return;
        }
        ArmorStand stand = (ArmorStand)entity;
        Option option = this.optionByEntity.get(stand.getUniqueId());
        if (option == null) {
            return;
        }
        Player player = event.getPlayer();
        Session session = this.sessions.get(player.getUniqueId());
        if (session == null || session.resolved) {
            return;
        }
        if (!option.playerId.equals(player.getUniqueId())) {
            return;
        }
        event.setCancelled(true);
        this.resolveOption(player, session, option);
    }

    @EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=false)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!this.enabled) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof ArmorStand)) {
            return;
        }
        ArmorStand stand = (ArmorStand)entity;
        Option option = this.optionByEntity.get(stand.getUniqueId());
        if (option == null) {
            return;
        }
        Entity entity2 = event.getDamager();
        if (!(entity2 instanceof Player)) {
            return;
        }
        Player player = (Player)entity2;
        Session session = this.sessions.get(player.getUniqueId());
        if (session == null || session.resolved) {
            return;
        }
        if (!option.playerId.equals(player.getUniqueId())) {
            return;
        }
        event.setCancelled(true);
        this.resolveOption(player, session, option);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.endSession(event.getPlayer().getUniqueId(), true);
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        this.endSession(event.getPlayer().getUniqueId(), true);
    }

    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (this.sessions.containsKey(player.getUniqueId())) {
            player.sendMessage(Text.color("&cYou cannot teleport while your item is in the wormhole."));
            event.setCancelled(true);
        }
    }

    private void startSession(Player player, ItemStack stack, Location origin) {
        boolean isCellBuster = this.cellBusterManager.isBuster(stack);
        boolean isPickaxe = !isCellBuster && this.pickaxeManager.isPrisonPickaxe(stack);
        boolean isGear = !isPickaxe && !isCellBuster && this.gearManager.isGear(stack);
        boolean isSatchel = this.satchelManager.isSatchel(stack);
        if (!(isPickaxe || isGear || isCellBuster || isSatchel)) {
            player.sendMessage(Text.color(this.msgInvalid));
            return;
        }
        if (isSatchel) {
            this.handleSatchel(player, stack);
            return;
        }
        boolean brokenGear = isGear && this.gearManager.isBroken(stack);
        Session session = new Session(player.getUniqueId(), stack, isPickaxe, isGear, isCellBuster, brokenGear);
        this.sessions.put(player.getUniqueId(), session);
        Location displayLoc = this.center.clone().add(0.0, this.displayHeight, 0.0);
        session.display = this.spawnDisplay(stack, origin, player);
        this.spawnNameHolograms(stack, session, origin, player);
        this.animateIntoCenter(session, origin, displayLoc, () -> {
            this.startSpin(session);
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.revealOptions(session), this.holdTicks);
        });
    }

    private List<Option> createOptions(Session session) {
        Map<String, Integer> current = new HashMap<>();
        ItemStack item = session.item;
        boolean isCellBuster = session.isCellBuster;
        boolean isPickaxe = session.isPickaxe;
        boolean isGear = session.isGear;
        current = isCellBuster ? this.cellBusterManager.getEnchants(item) : (isPickaxe ? this.pickaxeManager.getEnchants(item) : this.gearManager.getEnchants(item));
        List<CustomEnchant> pool = isCellBuster ? this.enchantService.getAll().stream().filter(ce -> ce.getId().equalsIgnoreCase("buster_efficiency")).collect(Collectors.toList()) : (isPickaxe ? this.enchantService.getAll().stream().filter(ce -> !ce.getId().equalsIgnoreCase("buster_efficiency")).collect(Collectors.toList()) : this.gearEnchantService.getAll().stream().filter(ce -> ce.isApplicableTo(item.getType())).collect(Collectors.toList()));
        Map<String, Integer> finalCurrent = current;
        List<CustomEnchant> candidates = pool.stream().filter(ce -> finalCurrent.getOrDefault(ce.getId(), 0) < ce.getMaxLevel()).collect(Collectors.toList());
        List<CustomEnchant> chosen = isCellBuster || isPickaxe ? this.enchantService.pickRandomOptions(Math.min(this.optionCount, candidates.size()), candidates) : this.gearEnchantService.pickRandomOptions(Math.min(this.optionCount, candidates.size()), candidates);
        if (chosen.isEmpty() && !candidates.isEmpty()) {
            chosen = candidates.subList(0, Math.min(this.optionCount, candidates.size()));
        }
        ArrayList<Option> result = new ArrayList<Option>();
        double radius = this.optionRadius + (double)chosen.size() * 1.4;
        int index = 0;
        for (CustomEnchant enchant : chosen) {
            int nextLevel = Math.min(enchant.getMaxLevel(), current.getOrDefault(enchant.getId(), 0) + 1);
            int chance = this.generateChance(enchant.getRarity());
            Location optionLoc = this.center.clone().add(Math.cos((double)index * (Math.PI * 2 / (double)chosen.size())) * radius, this.displayHeight - 2.0, Math.sin((double)index * (Math.PI * 2 / (double)chosen.size())) * radius);
            Option opt = this.spawnOption(enchant, nextLevel, chance, optionLoc, session.playerId);
            result.add(opt);
            ++index;
        }
        return result;
    }

    private void resolveOption(Player player, Session session, Option option) {
        if (session.resolved) {
            return;
        }
        session.resolved = true;
        for (Option opt : session.options) {
            if (opt.armorStand() == null) continue;
            this.optionByEntity.remove(opt.armorStand().getUniqueId());
        }
        for (Option opt : new ArrayList<Option>(session.options)) {
            if (opt == option) continue;
            this.optionByEntity.remove(opt.armorStand().getUniqueId());
            if (opt.armorStand() != null && !opt.armorStand().isDead()) {
                opt.armorStand().remove();
            }
            if (opt.labelStand() != null && !opt.labelStand().isDead()) {
                opt.labelStand().remove();
            }
            if (opt.iconDisplay() == null || opt.iconDisplay().isDead()) continue;
            opt.iconDisplay().remove();
        }
        Location centerLoc = this.center.clone().add(0.0, this.displayHeight, 0.0);
        this.animateSelectedToCenter(option, centerLoc, () -> {
            Player p;
            if (option.armorStand() != null && !option.armorStand().isDead()) {
                option.armorStand().remove();
            }
            if (option.labelStand() != null && !option.labelStand().isDead()) {
                option.labelStand().remove();
            }
            if (option.iconDisplay() != null && !option.iconDisplay().isDead()) {
                option.iconDisplay().remove();
            }
            if ((p = Bukkit.getPlayer((UUID)session.playerId)) != null) {
                p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
            }
            this.startOrbitAnimation(session, option, centerLoc);
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.finalizeEnchant(player, session, option), 100L);
        });
    }

    private void endSession(UUID playerId, boolean returnItem) {
        Session session = this.sessions.remove(playerId);
        if (session == null) {
            return;
        }
        this.stopSpin(session);
        this.stopOrbit(session);
        this.clearOptions(session);
        if (session.display != null && !session.display.isDead()) {
            session.display.remove();
        }
        session.nameStands.forEach(st -> {
            if (!st.isDead()) {
                st.remove();
            }
        });
        session.nameStands.clear();
        if (session.resultStand != null && !session.resultStand.isDead()) {
            session.resultStand.remove();
        }
        session.resultStand = null;
        if (returnItem) {
            Player player = Bukkit.getPlayer((UUID)playerId);
            if (player != null && player.isOnline()) {
                this.returnItem(player, session.item);
            } else {
                this.center.getWorld().dropItem(this.center, session.item);
            }
        }
    }

    private void clearOptions(Session session) {
        for (Option opt : session.options) {
            this.optionByEntity.remove(opt.armorStand().getUniqueId());
            if (opt.armorStand() != null && !opt.armorStand().isDead()) {
                opt.armorStand().remove();
            }
            if (opt.labelStand() != null && !opt.labelStand().isDead()) {
                opt.labelStand().remove();
            }
            if (opt.iconDisplay() == null || opt.iconDisplay().isDead()) continue;
            opt.iconDisplay().remove();
        }
        session.options.clear();
    }

    private void revealOptions(Session session) {
        if (session.resolved || session.display == null || session.display.isDead()) {
            return;
        }
        if (session.brokenGear) {
            Player player = Bukkit.getPlayer((UUID)session.playerId);
            if (player != null) {
                this.repairBrokenGear(player, session);
            } else {
                this.endSession(session.playerId, true);
            }
            return;
        }
        List<Option> options = this.createOptions(session);
        if (options.isEmpty()) {
            Player player = Bukkit.getPlayer((UUID)session.playerId);
            if (player != null) {
                player.sendMessage(Text.color("&cNo enchants available for this item."));
            }
            this.endSession(session.playerId, true);
            return;
        }
        session.options.addAll(options);
        session.options.forEach(opt -> this.optionByEntity.put(opt.armorStand().getUniqueId(), (Option)opt));
        this.applyPendingDust(session);
        for (int i = 0; i < options.size(); ++i) {
            Option opt2 = options.get(i);
            Location target = opt2.armorStand().getLocation().clone();
            opt2.armorStand().teleport(this.center.clone().add(0.0, this.displayHeight, 0.0));
            if (opt2.labelStand() != null) {
                opt2.labelStand().teleport(this.center.clone().add(0.0, this.displayHeight + 1.2, 0.0));
            }
            int delay = i * 6;
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.animateOptionTo(opt2, target), (long)delay);
        }
        Player player = Bukkit.getPlayer((UUID)session.playerId);
        if (player != null) {
            player.sendMessage(Text.color(this.msgPickPrompt));
        }
    }

    private ItemDisplay spawnDisplay(ItemStack stack, Location loc, Player owner) {
        ItemDisplay disp = (ItemDisplay)loc.getWorld().spawn(loc, ItemDisplay.class, d -> {
            d.setItemStack(stack.clone());
            Transformation t = new Transformation(new Vector3f(0.0f, 0.0f, 0.0f), new Quaternionf(), new Vector3f(1.125f, 1.125f, 1.125f), new Quaternionf());
            d.setTransformation(t);
            d.setBillboard(Display.Billboard.CENTER);
            d.setSilent(true);
        });
        this.hideFromOthers(owner, (Entity)disp);
        return disp;
    }

    private void spawnNameHolograms(ItemStack stack, Session session, Location base, Player owner) {
        List<String> lines = this.buildNameLines(stack, session);
        session.nameStands.forEach(st -> {
            if (!st.isDead()) {
                st.remove();
            }
        });
        session.nameStands.clear();
        for (int i = 0; i < lines.size(); ++i) {
            double yOffset = this.nameYOffset(i, lines.size());
            String line = lines.get(i);
            ArmorStand stand = (ArmorStand)base.getWorld().spawn(base.clone().add(0.0, yOffset, 0.0), ArmorStand.class, as -> {
                as.setInvisible(true);
                as.setMarker(true);
                as.setGravity(false);
                as.setSmall(true);
                as.setCustomName(Text.color(line));
                as.setCustomNameVisible(true);
                as.setSilent(true);
            });
            this.hideFromOthers(owner, (Entity)stand);
            session.nameStands.add(stand);
        }
    }

    private Option spawnOption(CustomEnchant enchant, int level, int chance, Location loc, UUID playerId) {
        String text = Text.color(String.format("%s%s &7(&a%d%%&7 / &c%d%%&7)", enchant.getRarity().getColorCode(), enchant.getDisplayNameForLevel(level), chance, Math.max(0, 100 - chance)));
        ItemStack displayItem = new ItemStack(enchant.getRarity().getDisplayMaterial());
        ArmorStand labelStand = (ArmorStand)loc.getWorld().spawn(loc.clone().add(0.0, 1.2, 0.0), ArmorStand.class, stand -> {
            stand.setInvisible(true);
            stand.setMarker(true);
            stand.setGravity(false);
            stand.setSmall(true);
            stand.setSilent(true);
            stand.setCustomName(Text.color(text));
            stand.setCustomNameVisible(true);
        });
        ArmorStand hitStand = (ArmorStand)loc.getWorld().spawn(loc, ArmorStand.class, stand -> {
            stand.setInvisible(true);
            stand.setMarker(false);
            stand.setGravity(false);
            stand.setSmall(false);
            stand.setSilent(true);
            stand.setBasePlate(false);
            stand.setInvulnerable(false);
            stand.setCollidable(true);
            stand.setCustomNameVisible(false);
        });
        ItemDisplay icon = (ItemDisplay)loc.getWorld().spawn(loc, ItemDisplay.class, disp -> {
            disp.setItemStack(displayItem);
            Transformation t = new Transformation(new Vector3f(0.0f, 0.0f, 0.0f), new Quaternionf(), new Vector3f(0.8f, 0.8f, 0.8f), new Quaternionf());
            disp.setTransformation(t);
            disp.setBillboard(Display.Billboard.CENTER);
            disp.setSilent(true);
        });
        Player owner = Bukkit.getPlayer((UUID)playerId);
        this.hideFromOthers(owner, (Entity)labelStand);
        this.hideFromOthers(owner, (Entity)hitStand);
        this.hideFromOthers(owner, (Entity)icon);
        return new Option(playerId, enchant, level, chance, hitStand, labelStand, icon);
    }

    private boolean isEnchantable(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR) {
            return false;
        }
        return this.pickaxeManager.isPrisonPickaxe(stack) || this.gearManager.isGear(stack) || this.satchelManager.isSatchel(stack) || this.cellBusterManager.isBuster(stack);
    }

    private boolean hasFullEnergy(ItemStack stack) {
        if (this.pickaxeManager.isPrisonPickaxe(stack)) {
            return this.pickaxeManager.isFull(stack);
        }
        if (this.cellBusterManager.isBuster(stack)) {
            return this.cellBusterManager.isFull(stack);
        }
        if (this.gearManager.isGear(stack)) {
            return this.gearManager.isFull(stack);
        }
        if (this.satchelManager.isSatchel(stack)) {
            return this.satchelManager.isFull(stack);
        }
        return false;
    }

    private void repairBrokenGear(Player player, Session session) {
        session.resolved = true;
        this.stopSpin(session);
        ItemStack item = session.item;
        this.gearManager.repair(item);
        this.gearManager.setEnergy(item, 0);
        if (session.display != null && !session.display.isDead()) {
            session.display.setItemStack(item.clone());
            this.spawnNameHolograms(item, session, session.display.getLocation(), player);
        }
        this.showResult(session, true);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        player.sendMessage(Text.color("&aYour gear was repaired by the wormhole."));
        this.movePickaxeToPlayerAndReturn(player, session, item);
    }

    private boolean isInZone(Location location) {
        if (location == null || this.center == null) {
            return false;
        }
        if (!location.getWorld().equals((Object)this.center.getWorld())) {
            return false;
        }
        return location.toVector().setY(0).distanceSquared(this.center.toVector().setY(0)) <= this.radiusSquared;
    }

    private void returnItem(Player player, ItemStack stack) {
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack[]{stack});
        if (!leftover.isEmpty()) {
            leftover.values().forEach(item -> player.getWorld().dropItem(player.getLocation(), item));
        }
    }

    private void applyDust(Player player, Session session, ItemStack dust) {
        if (session.resolved) {
            player.sendMessage(Text.color("&cYou already selected an enchant."));
            return;
        }
        if (this.dustService == null || !this.dustService.isDust(dust)) {
            player.sendMessage(Text.color("&cThat dust cannot be used here."));
            return;
        }
        EnchantRarity rarity = this.dustService.getRarity(dust);
        int boost = this.dustService.getPercent(dust);
        if (rarity == null || boost <= 0) {
            player.sendMessage(Text.color("&cThat dust is invalid."));
            return;
        }
        this.consumeDust(player, dust);
        if (session.options.isEmpty()) {
            session.pendingDust.merge(rarity, boost, Integer::sum);
            player.sendMessage(Text.color("&aStored " + boost + "% " + rarity.name().toLowerCase() + " dust for your options."));
            return;
        }
        boolean applied = false;
        for (Option opt : session.options) {
            if (opt.enchant().getRarity() != rarity) continue;
            applied = true;
            int current = session.chanceBoosts.getOrDefault(opt, opt.chance());
            int newChance = Math.min(100, current + boost);
            session.chanceBoosts.put(opt, newChance);
            if (opt.labelStand() == null || opt.labelStand().isDead()) continue;
            String text = Text.color(String.format("%s%s &7(&a%d%%&7 / &c%d%%&7)", opt.enchant().getRarity().getColorCode(), opt.enchant().getDisplayNameForLevel(opt.level()), newChance, Math.max(0, 100 - newChance)));
            opt.labelStand().setCustomName(text);
        }
        if (applied) {
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);
            player.spawnParticle(Particle.ENCHANT, player.getLocation().add(0.0, 1.0, 0.0), 20, 0.4, 0.5, 0.4, 0.01);
            player.sendMessage(Text.color("&aApplied +" + boost + "% to all " + rarity.name().toLowerCase() + " options."));
        } else {
            player.sendMessage(Text.color("&cNo " + rarity.name().toLowerCase() + " options are available to boost."));
        }
    }

    private void handleSatchel(Player player, ItemStack satchel) {
        if (!this.satchelManager.isFull(satchel)) {
            player.sendMessage(Text.color(this.msgNotFull));
            return;
        }
        int currentEnergy = this.satchelManager.getEnergy(satchel);
        int maxEnergy = this.satchelManager.getMaxEnergy(satchel);
        int remaining = Math.max(0, currentEnergy - maxEnergy);
        this.satchelManager.levelUp(satchel, remaining);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.1f);
        player.sendMessage(Text.color(this.msgSatchelUpgraded));
        this.returnItem(player, satchel);
    }

    private int generateChance(EnchantRarity rarity) {
        double mean = 45.0;
        double sd = 15.0;
        if (rarity == EnchantRarity.ELITE || rarity == EnchantRarity.LEGENDARY) {
            mean = 35.0;
            sd = 12.0;
        }
        double val = this.plugin.getRandom().nextGaussian() * sd + mean;
        return (int)Math.round(Math.max(5.0, Math.min(85.0, val)));
    }

    private String roman(int number) {
        int n = Math.max(1, number);
        int[] values = new int[]{1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] numerals = new String[]{"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; ++i) {
            while (n >= values[i]) {
                sb.append(numerals[i]);
                n -= values[i];
            }
        }
        return sb.toString();
    }

    private double nameYOffset(int idx, int total) {
        double base = 1.3;
        double spacing = -0.22;
        double sizeNudge = (double)total * 0.05;
        double clearance = total > 1 ? 1.0 : 0.0;
        return base + sizeNudge + clearance + (double)idx * spacing;
    }

    private List<String> buildNameLines(ItemStack stack, Session session) {
        ArrayList<String> lines = new ArrayList<String>();
        String baseName = stack.hasItemMeta() && stack.getItemMeta().hasDisplayName() ? stack.getItemMeta().getDisplayName() : stack.getType().name().toLowerCase().replace("_", " ");
        lines.add(baseName);
        Map<String, Integer> enchants = session.isCellBuster ? this.cellBusterManager.getEnchants(stack) : (session.isPickaxe ? this.pickaxeManager.getEnchants(stack) : (session.isGear ? this.gearManager.getEnchants(stack) : Map.of()));
        if (!enchants.isEmpty()) {
            ArrayList<Map.Entry<String, Integer>> entries = new ArrayList<Map.Entry<String, Integer>>(enchants.entrySet());
            entries.sort(Map.Entry.comparingByKey());
            for (Map.Entry<String, Integer> entry : entries) {
                String id = entry.getKey();
                int level = entry.getValue();
                CustomEnchant ce = this.enchantService.getById(id);
                String format = ce != null ? ce.getLoreLine(level, "&d{enchant} {level}") : "&d{enchant} {level}";
                String line = format.replace("{enchant}", id).replace("{level}", this.roman(level));
                lines.add(line);
            }
        }
        return lines;
    }

    private void startSpin(Session session) {
        if (session.display == null || this.center == null) {
            return;
        }
        session.baseLocation = this.center.clone().add(0.0, this.displayHeight, 0.0);
        session.spinTaskId = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            if (!this.sessions.containsKey(session.playerId)) {
                this.stopSpin(session);
                return;
            }
            ItemDisplay display = session.display;
            if (display == null || display.isDead()) {
                this.stopSpin(session);
                return;
            }
            Location target = session.baseLocation.clone();
            display.teleport(target);
            Player owner = this.getPlayer(session.playerId);
            if (owner != null) {
                owner.spawnParticle(Particle.ENCHANT, target.getX(), target.getY(), target.getZ(), 10, 0.3, 0.5, 0.3, 0.0);
                owner.spawnParticle(Particle.WITCH, target.getX(), target.getY(), target.getZ(), 6, 0.2, 0.4, 0.2, 0.0);
            } else {
                World w = target.getWorld();
                if (w != null) {
                    w.spawnParticle(Particle.ENCHANT, target.getX(), target.getY(), target.getZ(), 10, 0.3, 0.5, 0.3, 0.0);
                    w.spawnParticle(Particle.WITCH, target.getX(), target.getY(), target.getZ(), 6, 0.2, 0.4, 0.2, 0.0);
                }
            }
            if (!session.nameStands.isEmpty()) {
                for (int i = 0; i < session.nameStands.size(); ++i) {
                    ArmorStand st = session.nameStands.get(i);
                    if (st == null || st.isDead()) continue;
                    double yOffset = this.nameYOffset(i, session.nameStands.size());
                    st.teleport(target.clone().add(0.0, yOffset, 0.0));
                }
            }
            this.tickOptionDisplays(session);
        }, 0L, (long)this.spinSpeedTicks).getTaskId();
    }

    private void stopSpin(Session session) {
        if (session.spinTaskId != -1) {
            Bukkit.getScheduler().cancelTask(session.spinTaskId);
            session.spinTaskId = -1;
        }
    }

    private void startOrbitAnimation(Session session, Option selected, Location centerLoc) {
        this.stopOrbit(session);
        session.orbitAngle = 0.0;
        session.orbitRadius = 1.2;
        ItemStack dye = new ItemStack(selected.enchant().getRarity().getDisplayMaterial());
        Location start = centerLoc.clone();
        Player owner = this.getPlayer(session.playerId);
        for (int i = 0; i < 15; ++i) {
            Vector3f dir = this.randomUnitVector().mul(1.0f + this.rng.nextFloat() * 0.4f);
            session.orbitDirs.add(dir);
            Location spawnLoc = start.clone().add((double)dir.x, (double)dir.y, (double)dir.z);
            ItemDisplay orb = this.spawnOrbitDisplay(dye, spawnLoc, owner);
            session.orbitDisplays.add(orb);
        }
        session.orbitTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            session.orbitAngle += 0.17453292519943295;
            session.orbitRadius = Math.max(0.25, session.orbitRadius - 0.03);
            for (int i = 0; i < session.orbitDisplays.size(); ++i) {
                ItemDisplay orb = session.orbitDisplays.get(i);
                if (orb == null || orb.isDead()) continue;
                Vector3f dir = session.orbitDirs.get(i);
                double angle = session.orbitAngle + (double)i * 0.4;
                double radius = session.orbitRadius;
                double x = centerLoc.getX() + (double)dir.x * radius + 0.1 * Math.cos(angle);
                double y = centerLoc.getY() + (double)dir.y * radius + 0.05 * Math.sin(angle * 2.0);
                double z = centerLoc.getZ() + (double)dir.z * radius + 0.1 * Math.sin(angle);
                Location pos = new Location(centerLoc.getWorld(), x, y, z);
                orb.teleport(pos);
                Transformation t = orb.getTransformation();
                Quaternionf rot = new Quaternionf().rotationY((float)(session.orbitAngle * 1.5));
                Transformation updated = new Transformation(t.getTranslation(), rot, t.getScale(), t.getRightRotation());
                orb.setTransformation(updated);
            }
        }, 0L, 2L);
        this.stopSound(session);
        session.soundTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            Player p = this.getPlayer(session.playerId);
            if (p != null) {
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.1f);
            } else if (centerLoc.getWorld() != null) {
                centerLoc.getWorld().playSound(centerLoc, Sound.BLOCK_ANVIL_USE, 1.0f, 1.1f);
            }
        }, 0L, 8L);
    }

    private void stopOrbit(Session session) {
        if (session.orbitTask != null) {
            session.orbitTask.cancel();
            session.orbitTask = null;
        }
        session.orbitDisplays.forEach(d -> {
            if (d != null && !d.isDead()) {
                d.remove();
            }
        });
        session.orbitDisplays.clear();
        session.orbitDirs.clear();
        this.stopSound(session);
    }

    private void stopSound(Session session) {
        if (session.soundTask != null) {
            session.soundTask.cancel();
            session.soundTask = null;
        }
    }

    private void hideFromOthers(Player owner, Entity entity) {
        if (entity == null) {
            return;
        }
        if (owner != null) {
            owner.showEntity((Plugin)this.plugin, entity);
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (owner != null && p.equals((Object)owner)) continue;
            p.hideEntity((Plugin)this.plugin, entity);
        }
    }

    private Player getPlayer(UUID id) {
        return Bukkit.getPlayer((UUID)id);
    }

    public void shutdown() {
        for (UUID id : new ArrayList<UUID>(this.sessions.keySet())) {
            this.endSession(id, true);
        }
        this.optionByEntity.clear();
    }

    private boolean canAcceptDust(Session session, ItemStack dust) {
        if (this.dustService == null || !this.dustService.isDust(dust)) {
            return false;
        }
        EnchantRarity rarity = this.dustService.getRarity(dust);
        if (rarity == null) {
            return false;
        }
        if (session.options.isEmpty()) {
            return true;
        }
        return session.options.stream().anyMatch(opt -> opt.enchant().getRarity() == rarity);
    }

    private void consumeDust(Player player, ItemStack dust) {
        ItemStack consume = dust.clone();
        consume.setAmount(1);
        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
            Map<Integer, ItemStack> leftovers = player.getInventory().removeItem(new ItemStack[]{consume});
            if (!leftovers.isEmpty()) {
                for (int slot = 0; slot < player.getInventory().getSize(); ++slot) {
                    ItemStack invItem = player.getInventory().getItem(slot);
                    if (invItem == null || !invItem.isSimilar(dust)) continue;
                    int amount = invItem.getAmount();
                    if (amount <= 1) {
                        player.getInventory().setItem(slot, null);
                        break;
                    }
                    invItem.setAmount(amount - 1);
                    player.getInventory().setItem(slot, invItem);
                    break;
                }
            }
            player.updateInventory();
        });
    }

    private void showResult(Session session, boolean success) {
        if (session.display == null || session.display.isDead()) {
            return;
        }
        if (session.resultStand != null && !session.resultStand.isDead()) {
            session.resultStand.remove();
        }
        Location base = session.display.getLocation().clone().add(0.0, 3.4, 0.0);
        String text = success ? "&a&lSuccess" : "&c&lFailed";
        Player owner = this.getPlayer(session.playerId);
        session.resultStand = (ArmorStand)base.getWorld().spawn(base, ArmorStand.class, st -> {
            st.setInvisible(true);
            st.setMarker(true);
            st.setGravity(false);
            st.setSmall(true);
            st.setSilent(true);
            st.setCustomName(Text.color(text));
            st.setCustomNameVisible(true);
        });
        this.hideFromOthers(owner, (Entity)session.resultStand);
    }

    private void movePickaxeToPlayerAndReturn(Player player, Session session, ItemStack item) {
        if (session.display == null || session.display.isDead()) {
            this.returnItem(player, item);
            this.endSession(player.getUniqueId(), false);
            return;
        }
        this.stopSpin(session);
        Location start = session.display.getLocation().clone();
        Location target = player.getLocation().add(player.getLocation().getDirection().normalize().multiply(2.5)).add(0.0, 1.5, 0.0);
        int steps = 20;
        double dx = (target.getX() - start.getX()) / (double)steps;
        double dy = (target.getY() - start.getY()) / (double)steps;
        double dz = (target.getZ() - start.getZ()) / (double)steps;
        int i = 0;
        while (i <= steps) {
            int tick = i++;
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                Location frame = start.clone().add(dx * (double)tick, dy * (double)tick, dz * (double)tick);
                if (session.display != null && !session.display.isDead()) {
                    session.display.teleport(frame);
                }
                this.updateNameStackPosition(session, frame);
                if (session.resultStand != null && !session.resultStand.isDead()) {
                    session.resultStand.teleport(frame.clone().add(0.0, 3.4, 0.0));
                }
            }, (long)tick);
        }
        BukkitTask followTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            if (session.display == null || session.display.isDead()) {
                return;
            }
            Location followTarget = player.getLocation().add(player.getLocation().getDirection().normalize().multiply(2.5)).add(0.0, 1.5, 0.0);
            session.display.teleport(followTarget);
            this.updateNameStackPosition(session, followTarget);
            if (session.resultStand != null && !session.resultStand.isDead()) {
                session.resultStand.teleport(followTarget.clone().add(0.0, 3.4, 0.0));
            }
        }, (long)steps + 1L, 2L);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            followTask.cancel();
            this.returnItem(player, item);
            this.endSession(player.getUniqueId(), false);
        }, (long)steps + 1L + 60L);
    }

    private void updateNameStackPosition(Session session, Location base) {
        if (session.nameStands.isEmpty()) {
            return;
        }
        for (int i = 0; i < session.nameStands.size(); ++i) {
            ArmorStand st = session.nameStands.get(i);
            if (st == null || st.isDead()) continue;
            double yOffset = this.nameYOffset(i, session.nameStands.size());
            st.teleport(base.clone().add(0.0, yOffset, 0.0));
        }
    }

    private double round(double v) {
        return (double)Math.round(v * 100.0) / 100.0;
    }

    private void applyPendingDust(Session session) {
        if (session.pendingDust.isEmpty()) {
            return;
        }
        for (Option opt : session.options) {
            int boost = session.pendingDust.getOrDefault((Object)opt.enchant().getRarity(), 0);
            if (boost <= 0) continue;
            int current = session.chanceBoosts.getOrDefault(opt, opt.chance());
            int newChance = Math.min(100, current + boost);
            session.chanceBoosts.put(opt, newChance);
            if (opt.labelStand() == null || opt.labelStand().isDead()) continue;
            String text = Text.color(String.format("%s%s &7(&a%d%%&7 / &c%d%%&7)", opt.enchant().getRarity().getColorCode(), opt.enchant().getDisplayNameForLevel(opt.level()), newChance, Math.max(0, 100 - newChance)));
            opt.labelStand().setCustomName(text);
        }
        session.pendingDust.clear();
    }

    private void tickOptionDisplays(Session session) {
        for (Option opt : session.options) {
            if (opt.iconDisplay() != null && !opt.iconDisplay().isDead()) {
                Transformation t = opt.iconDisplay().getTransformation();
                Quaternionf rot = new Quaternionf((Quaternionfc)t.getLeftRotation()).mul((Quaternionfc)new Quaternionf().rotationY(0.1308997f));
                Transformation updated = new Transformation(t.getTranslation(), rot, t.getScale(), t.getRightRotation());
                opt.iconDisplay().setTransformation(updated);
            }
            if (opt.labelStand() == null || opt.labelStand().isDead() || opt.armorStand() == null || opt.armorStand().isDead()) continue;
            Location base = opt.armorStand().getLocation();
            opt.labelStand().teleport(base.clone().add(0.0, 1.2, 0.0));
        }
    }

    private Vector3f randomUnitVector() {
        double theta = this.rng.nextDouble() * 2.0 * Math.PI;
        double phi = Math.acos(2.0 * this.rng.nextDouble() - 1.0);
        double x = Math.sin(phi) * Math.cos(theta);
        double y = Math.sin(phi) * Math.sin(theta);
        double z = Math.cos(phi);
        return new Vector3f((float)x, (float)y, (float)z);
    }

    private void animateIntoCenter(Session session, Location origin, Location destination, Runnable onComplete) {
        int steps = 15;
        double dx = (destination.getX() - origin.getX()) / (double)steps;
        double dy = (destination.getY() - origin.getY()) / (double)steps;
        double dz = (destination.getZ() - origin.getZ()) / (double)steps;
        int i = 0;
        while (i <= steps) {
            int tick = i++;
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (session.display == null || session.display.isDead()) {
                    return;
                }
                Location frame = origin.clone().add(dx * (double)tick, dy * (double)tick, dz * (double)tick);
                session.display.teleport(frame);
                if (!session.nameStands.isEmpty()) {
                    for (int idx = 0; idx < session.nameStands.size(); ++idx) {
                        ArmorStand st = session.nameStands.get(idx);
                        if (st == null || st.isDead()) continue;
                        double yOffset = this.nameYOffset(idx, session.nameStands.size());
                        st.teleport(frame.clone().add(0.0, yOffset, 0.0));
                    }
                }
            }, (long)tick);
        }
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, onComplete, (long)steps + 1L);
    }

    private void animateSelectedToCenter(Option opt, Location destination, Runnable onComplete) {
        ArmorStand stand = opt.armorStand();
        Location origin = stand.getLocation().clone();
        int steps = 12;
        double dx = (destination.getX() - origin.getX()) / (double)steps;
        double dy = (destination.getY() - origin.getY()) / (double)steps;
        double dz = (destination.getZ() - origin.getZ()) / (double)steps;
        int i = 0;
        while (i <= steps) {
            int tick = i++;
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (stand.isDead()) {
                    return;
                }
                Location frame = origin.clone().add(dx * (double)tick, dy * (double)tick, dz * (double)tick);
                stand.teleport(frame);
                if (opt.labelStand() != null && !opt.labelStand().isDead()) {
                    opt.labelStand().teleport(frame.clone().add(0.0, 1.2, 0.0));
                }
                if (opt.iconDisplay() != null && !opt.iconDisplay().isDead()) {
                    opt.iconDisplay().teleport(frame);
                }
            }, (long)tick);
        }
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, onComplete, (long)steps + 1L);
    }

    private void animateOptionTo(Option opt, Location target) {
        ArmorStand stand = opt.armorStand();
        Location start = stand.getLocation().clone();
        int steps = 10;
        double dx = (target.getX() - start.getX()) / (double)steps;
        double dy = (target.getY() - start.getY()) / (double)steps;
        double dz = (target.getZ() - start.getZ()) / (double)steps;
        int i = 0;
        while (i <= steps) {
            int tick = i++;
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (stand.isDead()) {
                    return;
                }
                Location frame = start.clone().add(dx * (double)tick, dy * (double)tick, dz * (double)tick);
                stand.teleport(frame);
                if (opt.labelStand() != null && !opt.labelStand().isDead()) {
                    opt.labelStand().teleport(frame.clone().add(0.0, 1.2, 0.0));
                }
                if (opt.iconDisplay() != null && !opt.iconDisplay().isDead()) {
                    opt.iconDisplay().teleport(frame);
                }
            }, (long)tick);
        }
    }

    private ItemDisplay spawnOrbitDisplay(ItemStack item, Location loc, Player owner) {
        ItemDisplay disp = (ItemDisplay)loc.getWorld().spawn(loc, ItemDisplay.class);
        disp.setItemStack(item);
        Transformation t = new Transformation(new Vector3f(0.0f, 0.0f, 0.0f), new Quaternionf(), new Vector3f(0.8f, 0.8f, 0.8f), new Quaternionf());
        disp.setTransformation(t);
        disp.setBillboard(Display.Billboard.CENTER);
        disp.setSilent(true);
        this.hideFromOthers(owner, (Entity)disp);
        return disp;
    }

    private void finalizeEnchant(Player player, Session session, Option option) {
        boolean success = false;
        this.stopOrbit(session);
        ItemStack item = session.item;
        int effectiveChance = session.chanceBoosts.getOrDefault(option, option.chance());
        success = session.isPickaxe || session.isCellBuster ? this.enchantService.rollSuccess(effectiveChance) == 1 : this.gearEnchantService.rollSuccess(effectiveChance) == 1;
        if ((session.isPickaxe || session.isCellBuster) && option.enchant.getId().equalsIgnoreCase("buster_efficiency") && !session.isCellBuster) {
            player.sendMessage(Text.color("&cThis enchant is only for Cell Busters."));
            this.endSession(player.getUniqueId(), true);
            return;
        }
        if (session.isGear && !option.enchant.isApplicableTo(item.getType())) {
            player.sendMessage(Text.color("&cThat enchant cannot be applied to this item."));
            this.endSession(player.getUniqueId(), true);
            return;
        }
        if (success) {
            if (session.isPickaxe) {
                this.pickaxeManager.applyEnchant(item, option.enchant.getId(), option.level, option.enchant.getMaxLevel());
                this.pickaxeManager.levelUp(item);
                int maxEnergy = this.pickaxeManager.getMaxEnergy(item);
                int currentEnergy = this.pickaxeManager.getEnergy(item);
                int remaining = Math.max(0, currentEnergy - maxEnergy);
                this.pickaxeManager.setEnergyOverflow(item, remaining);
            } else if (session.isCellBuster) {
                this.cellBusterManager.applyEnchant(item, option.enchant.getId(), option.level, option.enchant.getMaxLevel());
                this.cellBusterManager.levelUp(item);
                this.cellBusterManager.setEnergy(item, 0);
            } else {
                int maxEnergy = this.gearManager.getMaxEnergy(item);
                int currentEnergy = this.gearManager.getEnergy(item);
                int remaining = Math.max(0, currentEnergy - maxEnergy);
                if (session.brokenGear) {
                    this.gearManager.repair(item);
                }
                this.gearManager.applyEnchant(item, option.enchant.getId(), option.level, option.enchant.getMaxLevel());
                this.gearManager.levelUp(item);
                this.gearManager.setEnergy(item, remaining);
                this.gearManager.updateLore(item);
            }
            if (session.display != null && !session.display.isDead()) {
                this.spawnNameHolograms(item, session, session.display.getLocation(), player);
            }
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        } else {
            if (session.isPickaxe) {
                this.pickaxeManager.levelUp(item);
                this.pickaxeManager.setEnergyOverflow(item, Math.max(0, this.pickaxeManager.getEnergy(item) - this.pickaxeManager.getMaxEnergy(item)));
                if (this.dustService != null) {
                    ItemStack powder = this.dustService.createPowder(option.enchant().getRarity());
                    this.giveOrDrop(player, powder);
                    player.sendMessage(Text.color("&eYou received " + powder.getItemMeta().getDisplayName() + " &efrom the failed enchant."));
                }
            } else if (session.isCellBuster) {
                this.cellBusterManager.levelUp(item);
                this.cellBusterManager.setEnergy(item, 0);
            } else {
                this.gearManager.updateLore(item);
            }
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 1.0f, 1.0f);
        }
        this.showResult(session, success);
        this.movePickaxeToPlayerAndReturn(player, session, item);
    }

    private void giveOrDrop(Player player, ItemStack stack) {
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack[]{stack});
        if (!leftover.isEmpty()) {
            leftover.values().forEach(it -> player.getWorld().dropItem(player.getLocation(), it));
        }
    }

    private static class Session {
        private final UUID playerId;
        private final ItemStack item;
        private final boolean isPickaxe;
        private final boolean isGear;
        private final boolean isCellBuster;
        private final boolean brokenGear;
        private final List<Option> options = new ArrayList<Option>();
        private ItemDisplay display;
        private final List<ArmorStand> nameStands = new ArrayList<ArmorStand>();
        private final List<ItemDisplay> orbitDisplays = new ArrayList<ItemDisplay>();
        private final List<Vector3f> orbitDirs = new ArrayList<Vector3f>();
        private final Map<Option, Integer> chanceBoosts = new HashMap<Option, Integer>();
        private final Map<EnchantRarity, Integer> pendingDust = new HashMap<EnchantRarity, Integer>();
        private BukkitTask orbitTask;
        private BukkitTask soundTask;
        private double orbitAngle = 0.0;
        private double orbitRadius = 1.2;
        private boolean resolved = false;
        private int spinTaskId = -1;
        private Location baseLocation;
        private ArmorStand resultStand;

        Session(UUID playerId, ItemStack item, boolean isPickaxe, boolean isGear, boolean isCellBuster, boolean brokenGear) {
            this.playerId = playerId;
            this.item = item;
            this.isPickaxe = isPickaxe;
            this.isGear = isGear;
            this.isCellBuster = isCellBuster;
            this.brokenGear = brokenGear;
        }
    }

    private record Option(UUID playerId, CustomEnchant enchant, int level, int chance, ArmorStand armorStand, ArmorStand labelStand, ItemDisplay iconDisplay) {
    }
}
