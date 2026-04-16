package org.axial.prisonsCore.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.model.CustomEnchant;
import org.axial.prisonsCore.model.EnchantRarity;
import org.axial.prisonsCore.model.PickaxeType;
import org.axial.prisonsCore.service.FeatureToggleService;
import org.axial.prisonsCore.service.GeneratorService;
import org.axial.prisonsCore.service.ReputationService;
import org.axial.prisonsCore.service.SafezoneService;
import org.axial.prisonsCore.service.ShardService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class GlobalCommandTabCompleter implements TabCompleter {
    private final PrisonsCore plugin;
    private final TabCompleter delegate;

    public GlobalCommandTabCompleter(PrisonsCore plugin, TabCompleter delegate) {
        this.plugin = plugin;
        this.delegate = delegate;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (this.delegate != null) {
            List<String> delegated = this.delegate.onTabComplete(sender, command, alias, args);
            if (delegated != null && !delegated.isEmpty()) {
                return delegated;
            }
        }
        return this.fallback(command.getName().toLowerCase(Locale.ROOT), args);
    }

    private List<String> fallback(String command, String[] args) {
        if (args.length == 0) {
            return List.of();
        }
        return switch (command) {
            case "extract" -> args.length == 1 ? this.filter(List.of("all"), args[0]) : List.of();
            case "setplayerlevel", "tpa", "pay" -> args.length == 1 ? this.onlinePlayers(args[0]) : List.of();
            case "miningtotalexp" -> args.length == 1
                    ? this.filter(List.of("set", "add", "remove", "reset"), args[0])
                    : args.length == 2 ? this.onlinePlayers(args[1]) : List.of();
            case "enchant" -> args.length == 1
                    ? this.filter(List.of("give"), args[0])
                    : args.length == 2 ? this.filter(this.customEnchantIds(), args[1])
                    : List.of();
            case "wormhole" -> args.length == 1 ? this.filter(List.of("on", "info", "setcenter", "setradius", "setoptioncount", "setheight", "reload"), args[0]) : List.of();
            case "chargeorb" -> args.length == 1
                    ? this.filter(List.of("give", "slot"), args[0])
                    : args.length == 3 ? this.onlinePlayers(args[2]) : List.of();
            case "dust" -> args.length == 1
                    ? this.filter(List.of("give"), args[0])
                    : args.length == 2 ? this.filter(this.enchantRarities(), args[1])
                    : args.length == 4 ? this.onlinePlayers(args[3]) : List.of();
            case "gear" -> args.length == 1 ? this.filter(this.applicableMaterials(), args[0]) : List.of();
            case "prestigetoken" -> args.length == 1
                    ? this.filter(List.of("give", "apply"), args[0])
                    : args.length == 2 && "give".equalsIgnoreCase(args[0]) ? this.onlinePlayers(args[1]) : List.of();
            case "applyore" -> List.of();
            case "setworth" -> args.length == 1 ? this.filter(this.materialNamesWithHand(), args[0]) : List.of();
            case "eco" -> args.length == 1
                    ? this.filter(List.of("add", "set", "remove", "reset"), args[0])
                    : args.length == 2 ? this.onlinePlayers(args[1]) : List.of();
            case "setwarp" -> args.length == 1 ? this.filter(this.defaultWarpNames(), args[0]) : List.of();
            case "warp" -> args.length == 1 ? this.filter(this.plugin.getTeleportService().getWarpNames(), args[0]) : List.of();
            case "tutorial" -> args.length == 1 ? this.filter(List.of("skip"), args[0]) : args.length == 2 ? this.filter(List.of("confirm"), args[1]) : List.of();
            case "kit" -> List.of();
            case "fragment", "shard", "fragments" -> args.length == 1
                    ? this.filter(List.of("give", "addreward"), args[0])
                    : args.length == 2 ? this.filter(this.fragmentTiers(), args[1])
                    : args.length == 3 && "give".equalsIgnoreCase(args[0]) ? this.onlinePlayers(args[2]) : List.of();
            case "meteor" -> args.length == 1
                    ? this.filter(List.of("flare", "setworld"), args[0])
                    : args.length == 2 && "flare".equalsIgnoreCase(args[0]) ? this.onlinePlayers(args[1]) : List.of();
            case "withdraw" -> List.of();
            case "spawn", "setspawn", "tutorialsetspawn", "tpaccept", "tpdeny", "tptoggle", "level", "balance", "baltop", "pickaxeprestige", "levels", "shop", "feed", "gmc", "gms", "fly", "jet", "prisonsreload", "enchants", "enchanter", "prestige", "fix" ->
                    "fix".equals(command) && args.length == 1 ? this.filter(List.of("hand", "all"), args[0]) : List.of();
            default -> this.commandSpecificFallback(command, args);
        };
    }

    private List<String> commandSpecificFallback(String command, String[] args) {
        return switch (command) {
            case "contraband" -> args.length == 1
                    ? this.filter(List.of("give"), args[0])
                    : args.length == 2 ? this.filter(this.plugin.getContrabandService().tierIds(), args[1])
                    : args.length == 3 ? this.onlinePlayers(args[2]) : List.of();
            case "xpbooster", "energybooster" -> args.length == 1 ? this.filter(List.of("give"), args[0]) : args.length == 2 ? this.onlinePlayers(args[1]) : List.of();
            case "generator" -> args.length == 1
                    ? this.filter(List.of("give"), args[0])
                    : args.length == 2 ? this.filter(this.generatorTiers(), args[1])
                    : args.length == 3 ? this.onlinePlayers(args[2]) : List.of();
            case "refined" -> args.length == 1
                    ? this.filter(List.of("give"), args[0])
                    : args.length == 2 ? this.filter(this.refinedTiers(), args[1])
                    : args.length == 3 ? this.onlinePlayers(args[2]) : List.of();
            case "customblock" -> args.length == 1 ? this.filter(List.of("give"), args[0]) : List.of();
            case "reputation" -> args.length == 1
                    ? this.filter(List.of("get", "set"), args[0])
                    : args.length == 2 ? this.onlinePlayers(args[1])
                    : args.length == 3 ? this.filter(this.reputationStages(), args[2]) : List.of();
            case "prisonscore" -> args.length == 1
                    ? this.filter(List.of("enable", "disable"), args[0])
                    : args.length == 2 ? this.filter(this.featureNames(), args[1]) : List.of();
            case "gangpoints" -> args.length == 1
                    ? this.filter(List.of("give"), args[0])
                    : args.length == 2 ? this.onlinePlayers(args[1]) : List.of();
            default -> List.of();
        };
    }

    private List<String> filter(Collection<String> values, String partial) {
        String normalized = partial == null ? "" : partial.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (value != null && value.toLowerCase(Locale.ROOT).startsWith(normalized)) {
                result.add(value);
            }
        }
        return result;
    }

    private List<String> onlinePlayers(String partial) {
        return this.filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), partial);
    }

    private List<String> customEnchantIds() {
        return this.plugin.getCustomEnchantService().getAll().stream().map(CustomEnchant::getId).collect(Collectors.toList());
    }

    private List<String> enchantRarities() {
        return java.util.Arrays.stream(EnchantRarity.values()).map(Enum::name).collect(Collectors.toList());
    }

    private List<String> applicableMaterials() {
        List<String> result = new ArrayList<>();
        for (Material material : Material.values()) {
            if (this.plugin.getGearManager().isApplicableMaterial(material)) {
                result.add(material.name());
            }
        }
        return result;
    }

    private List<String> materialNamesWithHand() {
        List<String> result = new ArrayList<>();
        result.add("hand");
        for (Material material : Material.values()) {
            if (material.isItem()) {
                result.add(material.name());
            }
        }
        return result;
    }

    private List<String> defaultWarpNames() {
        return List.of("coal", "iron", "lapis", "gold", "redstone", "diamond", "emerald");
    }

    private List<String> fragmentTiers() {
        return java.util.Arrays.stream(ShardService.Tier.values()).map(Enum::name).collect(Collectors.toList());
    }

    private List<String> generatorTiers() {
        return java.util.Arrays.stream(GeneratorService.GeneratorTier.values()).map(Enum::name).collect(Collectors.toList());
    }

    private List<String> refinedTiers() {
        return java.util.Arrays.stream(GeneratorService.RefinedTier.values()).map(Enum::name).collect(Collectors.toList());
    }

    private List<String> reputationStages() {
        return java.util.Arrays.stream(ReputationService.Stage.values()).map(Enum::name).collect(Collectors.toList());
    }

    private List<String> featureNames() {
        return java.util.Arrays.stream(FeatureToggleService.Feature.values()).map(feature -> feature.name().toLowerCase(Locale.ROOT)).collect(Collectors.toList());
    }
}
