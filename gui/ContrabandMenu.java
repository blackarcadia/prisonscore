package org.axial.prisonsCore.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.service.ContrabandService;
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

public class ContrabandMenu implements Listener {
    private static final int INVENTORY_SIZE = 27;
    private static final int[] REWARD_SLOTS = new int[]{12, 13, 14};
    private final PrisonsCore plugin;
    private final ContrabandService contrabandService;
    private final Map<UUID, Session> sessions = new HashMap<UUID, Session>();

    public ContrabandMenu(PrisonsCore plugin, ContrabandService contrabandService) {
        this.plugin = plugin;
        this.contrabandService = contrabandService;
    }

    public void open(Player player, String tierId) {
        Session existing = this.sessions.remove(player.getUniqueId());
        if (existing != null) {
            existing.cancel();
        }
        Session holder = new Session(player.getUniqueId(), tierId);
        Inventory inventory = Bukkit.createInventory(holder, INVENTORY_SIZE, Text.color(this.contrabandService.menuTitle(tierId)));
        holder.inventory = inventory;
        this.fillBackground(inventory);
        for (int slot : REWARD_SLOTS) {
            inventory.setItem(slot, this.namedPane(Material.CHEST, "&7Rolling reward..."));
        }
        player.openInventory(inventory);
        this.startSpin(player, holder);
        this.sessions.put(player.getUniqueId(), holder);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof Session)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof Session session)) {
            return;
        }
        if (session.completed) {
            this.sessions.remove(session.owner);
            return;
        }
        Player player = (Player)event.getPlayer();
        this.finalizeReward(player, session);
    }

    private void startSpin(Player player, Session session) {
        final BukkitTask[] taskHolder = new BukkitTask[1];
        taskHolder[0] = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            if (session.completed) {
                if (taskHolder[0] != null) {
                    taskHolder[0].cancel();
                }
                return;
            }
            if (session.ticks >= 50) {
                this.finalizeReward(player, session);
                if (taskHolder[0] != null) {
                    taskHolder[0].cancel();
                }
                return;
            }
            boolean playedSound = false;
            for (int slot : REWARD_SLOTS) {
                if (session.rewards.containsKey(slot)) {
                    continue;
                }
                ContrabandService.RewardRoll roll = this.contrabandService.rollReward(session.tierId);
                if (roll == null) {
                    continue;
                }
                session.inventory.setItem(slot, this.contrabandService.previewItem(roll));
                playedSound = true;
            }
            if (playedSound) {
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.6f, 1.4f);
            }
            session.ticks += 2;
        }, 0L, 2L);
        session.task = taskHolder[0];
    }

    private void finalizeReward(Player player, Session session) {
        if (session.completed) {
            return;
        }
        session.completed = true;
        if (session.task != null) {
            session.task.cancel();
        }
        boolean foundReward = false;
        for (int slot : REWARD_SLOTS) {
            ContrabandService.RewardRoll roll = this.contrabandService.rollReward(session.tierId);
            if (roll == null) {
                session.inventory.setItem(slot, this.namedPane(Material.BARRIER, "&cNo rewards configured"));
                continue;
            }
            foundReward = true;
            session.rewards.put(slot, roll);
            session.inventory.setItem(slot, this.contrabandService.previewItem(roll));
            this.contrabandService.award(player, roll);
        }
        if (!foundReward) {
            player.sendMessage(Text.color("&cThis contraband has no rewards configured."));
            this.sessions.remove(player.getUniqueId());
            return;
        }
        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
        player.sendTitle(Text.color("&6&lContraband Opened"), "", 5, 30, 10);
        player.sendMessage(Text.color("&8&m------------------------------"));
        player.sendMessage(Text.color("&6&lContraband Rewards &7(" + Text.strip(this.contrabandService.displayName(session.tierId)) + "&7)"));
        for (int slot : REWARD_SLOTS) {
            ContrabandService.RewardRoll reward = session.rewards.get(slot);
            if (reward == null) {
                continue;
            }
            player.sendMessage(Text.color("&8• &r" + this.contrabandService.rewardSummary(reward)));
        }
        player.sendMessage(Text.color("&8&m------------------------------"));
        this.sessions.remove(player.getUniqueId());
    }

    private void fillBackground(Inventory inventory) {
        ItemStack filler = this.pane(Material.BLACK_STAINED_GLASS_PANE);
        for (int slot = 0; slot < inventory.getSize(); ++slot) {
            inventory.setItem(slot, filler);
        }
    }

    private ItemStack pane(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack namedPane(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Text.color(name));
        item.setItemMeta(meta);
        return item;
    }

    private static class Session implements InventoryHolder {
        private final UUID owner;
        private final String tierId;
        private final Map<Integer, ContrabandService.RewardRoll> rewards = new HashMap<Integer, ContrabandService.RewardRoll>();
        private Inventory inventory;
        private BukkitTask task;
        private int ticks;
        private boolean completed;

        private Session(UUID owner, String tierId) {
            this.owner = owner;
            this.tierId = tierId;
        }

        private void cancel() {
            if (this.task != null) {
                this.task.cancel();
            }
        }

        @Override
        public Inventory getInventory() {
            return this.inventory;
        }
    }
}
