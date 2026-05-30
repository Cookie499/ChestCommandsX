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
import org.jetbrains.annotations.Nullable;

import java.util.List;

class MenuCommand extends Command {

    MenuCommand(String label) {
        super(label);
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
        InternalMenu menu = getMenu(commandLabel, args);
        if (menu == null) {
            return false;
        }

        FoliaScheduler.runAtPlayer(player, () -> menu.openCheckingPermission(player));
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        return MenuManager.getOpenCommandSuggestions(alias, args);
    }

    private static @Nullable InternalMenu getMenu(String commandLabel, String[] args) {
        if (args.length == 0) {
            return MenuManager.getMenuByCommandLine(commandLabel);
        }

        return MenuManager.getMenuByCommandLine(commandLabel + " " + String.join(" ", args));
    }
}
