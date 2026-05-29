/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.menu;

import me.filoghost.chestcommands.util.FoliaScheduler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

class MenuCommand extends Command {

    private final InternalMenu menu;

    MenuCommand(String label, InternalMenu menu) {
        super(label);
        this.menu = menu;
        setDescription("Opens a ChestCommands menu.");
        setUsage("/" + label);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        FoliaScheduler.runAtPlayer(player, () -> menu.openCheckingPermission(player));
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
