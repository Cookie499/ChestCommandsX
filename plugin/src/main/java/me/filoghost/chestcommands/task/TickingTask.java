/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.task;

import me.filoghost.chestcommands.inventory.DefaultMenuView;
import me.filoghost.chestcommands.menu.InternalMenu;
import me.filoghost.chestcommands.menu.MenuManager;
import me.filoghost.chestcommands.placeholder.PlaceholderManager;
import me.filoghost.chestcommands.util.FoliaScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TickingTask implements Runnable {

    private long currentTick;

    @Override
    public void run() {
        updateMenus();
        PlaceholderManager.onTick();

        currentTick++;
    }

    private void updateMenus() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            FoliaScheduler.runAtPlayer(player, () -> refreshMenuFor(player));
        }
    }

    private void refreshMenuFor(Player player) {
        DefaultMenuView menuView = MenuManager.getOpenMenuView(player);

        if (menuView == null || !(menuView.getMenu() instanceof InternalMenu)) {
            return;
        }

        int refreshTicks = ((InternalMenu) menuView.getMenu()).getRefreshTicks();

        if (refreshTicks > 0 && currentTick % refreshTicks == 0) {
            menuView.refresh();
        }
    }

}
