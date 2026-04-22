/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryCloseEvent
 *  org.bukkit.event.player.PlayerKickEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.InventoryHolder
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package org.axial.prisonsCore.gui;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
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
import org.axial.prisonsCore.util.Lang;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class EnchanterMenu
implements Listener {
    private static final int INVENTORY_SIZE = 45;
    private static final int CENTER_SLOT = 13;
    private static final int[] OPTION_SLOTS = new int[]{29, 30, 31, 32, 33};
    private static final int[] FILLER_SLOTS = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 34, 35};
    private static final int[] EXTRA_ROW_SLOTS = new int[]{36, 37, 38, 39, 40, 41, 42, 43, 44};
    private final PrisonsCore plugin;
    private final PickaxeManager pickaxeManager;
    private final CustomEnchantService enchantService;
    private final EnchantDustService dustService;
    private final GearManager gearManager;
    private final GearEnchantService gearEnchantService;
    private final SatchelManager satchelManager;
    private final CellBusterManager cellBusterManager;

    public EnchanterMenu(PrisonsCore plugin, PickaxeManager pickaxeManager, CustomEnchantService enchantService, EnchantDustService dustService, GearManager gearManager, GearEnchantService gearEnchantService, SatchelManager satchelManager, CellBusterManager cellBusterManager) {
        this.plugin = plugin;
        this.pickaxeManager = pickaxeManager;
        this.enchantService = enchantService;
        this.dustService = dustService;
        this.gearManager = gearManager;
        this.gearEnchantService = gearEnchantService;
        this.satchelManager = satchelManager;
        this.cellBusterManager = cellBusterManager;
    }

    public void open(Player player) {
        Inventory inv;
        EnchanterSession session = new EnchanterSession();
        session.inventory = inv = Bukkit.createInventory((InventoryHolder)session, (int)45, (String)Text.color("&8Enchanter"));
        this.fillPane(inv);
        player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.0f);
        player.openInventory(inv);
    }

    private boolean isSession(Inventory inventory) {
        return inventory != null && inventory.getHolder() instanceof EnchanterSession;
    }

    @EventHandler(ignoreCancelled=true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!this.isSession(event.getInventory())) {
            return;
        }
        EnchanterSession session = (EnchanterSession)event.getInventory().getHolder();
        Player player = (Player)event.getWhoClicked();
        int raw = event.getRawSlot();
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();
        if (raw >= event.getInventory().getSize()) {
            if (current != null && (this.pickaxeManager.isPrisonPickaxe(current) || this.cellBusterManager.isBuster(current) || this.gearManager.isGear(current) || this.satchelManager.isSatchel(current))) {
                event.setCancelled(true);
                if (!this.hasFullEnergy(current)) {
                    player.sendMessage(Lang.msg("enchanter.needs-full", "&cItem must have full energy to enchant."));
                    return;
                }
                this.placeIntoCenter(session, player, current, event);
                event.setCurrentItem(null);
            }
            return;
        }
        if (raw == 13) {
            if (cursor != null && !cursor.getType().isAir()) {
                if (!(this.pickaxeManager.isPrisonPickaxe(cursor) || this.cellBusterManager.isBuster(cursor) || this.gearManager.isGear(cursor) || this.satchelManager.isSatchel(cursor))) {
                    event.setCancelled(true);
                    player.sendMessage(Lang.msg("pickaxe.not-a-pickaxe", "&cYou must hold a valid item to do that."));
                    return;
                }
                if (!this.hasFullEnergy(cursor)) {
                    event.setCancelled(true);
                    player.sendMessage(Lang.msg("enchanter.needs-full", "&cItem must have full energy to enchant."));
                    return;
                }
                this.placeIntoCenter(session, player, cursor, event);
                return;
            }
            event.setCancelled(true);
            return;
        }
        if (raw < event.getInventory().getSize()) {
            if (session.slotEnchant.containsKey(raw)) {
                CustomEnchant enchant;
                if (cursor != null && this.dustService.isDust(cursor) && (enchant = session.slotEnchant.get(raw)) != null && this.dustService.getRarity(cursor) == enchant.getRarity()) {
                    int boost = this.dustService.getPercent(cursor);
                    int currentChance = session.slotSuccess.getOrDefault(raw, enchant.getSuccessChance());
                    int newChance = Math.min(100, currentChance + boost);
                    session.slotSuccess.put(raw, newChance);
                    int level = session.slotLevel.getOrDefault(raw, 1);
                    ItemStack updated = this.enchantService.createOptionItem(enchant, level, newChance);
                    session.inventory.setItem(raw, updated);
                    if (cursor.getAmount() <= 1) {
                        event.setCursor(null);
                    } else {
                        cursor.setAmount(cursor.getAmount() - 1);
                        event.setCursor(cursor);
                    }
                    event.setCancelled(true);
                    return;
                }
                if (session.pending || session.returning) {
                    event.setCancelled(true);
                    return;
                }
                event.setCancelled(true);
                this.applyEnchant(player, session, raw);
                return;
            }
            if (raw != 13) {
                event.setCancelled(true);
            }
        }
    }

    private void fillPane(Inventory inventory) {
        ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(" ");
        pane.setItemMeta(meta);
        ItemStack gray = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gmeta = gray.getItemMeta();
        gmeta.setDisplayName(" ");
        gray.setItemMeta(gmeta);
        for (int slot : FILLER_SLOTS) {
            inventory.setItem(slot, pane);
        }
        for (int slot : EXTRA_ROW_SLOTS) {
            inventory.setItem(slot, gray);
        }
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

    private void placeIntoCenter(EnchanterSession session, Player player, ItemStack sourceItem, InventoryClickEvent event) {
        if (session.inventory.getItem(13) == null || session.inventory.getItem(13).getType().isAir()) {
            session.inventory.setItem(13, sourceItem.clone());
            if (event.isShiftClick()) {
                event.setCurrentItem(null);
            } else {
                event.setCursor(null);
            }
            this.refreshOptions(session);
            if (this.satchelManager.isSatchel(sourceItem)) {
                this.startSatchelUpgrade(session, player);
            }
        } else {
            player.sendMessage(Lang.msg("enchanter.remove-first", "&cRemove the current pickaxe first."));
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!this.isSession(event.getInventory())) {
            return;
        }
        EnchanterSession session = (EnchanterSession)event.getInventory().getHolder();
        ItemStack center = session.inventory.getItem(13);
        if (center != null && !center.getType().isAir() && (!session.enchantChosen || session.returning || session.pending)) {
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> event.getPlayer().openInventory(session.inventory));
            ((Player)event.getPlayer()).sendMessage(Lang.msg("enchanter.select-first", "&cSelect an enchant before leaving the enchanter."));
            return;
        }
        if (center != null && !center.getType().isAir()) {
            event.getPlayer().getInventory().addItem(new ItemStack[]{center});
        }
        session.clearOptions();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        InventoryHolder inventoryHolder = event.getPlayer().getOpenInventory().getTopInventory().getHolder();
        if (!(inventoryHolder instanceof EnchanterSession)) {
            return;
        }
        EnchanterSession session = (EnchanterSession)inventoryHolder;
        ItemStack center = session.inventory.getItem(13);
        if (center != null && !center.getType().isAir()) {
            event.getPlayer().getInventory().addItem(new ItemStack[]{center});
        }
        session.clearOptions();
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        InventoryHolder inventoryHolder = event.getPlayer().getOpenInventory().getTopInventory().getHolder();
        if (!(inventoryHolder instanceof EnchanterSession)) {
            return;
        }
        EnchanterSession session = (EnchanterSession)inventoryHolder;
        ItemStack center = session.inventory.getItem(13);
        if (center != null && !center.getType().isAir()) {
            event.getPlayer().getInventory().addItem(new ItemStack[]{center});
        }
        session.clearOptions();
    }

    private void applyEnchant(Player player, EnchanterSession session, int slot) {
        boolean brokenGear;
        boolean isGear;
        ItemStack pickaxe = session.inventory.getItem(13);
        if (pickaxe == null || pickaxe.getType().isAir()) {
            player.sendMessage(Text.color("&cPlace gear or a pickaxe in the center."));
            return;
        }
        if (this.satchelManager.isSatchel(pickaxe)) {
            player.sendMessage(Text.color("&cSatchels upgrade automatically when placed."));
            return;
        }
        boolean isCellBuster = this.cellBusterManager.isBuster(pickaxe);
        boolean isPickaxe = !isCellBuster && this.pickaxeManager.isPrisonPickaxe(pickaxe);
        isGear = !isPickaxe && !isCellBuster && this.gearManager.isGear(pickaxe);
        if (!(isPickaxe || isGear || isCellBuster)) {
            player.sendMessage(Text.color("&cInvalid item for enchanting."));
            return;
        }
        brokenGear = isGear && this.gearManager.isBroken(pickaxe);
        if (isGear && !brokenGear && !this.gearManager.isFull(pickaxe)) {
            player.sendMessage(Text.color("&cFill this gear's energy before enchanting."));
            return;
        }
        CustomEnchant enchant = session.slotEnchant.get(slot);
        int level = session.slotLevel.getOrDefault(slot, 1);
        if (enchant == null) {
            return;
        }
        int chance = session.slotSuccess.getOrDefault(slot, enchant.getSuccessChance());
        if ((isPickaxe || isCellBuster) && enchant.getId().equalsIgnoreCase("buster_efficiency") && !isCellBuster) {
            player.sendMessage(Text.color("&cThis enchant is only for Cell Busters."));
            session.pending = false;
            session.returning = false;
            return;
        }
        if (isGear && !enchant.isApplicableTo(pickaxe.getType())) {
            player.sendMessage(Text.color("&cThat enchant cannot be applied to this item."));
            session.pending = false;
            session.returning = false;
            return;
        }
        session.pending = true;
        session.selectedSlot = slot;
        this.fillSlotsWithPane(session.inventory, slot, Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.0f);
        session.bellTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 1.2f), 0L, 5L);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            boolean success;
            if (session.bellTask != null) {
                session.bellTask.cancel();
            }
            success = (isPickaxe || isCellBuster ? this.enchantService.rollSuccess(chance) : this.gearEnchantService.rollSuccess(chance)) == 1;
            if (success) {
                if (isPickaxe) {
                    this.pickaxeManager.applyEnchant(pickaxe, enchant.getId(), level, enchant.getMaxLevel());
                } else if (isCellBuster) {
                    this.cellBusterManager.applyEnchant(pickaxe, enchant.getId(), level, enchant.getMaxLevel());
                } else {
                    if (brokenGear) {
                        this.gearManager.repair(pickaxe);
                    }
                    this.gearManager.applyEnchant(pickaxe, enchant.getId(), level, enchant.getMaxLevel());
                    this.gearManager.levelUp(pickaxe);
                }
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                this.setPane(session.inventory, session.selectedSlot, Material.LIME_STAINED_GLASS_PANE);
                player.sendMessage(Lang.msg("enchanter.enchant-success", "&a{enchant} {level} applied!").replace("{enchant}", enchant.getId()).replace("{level}", String.valueOf(level)));
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 1.0f, 1.0f);
                this.setPane(session.inventory, session.selectedSlot, Material.RED_STAINED_GLASS_PANE);
                player.sendMessage(Lang.msg("enchanter.enchant-failed", "&c{enchant} failed to apply.").replace("{enchant}", enchant.getId()));
            }
            if (isPickaxe) {
                this.pickaxeManager.levelUp(pickaxe);
                int maxEnergy = this.pickaxeManager.getMaxEnergy(pickaxe);
                int currentEnergy = this.pickaxeManager.getEnergy(pickaxe);
                int remaining = Math.max(0, currentEnergy - maxEnergy);
                this.pickaxeManager.setEnergyOverflow(pickaxe, remaining);
            } else if (isCellBuster) {
                this.cellBusterManager.levelUp(pickaxe);
                this.cellBusterManager.setEnergy(pickaxe, 0);
            } else {
                int maxEnergy = this.gearManager.getMaxEnergy(pickaxe);
                int currentEnergy = this.gearManager.getEnergy(pickaxe);
                int remaining = Math.max(0, currentEnergy - maxEnergy);
                this.gearManager.setEnergy(pickaxe, remaining);
                this.gearManager.updateLore(pickaxe);
            }
            session.enchantChosen = true;
            session.pending = false;
            session.returning = true;
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                ItemStack centerNow = session.inventory.getItem(13);
                if (centerNow != null && !centerNow.getType().isAir()) {
                    session.inventory.setItem(13, null);
                    player.getInventory().addItem(new ItemStack[]{centerNow});
                }
                session.clearOptions();
                player.closeInventory();
                session.returning = false;
            }, 100L);
        }, 60L);
    }

    private void startSatchelUpgrade(EnchanterSession session, Player player) {
        ItemStack satchel = session.inventory.getItem(13);
        if (satchel == null || satchel.getType().isAir() || !this.satchelManager.isSatchel(satchel)) {
            return;
        }
        if (!this.satchelManager.isFull(satchel)) {
            player.sendMessage(Lang.msg("enchanter.needs-full", "&cItem must have full energy to enchant."));
            return;
        }
        if (session.pending) {
            return;
        }
        session.pending = true;
        session.selectedSlot = -1;
        session.enchantChosen = false;
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.0f);
        session.bellTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 1.2f), 0L, 5L);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            ItemStack current;
            if (session.bellTask != null) {
                session.bellTask.cancel();
            }
            if ((current = session.inventory.getItem(13)) == null || current.getType().isAir() || !this.satchelManager.isSatchel(current)) {
                session.pending = false;
                return;
            }
            int currentEnergy = this.satchelManager.getEnergy(current);
            int maxEnergy = this.satchelManager.getMaxEnergy(current);
            int remaining = Math.max(0, currentEnergy - maxEnergy);
            this.satchelManager.levelUp(current, remaining);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.1f);
            session.enchantChosen = true;
            session.pending = false;
            session.returning = true;
            session.inventory.setItem(13, null);
            player.getInventory().addItem(new ItemStack[]{current});
            session.clearOptions();
            player.closeInventory();
            session.returning = false;
        }, 60L);
    }

    private void refreshOptions(EnchanterSession session) {
        Map<String, Integer> current;
        boolean isGear;
        ItemStack center = session.inventory.getItem(13);
        if (center == null || center.getType().isAir()) {
            session.clearOptions();
            return;
        }
        if (this.satchelManager.isSatchel(center)) {
            session.clearOptions();
            return;
        }
        boolean isCellBuster = this.cellBusterManager.isBuster(center);
        boolean isPickaxe = !isCellBuster && this.pickaxeManager.isPrisonPickaxe(center);
        boolean bl = isGear = !isPickaxe && !isCellBuster && this.gearManager.isGear(center);
        if (!(isPickaxe || isGear || isCellBuster)) {
            session.clearOptions();
            return;
        }
        boolean brokenGear = isGear && this.gearManager.isBroken(center);
        session.clearOptions();
        Map<String, Integer> map = current = isPickaxe ? this.pickaxeManager.getEnchants(center) : this.gearManager.getEnchants(center);
        if (brokenGear) {
            for (int slot : OPTION_SLOTS) {
                session.inventory.setItem(slot, null);
            }
            session.slotEnchant.clear();
            session.slotLevel.clear();
            session.slotSuccess.clear();
            session.brokenGear = true;
            return;
        }
        List<CustomEnchant> pool = isCellBuster ? this.enchantService.getAll().stream().filter(ce -> ce.getId().equalsIgnoreCase("buster_efficiency")).collect(Collectors.toList()) : (isPickaxe ? this.enchantService.getAll().stream().filter(ce -> !ce.getId().equalsIgnoreCase("buster_efficiency")).collect(Collectors.toList()) : this.gearEnchantService.getAll().stream().filter(ce -> ce.isApplicableTo(center.getType())).collect(Collectors.toList()));
        List<CustomEnchant> candidates = pool.stream().filter(ce -> current.getOrDefault(ce.getId(), 0) < ce.getMaxLevel()).collect(Collectors.toList());
        List<CustomEnchant> options = isCellBuster || isPickaxe ? this.enchantService.pickRandomOptions(Math.min(OPTION_SLOTS.length, candidates.size()), candidates) : this.gearEnchantService.pickRandomOptions(Math.min(OPTION_SLOTS.length, candidates.size()), candidates);
        if (options.isEmpty() && !candidates.isEmpty()) {
            options = candidates.subList(0, Math.min(OPTION_SLOTS.length, candidates.size()));
        }
        if (options.isEmpty()) {
            ItemStack placeholder = new ItemStack(Material.BARRIER);
            ItemMeta pm = placeholder.getItemMeta();
            pm.setDisplayName(Text.color("&cNo enchants available"));
            placeholder.setItemMeta(pm);
            for (int slot : OPTION_SLOTS) {
                session.inventory.setItem(slot, placeholder);
            }
            session.slotEnchant.clear();
            session.slotLevel.clear();
            session.slotSuccess.clear();
            return;
        }
        for (int i = 0; i < options.size(); ++i) {
            CustomEnchant enchant = options.get(i);
            int nextLevel = Math.min(enchant.getMaxLevel(), current.getOrDefault(enchant.getId(), 0) + 1);
            int successChance = this.generateChance(enchant.getRarity());
            ItemStack item = isPickaxe ? this.enchantService.createOptionItem(enchant, nextLevel, successChance) : (isCellBuster ? this.enchantService.createOptionItem(enchant, nextLevel, successChance) : this.gearEnchantService.createOptionItem(enchant, nextLevel, successChance));
            int slot = OPTION_SLOTS[i];
            if ((isPickaxe || isCellBuster) && enchant.getId().equalsIgnoreCase("buster_efficiency")) {
                slot = OPTION_SLOTS[OPTION_SLOTS.length / 2];
            }
            session.inventory.setItem(slot, item);
            session.slotEnchant.put(slot, enchant);
            session.slotLevel.put(slot, nextLevel);
            session.slotSuccess.put(slot, successChance);
        }
        session.brokenGear = false;
    }

    private int generateChance() {
        return this.generateChance(null);
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

    private void setPane(Inventory inventory, int slot, Material material) {
        ItemStack pane = new ItemStack(material);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(" ");
        pane.setItemMeta(meta);
        inventory.setItem(slot, pane);
    }

    private void fillSlotsWithPane(Inventory inventory, int skipSlot, Material material) {
        for (int slot : OPTION_SLOTS) {
            if (slot == skipSlot) continue;
            this.setPane(inventory, slot, material);
        }
    }

    private static class EnchanterSession
    implements InventoryHolder {
        private Inventory inventory;
        private final Map<Integer, CustomEnchant> slotEnchant = new HashMap<Integer, CustomEnchant>();
        private final Map<Integer, Integer> slotLevel = new HashMap<Integer, Integer>();
        private final Map<Integer, Integer> slotSuccess = new HashMap<Integer, Integer>();
        private boolean enchantChosen = false;
        private boolean pending = false;
        private boolean returning = false;
        private BukkitTask bellTask;
        private int selectedSlot = -1;
        private boolean brokenGear = false;

        private EnchanterSession() {
        }

        public Inventory getInventory() {
            return this.inventory;
        }

        void clearOptions() {
            if (this.inventory == null) {
                return;
            }
            for (int slot : OPTION_SLOTS) {
                this.inventory.setItem(slot, null);
            }
            this.slotEnchant.clear();
            this.slotLevel.clear();
            this.slotSuccess.clear();
            this.selectedSlot = -1;
            this.pending = false;
            this.returning = false;
            if (this.bellTask != null) {
                this.bellTask.cancel();
                this.bellTask = null;
            }
        }
    }
}
