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
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.InventoryHolder
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package org.axial.prisonsCore.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.service.EconomyService;
import org.axial.prisonsCore.service.ShardService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
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

public class ShardMenu
implements Listener {
    private final ShardService shardService;
    private final EconomyService economyService;
    private final PrisonsCore plugin;
    private static final int DEFAULT_SLOT = 22;
    private static final int WITHDRAW_SLOT = 45;
    private static final int ROLL_ALL_SLOT = 49;
    private static final List<Integer> SLOT_ORDER = ShardMenu.buildSlotOrder();

    public ShardMenu(PrisonsCore plugin, ShardService shardService, EconomyService economyService) {
        this.plugin = plugin;
        this.shardService = shardService;
        this.economyService = economyService;
    }

    public void open(Player player, ItemStack initialShard) {
        Inventory inv;
        Session holder = new Session();
        holder.owner = player.getUniqueId();
        holder.inv = inv = Bukkit.createInventory((InventoryHolder)holder, (int)54, (String)Text.color("&8Mining Fragments"));
        this.fillPanes(inv);
        if (initialShard != null && this.shardService.isShard(initialShard)) {
            this.placeOneShard(holder, initialShard.clone(), player, true);
        }
        inv.setItem(45, this.namedPane(Material.CHEST, "&cWithdraw All Fragments"));
        inv.setItem(49, this.namedPane(Material.LIME_DYE, "&aRoll All Fragments"));
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder inventoryHolder = event.getInventory().getHolder();
        if (!(inventoryHolder instanceof Session)) {
            return;
        }
        Session session = (Session)inventoryHolder;
        int raw = event.getRawSlot();
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();
        if (raw < event.getInventory().getSize()) {
            if (raw == 45) {
                this.withdrawAll(session, (Player)event.getWhoClicked());
                event.setCancelled(true);
                return;
            }
            if (raw == 49) {
                this.rollAll(session, (Player)event.getWhoClicked());
                event.setCancelled(true);
                return;
            }
            if (this.isShardSlot(raw)) {
                if (current != null && this.shardService.isShard(current) && !session.isSpinning(raw)) {
                    this.spin((Player)event.getWhoClicked(), session, raw, current);
                }
                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);
        } else {
            Player player = (Player)event.getWhoClicked();
            if (current != null && this.shardService.isShard(current)) {
                event.setCancelled(true);
                int slot = this.findNextShardSlot(session);
                if (slot == -1) {
                    player.sendMessage(Text.color("&cNo fragment slots free."));
                    return;
                }
                this.placeOneShard(session, current, player, false);
                int remaining = current.getAmount() - 1;
                if (remaining <= 0) {
                    event.getClickedInventory().setItem(event.getSlot(), null);
                } else {
                    current.setAmount(remaining);
                    event.getClickedInventory().setItem(event.getSlot(), current);
                }
                return;
            }
            if (cursor != null && this.shardService.isShard(cursor)) {
                event.setCancelled(true);
                if (!this.placeOneShard(session, cursor, player, false)) {
                    return;
                }
                cursor.setAmount(cursor.getAmount() - 1);
                event.setCursor(cursor.getAmount() <= 0 ? null : cursor);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        InventoryHolder inventoryHolder = event.getInventory().getHolder();
        if (!(inventoryHolder instanceof Session)) {
            return;
        }
        Session session = (Session)inventoryHolder;
        this.completeSpinsOnClose(session);
        this.deliverQueued(session, (Player)event.getPlayer());
        session.clear();
    }

    private void spin(final Player player, final Session session, final int slot, ItemStack shard) {
        final BukkitTask[] holder = new BukkitTask[1];
        final ShardService.Tier tier = this.shardService.getTier(shard);
        ItemStack remaining = shard.clone();
        remaining.setAmount(remaining.getAmount() - 1);
        if (remaining.getAmount() <= 0) {
            remaining = null;
        }
        session.inv.setItem(slot, remaining == null ? this.pane(Material.GRAY_STAINED_GLASS_PANE) : remaining);
        session.setSpinning(slot, true);
        final List<ItemStack> rewards = this.shardService.getRewards(tier);
        if (rewards.isEmpty()) {
            if (player != null) {
                player.sendMessage(Text.color("&cNo rewards configured for this fragment tier."));
            }
            session.setSpinning(slot, false);
            return;
        }
        final Inventory inv = session.inv;
        holder[0] = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, new Runnable(){
            int ticks = 0;
            int index = ShardMenu.this.plugin.getRandom().nextInt(Math.max(1, rewards.size()));

            @Override
            public void run() {
                if (!session.isSpinning(slot)) {
                    holder[0].cancel();
                    return;
                }
                if (this.ticks >= 100) {
                    ItemStack finalReward = ShardMenu.this.shardService.rollReward(tier);
                    if (finalReward != null) {
                        session.rewards[slot] = finalReward.clone();
                        inv.setItem(slot, finalReward.clone());
                        ShardMenu.this.handleRewardPayout(player == null ? session.owner : player.getUniqueId(), player, tier, finalReward, session);
                    } else {
                        inv.setItem(slot, ShardMenu.this.pane(Material.GRAY_STAINED_GLASS_PANE));
                    }
                    session.setSpinning(slot, false);
                    holder[0].cancel();
                    return;
                }
                ItemStack display = rewards.get(this.index % rewards.size()).clone();
                inv.setItem(slot, display);
                if (!session.soundGate) {
                    if (player != null) {
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
                    }
                    session.soundGate = true;
                    Bukkit.getScheduler().runTaskLater((Plugin)ShardMenu.this.plugin, () -> session.soundGate = false, 1L);
                }
                ++this.index;
                ++this.ticks;
            }
        }, 0L, 1L);
        session.addTask(holder[0]);
    }

    private ItemStack pane(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack namedPane(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Text.color(name));
        item.setItemMeta(meta);
        return item;
    }

    private void fillPanes(Inventory inv) {
        for (int i = 0; i < inv.getSize(); ++i) {
            if (this.isShardSlot(i)) {
                inv.setItem(i, this.pane(Material.GRAY_STAINED_GLASS_PANE));
                continue;
            }
            inv.setItem(i, this.pane(Material.GRAY_STAINED_GLASS_PANE));
        }
    }

    private boolean isShardSlot(int slot) {
        return slot >= 0 && slot <= 44;
    }

    private boolean placeOneShard(Session session, ItemStack shard, Player player, boolean fromOpen) {
        int slot = this.findNextShardSlot(session);
        if (slot == -1) {
            if (player != null) {
                player.sendMessage(Text.color("&cNo fragment slots free."));
            }
            return false;
        }
        ItemStack one = shard.clone();
        one.setAmount(1);
        session.inv.setItem(slot, one);
        return true;
    }

    private int findNextShardSlot(Session session) {
        Inventory inv = session.inv;
        for (int slot : SLOT_ORDER) {
            if (!this.isEmptyShardSlot(inv, slot)) continue;
            return slot;
        }
        return -1;
    }

    private boolean isEmptyShardSlot(Inventory inv, int slot) {
        ItemStack it = inv.getItem(slot);
        Session session = inv.getHolder() instanceof Session ? (Session)inv.getHolder() : null;
        if (!this.isShardSlot(slot) || session != null && (session.isSpinning(slot) || session.rewards[slot] != null)) {
            return false;
        }
        return it == null || it.getType().isAir() || this.isPlaceholderPane(it);
    }

    private boolean isPlaceholderPane(ItemStack item) {
        return item != null && item.getType() == Material.GRAY_STAINED_GLASS_PANE;
    }

    private void withdrawAll(Session session, Player player) {
        for (int i = 0; i <= 44; ++i) {
            ItemStack it = session.inv.getItem(i);
            if (this.shardService.isShard(it)) {
                player.getInventory().addItem(new ItemStack[]{it.clone()});
            }
            session.inv.setItem(i, this.pane(Material.GRAY_STAINED_GLASS_PANE));
        }
        this.deliverQueued(session, player);
        player.closeInventory();
    }

    private void rollAll(Session session, Player player) {
        for (int i = 0; i <= 44; ++i) {
            ItemStack it = session.inv.getItem(i);
            if (!this.shardService.isShard(it) || session.isSpinning(i)) continue;
            this.spin(player, session, i, it);
        }
    }

    private void deliverQueued(Session session, Player player) {
        for (PendingGive give : session.pendingGives) {
            if (!give.playerId.equals(player.getUniqueId())) continue;
            player.getInventory().addItem(new ItemStack[]{give.item.clone()});
        }
        for (PendingSell sell : session.pendingAutoSell) {
            if (!sell.playerId.equals(player.getUniqueId())) continue;
            double total = sell.worthEach * (double)sell.item.getAmount();
            this.economyService.deposit(player, total);
            player.sendMessage(Text.color("&aSold &f" + sell.item.getAmount() + "x " + sell.item.getType().name() + " &7for &a" + this.economyService.format(total)));
        }
        if (!session.pendingGives.isEmpty() || !session.pendingAutoSell.isEmpty()) {
            this.playWinSoundOnce(player, session);
        }
        session.pendingGives.clear();
        session.pendingAutoSell.clear();
        for (int i = 0; i <= 44; ++i) {
            if (session.rewards[i] == null) continue;
            session.inv.setItem(i, this.pane(Material.GRAY_STAINED_GLASS_PANE));
            session.rewards[i] = null;
        }
    }

    private void completeSpinsOnClose(Session session) {
        for (int slot = 0; slot <= 44; ++slot) {
            if (session.isSpinning(slot)) {
                ItemStack finalReward;
                ItemStack shard = session.inv.getItem(slot);
                ShardService.Tier tier = this.shardService.getTier(shard);
                if (tier == null) {
                    tier = ShardService.Tier.BASIC;
                }
                if ((finalReward = this.shardService.rollReward(tier)) != null) {
                    session.rewards[slot] = finalReward.clone();
                    session.inv.setItem(slot, finalReward.clone());
                    this.handleRewardPayout(session.owner, null, tier, finalReward, session);
                } else {
                    session.inv.setItem(slot, this.pane(Material.GRAY_STAINED_GLASS_PANE));
                }
                session.setSpinning(slot, false);
                continue;
            }
            ItemStack item = session.inv.getItem(slot);
            if (!this.shardService.isShard(item)) continue;
            session.pendingGives.add(new PendingGive(session.owner, item.clone()));
        }
    }

    private void handleRewardPayout(UUID ownerId, Player player, ShardService.Tier tier, ItemStack reward, Session session) {
        double worth;
        if (reward == null) {
            return;
        }
        if (ownerId == null) {
            if (player != null) {
                ownerId = player.getUniqueId();
            } else {
                return;
            }
        }
        if (tier == ShardService.Tier.BASIC && this.isAutoSellMaterial(reward.getType()) && (worth = this.economyService.getWorth(reward.getType())) > 0.0) {
            session.pendingAutoSell.add(new PendingSell(ownerId, reward.clone(), worth));
            if (player != null) {
                player.sendMessage(Text.color("&aFragment reward queued for sale: &f" + reward.getAmount() + "x " + reward.getType().name()));
            }
            return;
        }
        Player target = player;
        if (target == null && ownerId != null) {
            target = Bukkit.getPlayer((UUID)ownerId);
        }
        if (target != null) {
            target.getInventory().addItem(new ItemStack[]{reward.clone()});
            this.playWinSoundOnce(target, session);
            target.sendMessage(Text.color("&aYou received: &f" + reward.getType().name()));
        } else {
            session.pendingGives.add(new PendingGive(ownerId, reward.clone()));
        }
    }

    private void playWinSoundOnce(Player player, Session session) {
        if (session.winSoundGate) {
            return;
        }
        session.winSoundGate = true;
        if (player != null) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            session.winSoundGate = false;
        }, 5L);
    }

    private boolean isAutoSellMaterial(Material mat) {
        return mat == Material.COAL_ORE || mat == Material.COAL || mat == Material.IRON_ORE || mat == Material.IRON_INGOT;
    }

    private static List<Integer> buildSlotOrder() {
        ArrayList<Integer> slots = new ArrayList<Integer>();
        for (int i2 = 0; i2 <= 44; ++i2) {
            slots.add(i2);
        }
        slots.sort(Comparator.comparingInt(ShardMenu::manhattanFromDefault).thenComparingInt(i -> i));
        return slots;
    }

    private static int manhattanFromDefault(int slot) {
        int row = slot / 9;
        int col = slot % 9;
        int dRow = Math.abs(row - 2);
        int dCol = Math.abs(col - 4);
        return dRow + dCol;
    }

    private static class Session
    implements InventoryHolder {
        private Inventory inv;
        private final boolean[] spinning = new boolean[54];
        private final Set<BukkitTask> tasks = new HashSet<BukkitTask>();
        private boolean soundGate = false;
        private boolean winSoundGate = false;
        private final ItemStack[] rewards = new ItemStack[54];
        private final List<PendingSell> pendingAutoSell = new ArrayList<PendingSell>();
        private final List<PendingGive> pendingGives = new ArrayList<PendingGive>();
        private UUID owner;

        private Session() {
        }

        public Inventory getInventory() {
            return this.inv;
        }

        void clear() {
            this.tasks.forEach(BukkitTask::cancel);
            this.tasks.clear();
        }

        void setSpinning(int slot, boolean state) {
            this.spinning[slot] = state;
        }

        boolean isSpinning(int slot) {
            return this.spinning[slot];
        }

        void addTask(BukkitTask task) {
            this.tasks.add(task);
        }
    }

    private record PendingGive(UUID playerId, ItemStack item) {
    }

    private record PendingSell(UUID playerId, ItemStack item, double worthEach) {
    }
}
