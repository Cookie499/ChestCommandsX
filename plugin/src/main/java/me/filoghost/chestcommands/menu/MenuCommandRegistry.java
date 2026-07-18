/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.menu;

import me.filoghost.fcommons.logging.ErrorCollector;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

final class MenuCommandRegistry {

    private static final Pattern VALID_COMMAND = Pattern.compile("[a-z0-9_-]+");

    private final Set<String> activeRootCommands = new HashSet<>();
    private final Set<String> lifecycleRegisteredRoots = new HashSet<>();
    private final Map<String, Set<String>> fullCommandsByRoot = new HashMap<>();
    private boolean lifecycleRegistrationCompleted;

    void registerLifecycleHandler(Plugin plugin) {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();
            lifecycleRegisteredRoots.clear();
            for (String rootCommand : activeRootCommands) {
                commands.register(rootCommand, "Opens a ChestCommands menu.", new MenuCommand(rootCommand));
                lifecycleRegisteredRoots.add(rootCommand);
            }
            lifecycleRegistrationCompleted = true;
        });
    }

    void unregisterAll() {
        activeRootCommands.clear();
        fullCommandsByRoot.clear();
    }

    boolean register(String rawCommand, InternalMenu menu, ErrorCollector errorCollector) {
        String fullCommand = normalize(rawCommand);
        if (fullCommand.isEmpty()) {
            return false;
        }

        List<String> commandParts = split(fullCommand);
        if (commandParts.isEmpty()) {
            return false;
        }

        for (String commandPart : commandParts) {
            if (!VALID_COMMAND.matcher(commandPart).matches()) {
                errorCollector.add("invalid menu command \"" + rawCommand + "\" in \"" + menu.getSourceFile()
                        + "\": use only letters, numbers, underscores, hyphens, and spaces between sub-commands");
                return false;
            }
        }

        String rootCommand = commandParts.get(0);

        if (fullCommandsByRoot.getOrDefault(rootCommand, Collections.emptySet()).contains(fullCommand)) {
            return false;
        }

        if (lifecycleRegistrationCompleted && !lifecycleRegisteredRoots.contains(rootCommand)) {
            errorCollector.add("menu command \"/" + rootCommand + "\" in \"" + menu.getSourceFile()
                    + "\" requires a server restart because it introduces a new root command");
            return false;
        }

        activeRootCommands.add(rootCommand);
        fullCommandsByRoot.computeIfAbsent(rootCommand, ignored -> new HashSet<>()).add(fullCommand);
        return true;
    }

    static String normalize(String rawCommand) {
        if (rawCommand == null) {
            return "";
        }

        String commandName = rawCommand.trim();
        while (commandName.startsWith("/")) {
            commandName = commandName.substring(1);
        }

        return String.join(" ", split(commandName)).toLowerCase(Locale.ROOT);
    }

    static List<String> split(String command) {
        if (command == null) {
            return Collections.emptyList();
        }

        String trimmedCommand = command.trim();
        if (trimmedCommand.isEmpty()) {
            return Collections.emptyList();
        }

        return new ArrayList<>(Arrays.asList(trimmedCommand.split("\\s+")));
    }

}
