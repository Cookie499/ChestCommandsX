/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.placeholder;

import me.filoghost.chestcommands.api.PlaceholderReplacer;
import me.filoghost.chestcommands.hook.PlaceholderAPIHook;
import me.filoghost.chestcommands.placeholder.scanner.PlaceholderMatch;
import me.filoghost.chestcommands.placeholder.scanner.PlaceholderScanner;
import me.filoghost.fcommons.MaterialsHelper;
import me.filoghost.fcommons.Preconditions;
import me.filoghost.fcommons.logging.Log;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PlaceholderManager {

    private static final List<StaticPlaceholder> staticPlaceholders = new ArrayList<>();
    private static final PlaceholderRegistry dynamicPlaceholderRegistry = new PlaceholderRegistry();
    private static final PlaceholderCache placeholderCache = new PlaceholderCache();
    private static final ThreadLocal<ItemStack> dragItemContext = new ThreadLocal<>();
    static {
        for (DefaultPlaceholder placeholder : DefaultPlaceholder.values()) {
            dynamicPlaceholderRegistry.registerInternalPlaceholder(placeholder.getIdentifier(), placeholder.getReplacer());
        }
    }

    public static boolean hasDynamicPlaceholders(List<String> list) {
        for (String element : list) {
            if (hasDynamicPlaceholders(element)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasDynamicPlaceholders(String text) {
        if (IntegerPlaceholderManager.hasPlaceholders(text)) {
            return true;
        }

        if (hasDragItemPlaceholder(text)) {
            return true;
        }

        if (new PlaceholderScanner(text).containsAny()) {
            return true;
        }

        if (PlaceholderAPIHook.INSTANCE.isEnabled() && PlaceholderAPIHook.hasPlaceholders(text)) {
            return true;
        }

        return false;
    }

    public static String replaceDynamicPlaceholders(String text, Player player) {
        return replaceDynamicPlaceholders(text, player, null);
    }

    public static String replaceDynamicPlaceholders(String text, Player player, Integer integerMaxValue) {
        text = IntegerPlaceholderManager.replacePlaceholders(text, integerMaxValue);
        text = replaceStaticPlaceholders(text);
        text = replaceDragItemPercentPlaceholders(text);
        text = new PlaceholderScanner(text).replace(match -> getReplacement(match, player));

        if (PlaceholderAPIHook.INSTANCE.isEnabled()) {
            text = PlaceholderAPIHook.setPlaceholders(text, player);
        }

        return text;
    }

    private static @Nullable String getReplacement(PlaceholderMatch placeholderMatch, Player player) {
        if ("drag_item".equalsIgnoreCase(placeholderMatch.getIdentifier())) {
            return getDragItemMaterial();
        }
        if ("drag_item_amount".equalsIgnoreCase(placeholderMatch.getIdentifier())) {
            return getDragItemAmount();
        }

        Placeholder placeholder = dynamicPlaceholderRegistry.getPlaceholder(placeholderMatch);

        if (placeholder == null) {
            return null; // Placeholder not found
        }

        return placeholderCache.computeIfAbsent(placeholderMatch, player, () -> {
            try {
                return placeholder.getReplacer().getReplacement(player, placeholderMatch.getArgument());
            } catch (Throwable t) {
                Log.severe("Encountered an exception while replacing the placeholder \"" + placeholderMatch.getIdentifier()
                        + "\" registered by the plugin \"" + placeholder.getPlugin().getName() + "\"", t);
                return "[PLACEHOLDER ERROR]";
            }
        });
    }

    public static void setStaticPlaceholders(List<StaticPlaceholder> staticPlaceholders) {
        PlaceholderManager.staticPlaceholders.clear();
        PlaceholderManager.staticPlaceholders.addAll(staticPlaceholders);
    }

    public static boolean hasStaticPlaceholders(List<String> list) {
        for (String element : list) {
            if (hasStaticPlaceholders(element)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasStaticPlaceholders(String text) {
        for (StaticPlaceholder staticPlaceholder : staticPlaceholders) {
            if (text.contains(staticPlaceholder.getIdentifier())) {
                return true;
            }
        }
        return false;
    }

    public static String replaceStaticPlaceholders(String text) {
        for (StaticPlaceholder staticPlaceholder : staticPlaceholders) {
            text = text.replace(staticPlaceholder.getIdentifier(), staticPlaceholder.getReplacement());
        }
        return text;
    }

    public static String replaceIntegerPlaceholders(String text) {
        return IntegerPlaceholderManager.replacePlaceholders(text);
    }

    public static String replaceIntegerPlaceholders(String text, int maxValue) {
        return IntegerPlaceholderManager.replacePlaceholders(text, maxValue);
    }

    public static void withPrivateIntegerScope(Object scope, Runnable runnable) {
        IntegerPlaceholderManager.withPrivateScope(scope, runnable);
    }

    public static void clearPrivateIntegerScope(Object scope) {
        IntegerPlaceholderManager.clearPrivateScope(scope);
    }

    public static void registerPluginPlaceholder(Plugin plugin, String identifier, PlaceholderReplacer placeholderReplacer) {
        Preconditions.notNull(plugin, "plugin");
        checkIdentifierArgument(identifier);
        Preconditions.notNull(placeholderReplacer, "placeholderReplacer");

        dynamicPlaceholderRegistry.registerExternalPlaceholder(plugin, identifier, placeholderReplacer);
    }

    public static boolean unregisterPluginPlaceholder(Plugin plugin, String identifier) {
        Preconditions.notNull(plugin, "plugin");
        checkIdentifierArgument(identifier);

        return dynamicPlaceholderRegistry.unregisterExternalPlaceholder(plugin, identifier);
    }

    private static void checkIdentifierArgument(String identifier) {
        Preconditions.notNull(identifier, "identifier");
        Preconditions.checkArgument(1 <= identifier.length() && identifier.length() <= 30, "identifier length must be between 1 and 30");
        Preconditions.checkArgument(identifier.matches("[a-zA-Z0-9_]+"), "identifier must contain only letters, numbers and underscores");
    }

    public static void onTick() {
        placeholderCache.onTick();
    }

    public static void withDragItemContext(@Nullable ItemStack itemStack, Runnable runnable) {
        ItemStack previousItemStack = dragItemContext.get();
        dragItemContext.set(itemStack != null ? itemStack.clone() : null);
        try {
            runnable.run();
        } finally {
            if (previousItemStack != null) {
                dragItemContext.set(previousItemStack);
            } else {
                dragItemContext.remove();
            }
        }
    }

    private static boolean hasDragItemPlaceholder(String text) {
        return text.contains("%drag_item%")
                || text.contains("%drag_item_amount%")
                || text.contains("{drag_item}")
                || text.contains("{drag_item_amount}");
    }

    private static String replaceDragItemPercentPlaceholders(String text) {
        return text
                .replace("%drag_item%", getDragItemMaterial())
                .replace("%drag_item_amount%", getDragItemAmount());
    }

    private static String getDragItemMaterial() {
        ItemStack itemStack = dragItemContext.get();
        if (itemStack == null || MaterialsHelper.isAir(itemStack.getType())) {
            return "";
        }

        return itemStack.getType().name();
    }

    private static String getDragItemAmount() {
        ItemStack itemStack = dragItemContext.get();
        if (itemStack == null || MaterialsHelper.isAir(itemStack.getType())) {
            return "0";
        }

        return String.valueOf(itemStack.getAmount());
    }

}
