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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
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
    
    private static final NamedTextColor VALID_SIGN_COLOR = NamedTextColor.DARK_BLUE;
    private static final Component VALID_SIGN_HEADER = Text.parseMiniMessage("<" + VALID_SIGN_COLOR + ">" + SIGN_CREATION_TRIGGER);


    private static final PlainTextComponentSerializer PLAIN_TEXT = PlainTextComponentSerializer.plainText();
    private static final String PLAIN_VALID_SIGN_HEADER = PLAIN_TEXT.serialize(VALID_SIGN_HEADER);

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        BlockState clickedBlockState = null;
        if (event.getClickedBlock() != null) {
            clickedBlockState = event.getClickedBlock().getState();
        }

        if (!(clickedBlockState instanceof Sign)) {
            return;
        }

        Sign sign = (Sign) clickedBlockState;

        Side side = isValidMenuSign(sign.getSide(Side.FRONT).line(HEADER_LINE)) ? Side.FRONT : Side.BACK;
        if (!isValidMenuSign(sign.getSide(side).line(HEADER_LINE))) {
            return;
        }

        String menuFileName = Utils.addYamlExtension(PLAIN_TEXT.serialize(sign.getSide(side).line(FILENAME_LINE)).trim());
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

        if (isCreatingMenuSign(PLAIN_TEXT.serialize(event.line(HEADER_LINE))) && canCreateMenuSign(player)) {
            String menuFileName = PLAIN_TEXT.serialize(event.line(FILENAME_LINE)).trim();
            
            if (menuFileName.isEmpty()) {
                event.setCancelled(true);
                Text.send(player, "<red>You must write a menu name in the second line.");
                return;
            }
            
            menuFileName = Utils.addYamlExtension(menuFileName);
    
            InternalMenu menu = MenuManager.getMenuByFileName(menuFileName);
            if (menu == null) {
                event.setCancelled(true);
                Text.send(player, "<red>Menu \"" + menuFileName + "\" was not found.");
                return;
            }
    
            event.line(HEADER_LINE, event.line(HEADER_LINE).color(VALID_SIGN_COLOR));
            Text.send(player, "<green>Successfully created a sign for the menu " + menuFileName + ".");
        }
    }

    

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChangeMonitor(SignChangeEvent event) {
        // Prevent players without permissions from creating menu signs
        if (isValidMenuSign(event.line(HEADER_LINE)) && !canCreateMenuSign(event.getPlayer())) {
            event.line(HEADER_LINE, Component.text(PLAIN_TEXT.serialize(event.line(HEADER_LINE))));
        }
    }
    
    private boolean isCreatingMenuSign(String headerLine) {
        return headerLine.equalsIgnoreCase(SIGN_CREATION_TRIGGER);
    }
    
    private boolean isValidMenuSign(Component headerLine) {
        return headerLine != null && PLAIN_TEXT.serialize(headerLine).equalsIgnoreCase(PLAIN_VALID_SIGN_HEADER);
    }
    
    private boolean canCreateMenuSign(Player player) {
        return player.hasPermission(Permissions.SIGN_CREATE);
    }

}
