/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.Projectile
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.block.BlockBreakEvent
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.entity.EntityDamageEvent
 *  org.bukkit.event.entity.EntityDeathEvent
 *  org.bukkit.event.entity.EntityRegainHealthEvent
 *  org.bukkit.event.entity.PlayerDeathEvent
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryCloseEvent
 *  org.bukkit.event.player.PlayerInteractEntityEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.event.player.PlayerMoveEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.inventory.EquipmentSlot
 *  org.bukkit.inventory.InventoryHolder
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.projectiles.ProjectileSource
 */
package org.axial.prisonsCore.cell.guard;

import java.util.UUID;
import org.axial.prisonsCore.cell.Cell;
import org.axial.prisonsCore.cell.CellService;
import org.axial.prisonsCore.cell.guard.CellGuardMenu;
import org.axial.prisonsCore.cell.guard.CellGuardService;
import org.axial.prisonsCore.cell.guard.CellGuardTier;
import org.axial.prisonsCore.service.FeatureToggleService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;

public class CellGuardListener
implements Listener {
    private final CellService cellService;
    private final CellGuardService cellGuardService;
    private final FeatureToggleService featureToggleService;

    public CellGuardListener(CellService cellService, CellGuardService cellGuardService, FeatureToggleService featureToggleService) {
        this.cellService = cellService;
        this.cellGuardService = cellGuardService;
        this.featureToggleService = featureToggleService;
    }

    @EventHandler(ignoreCancelled=false)
    public void onAnchorUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getItem() == null || !this.cellGuardService.isGuardAnchor(event.getItem())) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }
        if (!this.featureToggleService.checkEnabled(event.getPlayer(), FeatureToggleService.Feature.CELL_GUARDS)) {
            event.setCancelled(true);
            return;
        }
        Player player = event.getPlayer();
        Location placeLoc = event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null ? event.getClickedBlock().getLocation() : player.getLocation().getBlock().getLocation();
        Cell cell = this.cellService.getCellAt(placeLoc);
        if (cell == null || cell.getOwner() == null || !cell.getOwner().equals(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(Text.color("&cUse cell guard anchors inside your own cell."));
            return;
        }
        CellGuardTier tier = this.cellGuardService.tierFromItem(event.getItem());
        if (tier == null) {
            event.setCancelled(true);
            player.sendMessage(Text.color("&cInvalid cell guard data on this item."));
            return;
        }
        event.setCancelled(true);
        if (this.cellGuardService.placeGuard(cell, tier, placeLoc, player)) {
            ItemStack item = event.getItem();
            item.setAmount(item.getAmount() - 1);
            if (item.getAmount() <= 0) {
                player.getInventory().setItemInMainHand(null);
            } else {
                player.getInventory().setItemInMainHand(item);
            }
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onGuardRightClick(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() == null) {
            return;
        }
        if (!this.cellGuardService.isCellGuard(event.getRightClicked())) {
            return;
        }
        Player player = event.getPlayer();
        UUID placementId = this.cellGuardService.getPlacementIdByNpc(event.getRightClicked().getUniqueId());
        if (placementId == null) {
            return;
        }
        CellGuardService.Placement placement = this.cellGuardService.getPlacement(placementId);
        if (placement == null) {
            return;
        }
        Cell cell = this.cellService.getCell(placement.cellId());
        if (cell == null || cell.getOwner() == null || !cell.getOwner().equals(player.getUniqueId())) {
            return;
        }
        event.setCancelled(true);
        if (!this.featureToggleService.checkEnabled(player, FeatureToggleService.Feature.CELL_GUARDS)) {
            return;
        }
        CellGuardMenu.open(player, placementId);
    }

    @EventHandler(ignoreCancelled=true)
    public void onMenuClick(InventoryClickEvent event) {
        Player clicker;
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof CellGuardMenu)) {
            return;
        }
        CellGuardMenu menu = (CellGuardMenu)holder;
        event.setCancelled(true);
        if (event.getRawSlot() != 4) {
            return;
        }
        UUID placementId = menu.getPlacementId();
        if (this.cellGuardService.removePlacement(placementId, clicker = (Player)event.getWhoClicked(), true)) {
            clicker.closeInventory();
            clicker.sendMessage(Text.color("&cCell guard removed and anchor returned."));
        }
    }

    @EventHandler
    public void onMenuClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof CellGuardMenu) {
            // empty if block
        }
    }

    @EventHandler(ignoreCancelled=false)
    public void onDoorHit(BlockBreakEvent event) {
        if (event.getBlock() == null) {
            return;
        }
        if (!this.cellService.isCellDoor(event.getBlock())) {
            return;
        }
        Cell cell = this.cellService.getCellByDoor(event.getBlock());
        this.cellGuardService.handleDoorRaid(cell, event.getPlayer());
    }

    @EventHandler(ignoreCancelled=true)
    public void onGuardHit(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        LivingEntity living = (LivingEntity)entity;
        if (!this.cellGuardService.isCellGuard((Entity)living)) {
            return;
        }
        Player attacker = this.asPlayer(event.getDamager());
        if (attacker == null) {
            return;
        }
        UUID placementId = this.cellGuardService.getPlacementIdByNpc(living.getUniqueId());
        if (placementId == null) {
            return;
        }
        CellGuardService.Placement placement = this.cellGuardService.getPlacement(placementId);
        if (placement == null) {
            return;
        }
        this.cellGuardService.markRaider(attacker.getUniqueId(), placement.cellId());
        this.cellGuardService.commandAttack(living, attacker);
    }

    @EventHandler(ignoreCancelled=true)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        Player attacker = this.asPlayer(event.getDamager());
        if (attacker == null) {
            return;
        }
        if (this.cellGuardService.isCellGuard(event.getEntity())) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player victim = (Player)entity;
        Cell cell = this.cellService.getCellAt(victim.getLocation());
        if (cell == null) {
            return;
        }
        this.cellGuardService.handleOccupantAttack(attacker, victim, cell);
    }

    @EventHandler(ignoreCancelled=true)
    public void onGuardKill(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player victim = (Player)entity;
        Entity damager = event.getDamager();
        if (damager instanceof Projectile proj) {
            ProjectileSource projectileSource = proj.getShooter();
            if (projectileSource instanceof Entity shooter) {
                damager = shooter;
            }
        }
        if (!this.cellGuardService.isCellGuard(damager)) {
            return;
        }
        double finalHealth = victim.getHealth() - event.getFinalDamage();
        if (finalHealth <= 0.0) {
            this.cellGuardService.handlePlayerDeath(victim);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        this.cellGuardService.handlePlayerDeath(event.getEntity());
    }

    @EventHandler
    public void onGuardDeath(EntityDeathEvent event) {
        LivingEntity livingEntity;
        if (this.cellGuardService.isCellGuard((Entity)event.getEntity()) && (livingEntity = event.getEntity()) instanceof LivingEntity) {
            LivingEntity living = livingEntity;
            this.cellGuardService.handleGuardDeath(living);
        }
    }

    @EventHandler
    public void onGuardDamaged(EntityDamageEvent event) {
        Entity entity;
        if (this.cellGuardService.isCellGuard(event.getEntity()) && (entity = event.getEntity()) instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            living.getServer().getScheduler().runTask((Plugin)this.cellGuardService.getPlugin(), () -> this.cellGuardService.updateGuardName(living));
        }
    }

    @EventHandler
    public void onGuardHeal(EntityRegainHealthEvent event) {
        Entity entity;
        if (this.cellGuardService.isCellGuard(event.getEntity()) && (entity = event.getEntity()) instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            living.getServer().getScheduler().runTask((Plugin)this.cellGuardService.getPlugin(), () -> this.cellGuardService.updateGuardName(living));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.cellGuardService.handlePlayerQuit(event.getPlayer());
    }

    @EventHandler(ignoreCancelled=true)
    public void onMove(PlayerMoveEvent event) {
        if (!this.cellGuardService.isTrackedRaider(event.getPlayer().getUniqueId())) {
            return;
        }
        if (event.getTo() == null) {
            return;
        }
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockY() == event.getTo().getBlockY() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        this.cellGuardService.handlePlayerMove(event.getPlayer(), event.getTo());
    }

    private Player asPlayer(Entity entity) {
        if (entity instanceof Player p) {
            return p;
        }
        if (entity instanceof Projectile proj) {
            ProjectileSource projectileSource = proj.getShooter();
            if (projectileSource instanceof Player shooter) {
                return shooter;
            }
        }
        return null;
    }
}
