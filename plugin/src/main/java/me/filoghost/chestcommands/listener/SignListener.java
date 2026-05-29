/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.listener;

import me.filoghost.chestcommands.Permissions;
import me.filoghost.chestcommands.config.Lang;
import me.filoghost.chestcommands.menu.InternalMenu;
import me.filoghost.chestcommands.menu.MenuManager;
import me.filoghost.chestcommands.util.FoliaScheduler;
import me.filoghost.chestcommands.util.Text;
import me.filoghost.chestcommands.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignListener implements Listener {
    
    private static final int HEADER_LINE = 0;
    private static final int FILENAME_LINE = 1;
    
    private static final String SIGN_CREATION_TRIGGER = "[menu]";
    
    private static final ChatColor VALID_SIGN_COLOR = ChatColor.DARK_BLUE;
    private static final String VALID_SIGN_HEADER = VALID_SIGN_COLOR + SIGN_CREATION_TRIGGER;


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        BlockState clickedBlockState = event.getClickedBlock().getState();
        
        if (!(clickedBlockState instanceof Sign)) {
            return;
        }
        
        Sign sign = (Sign) clickedBlockState;
        
        if (!sign.getLine(HEADER_LINE).equalsIgnoreCase(VALID_SIGN_HEADER)) {
            return;
        }

        String menuFileName = Utils.addYamlExtension(sign.getLine(FILENAME_LINE).trim());
        InternalMenu menu = MenuManager.getMenuByFileName(menuFileName);
        
        if (menu == null) {
            Text.send(event.getPlayer(), Lang.get().menu_not_found);
            return;
        }
        
        FoliaScheduler.runAtPlayer(event.getPlayer(), () -> menu.openCheckingPermission(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreateMenuSign(SignChangeEvent event) {
        Player player = event.getPlayer();

        if (isCreatingMenuSign(event.getLine(HEADER_LINE)) && canCreateMenuSign(player)) {
            String menuFileName = event.getLine(FILENAME_LINE).trim();
            
            if (menuFileName.isEmpty()) {
                event.setCancelled(true);
                Text.send(player, "&cYou must write a menu name in the second line.");
                return;
            }
            
            menuFileName = Utils.addYamlExtension(menuFileName);
    
            InternalMenu menu = MenuManager.getMenuByFileName(menuFileName);
            if (menu == null) {
                event.setCancelled(true);
                Text.send(player, "&cMenu \"" + menuFileName + "\" was not found.");
                return;
            }
    
            event.setLine(HEADER_LINE, VALID_SIGN_COLOR + event.getLine(HEADER_LINE));
            Text.send(player, "&aSuccessfully created a sign for the menu " + menuFileName + ".");
        }
    }

    

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChangeMonitor(SignChangeEvent event) {
        // Prevent players without permissions from creating menu signs
        if (isValidMenuSign(event.getLine(HEADER_LINE)) && !canCreateMenuSign(event.getPlayer())) {
            event.setLine(HEADER_LINE, ChatColor.stripColor(event.getLine(HEADER_LINE)));
        }
    }
    
    private boolean isCreatingMenuSign(String headerLine) {
        return headerLine.equalsIgnoreCase(SIGN_CREATION_TRIGGER);
    }
    
    private boolean isValidMenuSign(String headerLine) {
        return headerLine.equalsIgnoreCase(VALID_SIGN_HEADER);
    }
    
    private boolean canCreateMenuSign(Player player) {
        return player.hasPermission(Permissions.SIGN_CREATE);
    }

}
