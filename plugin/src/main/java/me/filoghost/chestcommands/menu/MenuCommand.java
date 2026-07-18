/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.menu;

import me.filoghost.chestcommands.util.FoliaScheduler;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

class MenuCommand implements BasicCommand {

    private final String label;

    MenuCommand(String label) {
        this.label = label;
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
        CommandSender sender = commandSourceStack.getSender();
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return;
        }

        Player player = (Player) sender;
        InternalMenu menu = getMenu(label, args);
        if (menu == null) {
            return;
        }

        FoliaScheduler.runAtPlayer(player, () -> menu.openCheckingPermission(player));
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
        return MenuManager.getOpenCommandSuggestions(label, args);
    }

    private static @Nullable InternalMenu getMenu(String commandLabel, String[] args) {
        if (args.length == 0) {
            return MenuManager.getMenuByCommandLine(commandLabel);
        }

        return MenuManager.getMenuByCommandLine(commandLabel + " " + String.join(" ", args));
    }
}
