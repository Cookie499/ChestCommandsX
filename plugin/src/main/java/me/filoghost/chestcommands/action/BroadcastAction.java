/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.action;

import me.filoghost.chestcommands.placeholder.PlaceholderString;
import me.filoghost.chestcommands.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BroadcastAction implements Action {
    
    private final PlaceholderString message;

    public BroadcastAction(String serializedAction) {
        message = PlaceholderString.of(serializedAction);
    }

    @Override
    public void execute(Player player) {
        Bukkit.broadcast(Text.parseMiniMessage(message.getValue(player)));
    }

}
