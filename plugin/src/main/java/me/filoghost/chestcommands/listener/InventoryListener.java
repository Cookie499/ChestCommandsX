/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.listener;

import me.filoghost.chestcommands.ChestCommands;
import me.filoghost.chestcommands.api.Icon;
import me.filoghost.chestcommands.api.Menu;
import me.filoghost.chestcommands.config.Settings;
import me.filoghost.chestcommands.icon.InternalConfigurableIcon;
import me.filoghost.chestcommands.inventory.DefaultMenuView;
import me.filoghost.chestcommands.logging.Errors;
import me.filoghost.chestcommands.menu.InternalMenu;
import me.filoghost.chestcommands.menu.MenuManager;
import me.filoghost.chestcommands.placeholder.PlaceholderManager;
import me.filoghost.chestcommands.util.FoliaScheduler;
import me.filoghost.chestcommands.util.Text;
import me.filoghost.fcommons.logging.Log;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class InventoryListener implements Listener {

    private final Map<Player, Long> antiClickSpam = new WeakHashMap<>();


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        if (event.hasItem() && event.getAction() != Action.PHYSICAL) {
            MenuManager.openMenuByItem(event.getPlayer(), event.getItem(), event.getAction());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onEarlyInventoryClick(InventoryClickEvent event) {
        DefaultMenuView menuView = MenuManager.getOpenMenuView(event.getInventory());
        if (menuView != null && shouldCancelInventoryClick(event, menuView)) {
            // Cancel the event as early as possible
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onEarlyInventoryDrag(InventoryDragEvent event) {
        DefaultMenuView menuView = MenuManager.getOpenMenuView(event.getInventory());
        if (menuView != null && shouldCancelInventoryDrag(event.getRawSlots(), menuView, event.getInventory().getSize())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onLateInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        DefaultMenuView menuView = MenuManager.getOpenMenuView(inventory);
        if (menuView == null) {
            return;
        }

        boolean cancelClick = shouldCancelInventoryClick(event, menuView);
        if (cancelClick) {
            // Cancel the event again just in case a plugin un-cancels it
            event.setCancelled(true);
        }

        int slot = event.getRawSlot();
        Player clicker = (Player) event.getWhoClicked();
        Icon icon = menuView.getIcon(slot);
        if (icon == null) {
            return;
        }

        boolean draggableIcon = icon instanceof InternalConfigurableIcon && ((InternalConfigurableIcon) icon).isDraggable();

        Long cooldownUntil = antiClickSpam.get(clicker);
        long now = System.currentTimeMillis();
        int minDelay = Settings.get().anti_click_spam_delay;

        if (minDelay > 0) {
            if (cooldownUntil != null && cooldownUntil > now) {
                return;
            } else {
                antiClickSpam.put(clicker, now + minDelay);
            }
        }

        // Only handle the click AFTER the event has finished
        FoliaScheduler.runAtPlayer(clicker, () -> {
            try {
                if (draggableIcon) {
                    ItemStack dragItem = menuView.getItem(slot);
                    PlaceholderManager.withDragItemContext(dragItem, () -> {
                        ((InternalConfigurableIcon) icon).onClick(menuView, clicker, false);
                    });
                } else {
                    icon.onClick(menuView, clicker);
                }
            } catch (Throwable t) {
                handleIconClickException(clicker, menuView.getMenu(), t);
                menuView.close();
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onLateInventoryDrag(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        DefaultMenuView menuView = MenuManager.getOpenMenuView(inventory);
        if (menuView == null) {
            return;
        }

        boolean cancelDrag = shouldCancelInventoryDrag(event.getRawSlots(), menuView, inventory.getSize());
        if (cancelDrag) {
            event.setCancelled(true);
            return;
        }

        Player clicker = (Player) event.getWhoClicked();
        FoliaScheduler.runAtPlayer(clicker, () -> {
            for (int slot : event.getRawSlots()) {
                if (slot < 0 || slot >= inventory.getSize()) {
                    continue;
                }

                Icon icon = menuView.getIcon(slot);
                if (!(icon instanceof InternalConfigurableIcon) || !((InternalConfigurableIcon) icon).isDraggable()) {
                    continue;
                }

                try {
                    ItemStack dragItem = menuView.getItem(slot);
                    PlaceholderManager.withDragItemContext(dragItem, () -> {
                        ((InternalConfigurableIcon) icon).onClick(menuView, clicker, false);
                    });
                } catch (Throwable t) {
                    handleIconClickException(clicker, menuView.getMenu(), t);
                    menuView.close();
                    return;
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onInventoryClose(InventoryCloseEvent event) {
        DefaultMenuView menuView = MenuManager.getOpenMenuView(event.getInventory());
        if (menuView != null) {
            menuView.clearPrivateState();
        }
    }

    private boolean shouldCancelInventoryClick(InventoryClickEvent event, DefaultMenuView menuView) {
        int slot = event.getRawSlot();
        int menuSize = event.getInventory().getSize();

        if (slot < 0) {
            return true;
        }

        if (slot >= menuSize) {
            return !menuView.hasDraggableIcons() || event.isShiftClick();
        }

        Icon icon = menuView.getIcon(slot);
        return !(icon instanceof InternalConfigurableIcon && ((InternalConfigurableIcon) icon).isDraggable());
    }

    private boolean shouldCancelInventoryDrag(Set<Integer> rawSlots, DefaultMenuView menuView, int menuSize) {
        for (int slot : rawSlots) {
            if (slot < 0) {
                return true;
            }

            if (slot >= menuSize) {
                continue;
            }

            Icon icon = menuView.getIcon(slot);
            if (!(icon instanceof InternalConfigurableIcon) || !((InternalConfigurableIcon) icon).isDraggable()) {
                return true;
            }
        }

        return false;
    }

    private void handleIconClickException(Player clicker, Menu menu, Throwable throwable) {
        String menuDescription;
        if (menu.getPlugin() == ChestCommands.getInstance()) {
            menuDescription = "the menu \"" + Errors.formatPath(((InternalMenu) menu).getSourceFile()) + "\"";
        } else {
            menuDescription = "a menu created by the plugin \"" + menu.getPlugin().getName() + "\"";
        }

        Log.severe("Encountered an exception while handling a click inside " + menuDescription, throwable);
        Text.send(clicker, "<red>An internal error occurred when you clicked on the item.");
    }

}
