package org.axial.prisonsCore.gui;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.axial.prisonsCore.cell.guard.CellGuardService;
import org.axial.prisonsCore.cell.guard.CellGuardTier;
import net.kyori.adventure.text.Component;
import org.axial.prisonsCore.cell.CellDoorType;
import org.axial.prisonsCore.cell.CellService;
import org.axial.prisonsCore.model.PickaxeType;
import org.axial.prisonsCore.service.EconomyService;
import org.axial.prisonsCore.service.FeatureToggleService;
import org.axial.prisonsCore.service.GearManager;
import org.axial.prisonsCore.service.GeneratorService;
import org.axial.prisonsCore.service.PickaxeManager;
import org.axial.prisonsCore.service.SatchelManager;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShopMenu implements Listener {
    private static final int ROOT_INVENTORY_SIZE = 27;
    private static final int CATEGORY_INVENTORY_SIZE = 36;
    private static final int GEAR_CATEGORY_INVENTORY_SIZE = 45;
    private static final int GENERATOR_CATEGORY_INVENTORY_SIZE = 45;
    private static final int[] CATEGORY_SLOTS = new int[]{11, 12, 13, 14, 15};
    private static final int[] DEFAULT_ITEM_SLOTS = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
    private static final int[] GEAR_ITEM_SLOTS = new int[]{0, 1, 2, 3, 5, 6, 9, 10, 11, 12, 14, 15, 18, 19, 20, 21, 23, 24, 27, 28, 29, 30, 32, 33};
    private static final int[] GENERATOR_ITEM_SLOTS = new int[]{10, 11, 12, 13, 14, 15, 16, 28, 29, 30, 31, 32, 33, 34};
    private final EconomyService economyService;
    private final PickaxeManager pickaxeManager;
    private final GearManager gearManager;
    private final CellService cellService;
    private final CellGuardService cellGuardService;
    private final GeneratorService generatorService;
    private final FeatureToggleService featureToggleService;
    private final SatchelManager satchelManager;
    private final Map<Category, List<ShopItem>> itemsByCategory = new EnumMap<>(Category.class);

    public ShopMenu(EconomyService economyService, PickaxeManager pickaxeManager, GearManager gearManager, CellService cellService, CellGuardService cellGuardService, GeneratorService generatorService, FeatureToggleService featureToggleService, SatchelManager satchelManager) {
        this.economyService = economyService;
        this.pickaxeManager = pickaxeManager;
        this.gearManager = gearManager;
        this.cellService = cellService;
        this.cellGuardService = cellGuardService;
        this.generatorService = generatorService;
        this.featureToggleService = featureToggleService;
        this.satchelManager = satchelManager;
        this.loadItems();
    }

    public void open(Player player) {
        RootHolder holder = new RootHolder();
        Inventory inventory = Bukkit.createInventory(holder, ROOT_INVENTORY_SIZE, Text.color("&8Server Shop"));
        holder.inventory = inventory;
        this.fillBackground(inventory);
        for (int i = 0; i < Category.values().length && i < CATEGORY_SLOTS.length; ++i) {
            Category category = Category.values()[i];
            inventory.setItem(CATEGORY_SLOTS[i], this.categoryItem(category));
        }
        player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.0f);
        player.openInventory(inventory);
    }

    private void openCategory(Player player, Category category) {
        CategoryHolder holder = new CategoryHolder(category);
        Inventory inventory = Bukkit.createInventory(holder, this.inventorySize(category), Text.color("&8" + Text.strip(category.displayName)));
        holder.inventory = inventory;
        this.fillBackground(inventory);
        inventory.setItem(this.backSlot(category), this.namedItem(Material.NETHER_STAR, "&c&lBack"));
        List<ShopItem> items = this.itemsByCategory.getOrDefault(category, List.of());
        int[] itemSlots = this.itemSlots(category);
        for (int i = 0; i < items.size() && i < itemSlots.length; ++i) {
            inventory.setItem(itemSlots[i], this.shopItem(items.get(i)));
        }
        if (items.isEmpty()) {
            inventory.setItem(22, this.infoItem(Material.BARRIER, "&cEmpty", List.of("&7This category has not been", "&7configured yet.")));
        }
        player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.0f);
        player.openInventory(inventory);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BaseHolder holder)) {
            return;
        }
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getView().getTopInventory())) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!this.featureToggleService.checkEnabled(player, FeatureToggleService.Feature.SHOP)) {
            return;
        }
        int slot = event.getRawSlot();
        if (holder instanceof RootHolder) {
            this.handleRootClick(player, slot);
            return;
        }
        if (holder instanceof CategoryHolder categoryHolder) {
            this.handleCategoryClick(player, categoryHolder.category, slot, event.getClick());
        }
    }

    private void handleRootClick(Player player, int slot) {
        for (int i = 0; i < CATEGORY_SLOTS.length && i < Category.values().length; ++i) {
            if (CATEGORY_SLOTS[i] != slot) continue;
            this.openCategory(player, Category.values()[i]);
            return;
        }
    }

    private void handleCategoryClick(Player player, Category category, int slot, ClickType click) {
        if (slot == this.backSlot(category)) {
            this.open(player);
            return;
        }
        List<ShopItem> items = this.itemsByCategory.getOrDefault(category, List.of());
        int[] itemSlots = this.itemSlots(category);
        for (int i = 0; i < items.size() && i < itemSlots.length; ++i) {
            if (itemSlots[i] != slot) continue;
            this.handleShopItemClick(player, items.get(i), click);
            return;
        }
    }

    private void handleShopItemClick(Player player, ShopItem shopItem, ClickType click) {
        if (this.purchasePrice(shopItem) > 0.0 && click.isLeftClick()) {
            this.handlePurchase(player, shopItem);
            return;
        }
        if (click.isLeftClick()) {
            player.sendMessage(Text.color("&cThis item can not be purchased."));
            return;
        }
        if (!click.isRightClick()) {
            return;
        }
        if (!shopItem.sellable) {
            player.sendMessage(Text.color("&cThis item can not be sold."));
            return;
        }
        double worth = this.economyService.getWorth(shopItem.material);
        if (worth <= 0.0) {
            player.sendMessage(Text.color("&cThis item can not be sold right now."));
            return;
        }
        int amount = click.isShiftClick() ? this.countItems(player, shopItem.material) : this.countItems(player, shopItem.material) > 0 ? 1 : 0;
        if (amount <= 0) {
            player.sendMessage(Text.color("&cYou do not have any " + this.prettyName(shopItem.material) + "&c to sell."));
            return;
        }
        int removed = this.removeItems(player, shopItem.material, amount);
        if (removed <= 0) {
            player.sendMessage(Text.color("&cYou do not have any " + this.prettyName(shopItem.material) + "&c to sell."));
            return;
        }
        double total = worth * (double)removed;
        this.economyService.deposit(player, total);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        player.sendMessage(Text.color("&aSold " + removed + "x " + this.prettyName(shopItem.material) + " &afor &2" + this.economyService.format(total)));
    }

    private void handlePurchase(Player player, ShopItem shopItem) {
        if (shopItem.pickaxeTypeId == null && shopItem.doorType == null && shopItem.gearMaterial == null && shopItem.guardTier == null && shopItem.generatorTier == null && shopItem.refinedTier == null && shopItem.satchelMaterial == null) {
            player.sendMessage(Text.color("&cThis item can not be purchased."));
            return;
        }
        double price = this.purchasePrice(shopItem);
        if (price <= 0.0) {
            player.sendMessage(Text.color("&cThis item can not be purchased."));
            return;
        }
        if (this.economyService.getBalance(player) < price) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            player.sendMessage(Text.color("&cYou need &f" + this.economyService.format(price) + " &cto buy this item."));
            return;
        }
        if (!this.economyService.withdraw(player, price)) {
            player.sendMessage(Text.color("&cPurchase failed."));
            return;
        }
        if (shopItem.pickaxeTypeId != null) {
            PickaxeType type = this.pickaxeManager.getType(shopItem.pickaxeTypeId);
            if (type == null) {
                this.economyService.deposit(player, price);
                player.sendMessage(Text.color("&cThat pickaxe is not configured."));
                return;
            }
            player.getInventory().addItem(this.pickaxeManager.createPickaxe(type, 0));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            player.sendMessage(Text.color("&aPurchased " + Text.strip(type.getDisplayName()) + " &afor &2" + this.economyService.format(price)));
            return;
        }
        if (shopItem.doorType != null) {
            player.getInventory().addItem(this.cellService.createDoorItem(shopItem.doorType, 1));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            player.sendMessage(Text.color("&aPurchased " + shopItem.doorType.name() + " Cell Door &afor &2" + this.economyService.format(price)));
            return;
        }
        if (shopItem.guardTier != null) {
            player.getInventory().addItem(this.cellGuardService.createGuardAnchor(shopItem.guardTier, 1));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            player.sendMessage(Text.color("&aPurchased " + shopItem.guardTier.name() + " Cell Guard &afor &2" + this.economyService.format(price)));
            return;
        }
        if (shopItem.generatorTier != null) {
            player.getInventory().addItem(this.generatorService.createGeneratorItem(shopItem.generatorTier));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            player.sendMessage(Text.color("&aPurchased " + shopItem.generatorTier.name() + " Generator &afor &2" + this.economyService.format(price)));
            return;
        }
        if (shopItem.refinedTier != null) {
            player.getInventory().addItem(this.generatorService.createRefinedItem(shopItem.refinedTier));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            player.sendMessage(Text.color("&aPurchased " + shopItem.refinedTier.name() + " Refined Ore &afor &2" + this.economyService.format(price)));
            return;
        }
        if (shopItem.satchelMaterial != null) {
            player.getInventory().addItem(this.satchelManager.createSatchel(shopItem.satchelMaterial));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            player.sendMessage(Text.color("&aPurchased " + this.prettyName(shopItem.satchelMaterial) + " Satchel &afor &2" + this.economyService.format(price)));
            return;
        }
        if (shopItem.gearMaterial != null) {
            player.getInventory().addItem(this.gearManager.wrap(new ItemStack(shopItem.gearMaterial), shopItem.gearMaterial.name().toLowerCase()));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            player.sendMessage(Text.color("&aPurchased " + this.prettyName(shopItem.gearMaterial) + " &afor &2" + this.economyService.format(price)));
        }
    }

    private int countItems(Player player, Material material) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null || stack.getType() != material) continue;
            total += stack.getAmount();
        }
        return total;
    }

    private int removeItems(Player player, Material material, int amount) {
        int remaining = amount;
        for (int slot = 0; slot < player.getInventory().getSize() && remaining > 0; ++slot) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack == null || stack.getType() != material) continue;
            int remove = Math.min(remaining, stack.getAmount());
            remaining -= remove;
            if (remove == stack.getAmount()) {
                player.getInventory().setItem(slot, null);
            } else {
                stack.setAmount(stack.getAmount() - remove);
                player.getInventory().setItem(slot, stack);
            }
        }
        return amount - remaining;
    }

    private void loadItems() {
        this.itemsByCategory.clear();
        this.itemsByCategory.put(Category.ORES, List.of(
                ShopItem.sellable(Material.COAL_ORE),
                ShopItem.sellable(Material.COAL),
                ShopItem.sellable(Material.IRON_ORE),
                ShopItem.sellable(Material.IRON_INGOT),
                ShopItem.sellable(Material.LAPIS_ORE),
                ShopItem.sellable(Material.LAPIS_LAZULI),
                ShopItem.sellable(Material.GOLD_ORE),
                ShopItem.sellable(Material.GOLD_INGOT),
                ShopItem.sellable(Material.REDSTONE_ORE),
                ShopItem.sellable(Material.REDSTONE),
                ShopItem.sellable(Material.DIAMOND_ORE),
                ShopItem.sellable(Material.DIAMOND),
                ShopItem.sellable(Material.EMERALD_ORE),
                ShopItem.sellable(Material.EMERALD)
        ));
        this.itemsByCategory.put(Category.PICKAXES, List.of(
                ShopItem.pickaxe("wooden", 300.0, this.pickaxeManager.getType("wooden")),
                ShopItem.pickaxe("stone", 1000.0, this.pickaxeManager.getType("stone")),
                ShopItem.pickaxe("iron", 3000.0, this.pickaxeManager.getType("iron")),
                ShopItem.pickaxe("golden", 5000.0, this.pickaxeManager.getType("golden")),
                ShopItem.pickaxe("diamond", 10000.0, this.pickaxeManager.getType("diamond")),
                ShopItem.satchel(Material.COAL_ORE),
                ShopItem.satchel(Material.IRON_ORE),
                ShopItem.satchel(Material.LAPIS_ORE),
                ShopItem.satchel(Material.REDSTONE_ORE),
                ShopItem.satchel(Material.DIAMOND_ORE),
                ShopItem.satchel(Material.EMERALD_ORE)
        ));
        this.itemsByCategory.put(Category.GEAR_WEAPONS, List.of(
                ShopItem.gear(Material.CHAINMAIL_HELMET, 500.0),
                ShopItem.gear(Material.CHAINMAIL_CHESTPLATE, 500.0),
                ShopItem.gear(Material.CHAINMAIL_LEGGINGS, 500.0),
                ShopItem.gear(Material.CHAINMAIL_BOOTS, 500.0),
                ShopItem.gear(Material.STONE_SWORD, 500.0),
                ShopItem.gear(Material.STONE_AXE, 500.0),
                ShopItem.gear(Material.GOLDEN_HELMET, 1000.0),
                ShopItem.gear(Material.GOLDEN_CHESTPLATE, 1000.0),
                ShopItem.gear(Material.GOLDEN_LEGGINGS, 1000.0),
                ShopItem.gear(Material.GOLDEN_BOOTS, 1000.0),
                ShopItem.gear(Material.GOLDEN_SWORD, 1000.0),
                ShopItem.gear(Material.GOLDEN_AXE, 1000.0),
                ShopItem.gear(Material.IRON_HELMET, 2000.0),
                ShopItem.gear(Material.IRON_CHESTPLATE, 2000.0),
                ShopItem.gear(Material.IRON_LEGGINGS, 2000.0),
                ShopItem.gear(Material.IRON_BOOTS, 2000.0),
                ShopItem.gear(Material.IRON_SWORD, 2000.0),
                ShopItem.gear(Material.IRON_AXE, 2000.0),
                ShopItem.gear(Material.DIAMOND_HELMET, 5000.0),
                ShopItem.gear(Material.DIAMOND_CHESTPLATE, 5000.0),
                ShopItem.gear(Material.DIAMOND_LEGGINGS, 5000.0),
                ShopItem.gear(Material.DIAMOND_BOOTS, 5000.0),
                ShopItem.gear(Material.DIAMOND_SWORD, 5000.0),
                ShopItem.gear(Material.DIAMOND_AXE, 5000.0)
        ));
        this.itemsByCategory.put(Category.DOORS, List.of(
                ShopItem.door(CellDoorType.BASIC, 2000.0),
                ShopItem.door(CellDoorType.ELITE, 8000.0),
                ShopItem.door(CellDoorType.LEGENDARY, 10000.0),
                ShopItem.guard(CellGuardTier.BASIC, 3000.0),
                ShopItem.guard(CellGuardTier.UNIQUE, 4500.0),
                ShopItem.guard(CellGuardTier.RARE, 6000.0),
                ShopItem.guard(CellGuardTier.ELITE, 9000.0),
                ShopItem.guard(CellGuardTier.LEGENDARY, 12000.0)
        ));
        this.itemsByCategory.put(Category.GENERATORS, List.of(
                ShopItem.generator(GeneratorService.GeneratorTier.BASIC, 7500.0),
                ShopItem.generator(GeneratorService.GeneratorTier.UNCOMMON, 12500.0),
                ShopItem.generator(GeneratorService.GeneratorTier.UNIQUE, 20000.0),
                ShopItem.generator(GeneratorService.GeneratorTier.RARE, 30000.0),
                ShopItem.generator(GeneratorService.GeneratorTier.ELITE, 42500.0),
                ShopItem.generator(GeneratorService.GeneratorTier.LEGENDARY, 60000.0),
                ShopItem.generator(GeneratorService.GeneratorTier.MYTHICAL, 87500.0),
                ShopItem.refined(GeneratorService.RefinedTier.BASIC, 4000.0),
                ShopItem.refined(GeneratorService.RefinedTier.UNCOMMON, 6000.0),
                ShopItem.refined(GeneratorService.RefinedTier.UNIQUE, 9000.0),
                ShopItem.refined(GeneratorService.RefinedTier.RARE, 12500.0),
                ShopItem.refined(GeneratorService.RefinedTier.ELITE, 17500.0),
                ShopItem.refined(GeneratorService.RefinedTier.LEGENDARY, 25000.0),
                ShopItem.refined(GeneratorService.RefinedTier.MYTHICAL, 37500.0)
        ));
    }

    private ItemStack categoryItem(Category category) {
        String plainName = Text.strip(category.displayName);
        return this.infoItem(category.icon, category.color + category.displayName, List.of("&7Click to view the &7" + plainName + " items"));
    }

    private ItemStack shopItem(ShopItem shopItem) {
        ItemStack item = shopItem.displayItem(this.pickaxeManager, this.cellService, this.cellGuardService, this.generatorService, this.gearManager, this.satchelManager);
        if (shopItem.pickaxeTypeId != null || shopItem.doorType != null || shopItem.gearMaterial != null || shopItem.guardTier != null || shopItem.generatorTier != null || shopItem.refinedTier != null || shopItem.satchelMaterial != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
                lore.add(Text.componentNoItalics(""));
                lore.add(Text.componentNoItalics("&eLeft-Click &7to purchase"));
                lore.add(Text.componentNoItalics("&7Buy Price: &a" + this.economyService.format(this.purchasePrice(shopItem))));
                meta.lore(lore);
                item.setItemMeta(meta);
            }
            return item;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Text.componentNoItalics("&f" + this.prettyName(shopItem.material)));
            List<Component> lore = new ArrayList<>();
            double worth = this.economyService.getWorth(shopItem.material);
            lore.add(Text.componentNoItalics("&7Worth: " + (worth > 0.0 ? "&a" + this.economyService.format(worth) : "&cNot set")));
            lore.add(Text.componentNoItalics(""));
            lore.add(Text.componentNoItalics("&eRight-Click &7to sell &f1x"));
            lore.add(Text.componentNoItalics("&eShift Right-Click &7to sell &fall"));
            lore.add(Text.componentNoItalics("&cLeft-Click &7purchase disabled"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private double purchasePrice(ShopItem shopItem) {
        if (shopItem.satchelMaterial == null) {
            return shopItem.purchasePrice;
        }
        double worth = this.economyService.getWorth(shopItem.satchelMaterial);
        int capacity = this.satchelManager.getConfiguredCapacity(shopItem.satchelMaterial);
        if (worth <= 0.0 || capacity <= 0) {
            return 0.0;
        }
        return worth * (double)capacity * 50.0;
    }

    private void fillBackground(Inventory inventory) {
        ItemStack pane = this.namedItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int slot = 0; slot < inventory.getSize(); ++slot) {
            inventory.setItem(slot, pane);
        }
    }

    private int inventorySize(Category category) {
        if (category == Category.GEAR_WEAPONS) {
            return GEAR_CATEGORY_INVENTORY_SIZE;
        }
        if (category == Category.GENERATORS) {
            return GENERATOR_CATEGORY_INVENTORY_SIZE;
        }
        return CATEGORY_INVENTORY_SIZE;
    }

    private int[] itemSlots(Category category) {
        if (category == Category.GEAR_WEAPONS) {
            return GEAR_ITEM_SLOTS;
        }
        if (category == Category.GENERATORS) {
            return GENERATOR_ITEM_SLOTS;
        }
        return DEFAULT_ITEM_SLOTS;
    }

    private int backSlot(Category category) {
        return category == Category.GEAR_WEAPONS || category == Category.GENERATORS ? 40 : 31;
    }

    private ItemStack infoItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Text.componentNoItalics(name));
            List<Component> lines = new ArrayList<>();
            for (String line : lore) {
                lines.add(Text.componentNoItalics(line));
            }
            meta.lore(lines);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack namedItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Text.componentNoItalics(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    private String prettyName(Material material) {
        String[] words = material.name().toLowerCase().split("_");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return builder.toString();
    }

    private enum Category {
        ORES("&6&lOres", Material.COAL_ORE, ""),
        PICKAXES("&6&lPickaxes", Material.DIAMOND_PICKAXE, ""),
        GEAR_WEAPONS("&6&lGear", Material.DIAMOND_SWORD, ""),
        DOORS("&6&lCell Protection", Material.IRON_DOOR, ""),
        GENERATORS("&6&lGenerators", Material.SPAWNER, "");

        private final String displayName;
        private final Material icon;
        private final String color;

        Category(String displayName, Material icon, String color) {
            this.displayName = displayName;
            this.icon = icon;
            this.color = color;
        }
    }

    private record ShopItem(Material material, boolean sellable, double purchasePrice, String pickaxeTypeId, CellDoorType doorType, Material gearMaterial, CellGuardTier guardTier, GeneratorService.GeneratorTier generatorTier, GeneratorService.RefinedTier refinedTier, Material satchelMaterial) {
        private static ShopItem sellable(Material material) {
            return new ShopItem(material, true, 0.0, null, null, null, null, null, null, null);
        }

        private static ShopItem pickaxe(String typeId, double purchasePrice, PickaxeType type) {
            Material material = type != null ? type.getMaterial() : Material.WOODEN_PICKAXE;
            return new ShopItem(material, false, purchasePrice, typeId, null, null, null, null, null, null);
        }

        private static ShopItem satchel(Material material) {
            return new ShopItem(material, false, 0.0, null, null, null, null, null, null, material);
        }

        private static ShopItem door(CellDoorType doorType, double purchasePrice) {
            return new ShopItem(doorType.getMaterial(), false, purchasePrice, null, doorType, null, null, null, null, null);
        }

        private static ShopItem gear(Material material, double purchasePrice) {
            return new ShopItem(material, false, purchasePrice, null, null, material, null, null, null, null);
        }

        private static ShopItem guard(CellGuardTier tier, double purchasePrice) {
            return new ShopItem(Material.ARMOR_STAND, false, purchasePrice, null, null, null, tier, null, null, null);
        }

        private static ShopItem generator(GeneratorService.GeneratorTier tier, double purchasePrice) {
            return new ShopItem(tier.generatorBlock, false, purchasePrice, null, null, null, null, tier, null, null);
        }

        private static ShopItem refined(GeneratorService.RefinedTier tier, double purchasePrice) {
            return new ShopItem(tier.refinedBlock, false, purchasePrice, null, null, null, null, null, tier, null);
        }

        private ItemStack displayItem(PickaxeManager pickaxeManager, CellService cellService, CellGuardService cellGuardService, GeneratorService generatorService, GearManager gearManager, SatchelManager satchelManager) {
            if (this.pickaxeTypeId == null && this.doorType == null && this.gearMaterial == null && this.guardTier == null && this.generatorTier == null && this.refinedTier == null && this.satchelMaterial == null) {
                return new ItemStack(this.material);
            }
            if (this.pickaxeTypeId != null) {
                PickaxeType type = pickaxeManager.getType(this.pickaxeTypeId);
                if (type == null) {
                    return new ItemStack(this.material);
                }
                return pickaxeManager.createPickaxe(type, 0);
            }
            if (this.doorType != null) {
                return cellService.createDoorItem(this.doorType, 1);
            }
            if (this.guardTier != null) {
                return cellGuardService.createGuardAnchor(this.guardTier, 1);
            }
            if (this.generatorTier != null) {
                return generatorService.createGeneratorItem(this.generatorTier);
            }
            if (this.refinedTier != null) {
                return generatorService.createRefinedItem(this.refinedTier);
            }
            if (this.satchelMaterial != null) {
                return satchelManager.createSatchel(this.satchelMaterial);
            }
            return this.gearMaterial != null ? gearManager.wrap(new ItemStack(this.gearMaterial), this.gearMaterial.name().toLowerCase()) : new ItemStack(this.material);
        }
    }

    private abstract static class BaseHolder implements InventoryHolder {
        Inventory inventory;

        @Override
        public Inventory getInventory() {
            return this.inventory;
        }
    }

    private static final class RootHolder extends BaseHolder {
    }

    private static final class CategoryHolder extends BaseHolder {
        private final Category category;

        private CategoryHolder(Category category) {
            this.category = category;
        }
    }
}
