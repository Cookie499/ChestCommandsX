/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.listener;

import me.filoghost.chestcommands.menu.InternalMenu;
import me.filoghost.chestcommands.menu.MenuManager;
import me.filoghost.chestcommands.util.FoliaScheduler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.Nullable;

public class CommandListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String command = getCommandLine(event.getMessage());
        if (command == null) {
            return;
        }

        InternalMenu menu = MenuManager.getMenuByCommandLine(command);
        if (menu == null) {
            return;
        }
        
        event.setCancelled(true);
        FoliaScheduler.runAtPlayer(event.getPlayer(), () -> menu.openCheckingPermission(event.getPlayer()));
    }
    
    private static @Nullable String getCommandLine(String fullCommand) {
        if (!fullCommand.startsWith("/")) {
            return null;
        }

        return fullCommand.substring(1);
    }

}
