/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.menu;

import me.filoghost.fcommons.logging.ErrorCollector;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

final class MenuCommandRegistry {

    private static final String FALLBACK_PREFIX = "chestcommands";
    private static final Pattern VALID_COMMAND = Pattern.compile("[a-z0-9_-]+");

    private final CommandMap commandMap = Bukkit.getCommandMap();
    private final Map<String, Command> registeredCommands = new HashMap<>();
    private final Map<String, Set<String>> fullCommandsByRoot = new HashMap<>();

    void unregisterAll() {
        Map<String, Command> knownCommands = commandMap.getKnownCommands();

        for (Command command : registeredCommands.values()) {
            command.unregister(commandMap);
            Iterator<Map.Entry<String, Command>> iterator = knownCommands.entrySet().iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getValue() == command) {
                    iterator.remove();
                }
            }
        }

        registeredCommands.clear();
        fullCommandsByRoot.clear();
        syncCommands();
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

        if (!registeredCommands.containsKey(rootCommand) && commandMap.getKnownCommands().get(rootCommand) != null) {
            errorCollector.add("invalid menu command \"" + rawCommand + "\" in \"" + menu.getSourceFile()
                    + "\": root command \"/" + rootCommand + "\" conflicts with an already registered command");
            return false;
        }

        if (!registeredCommands.containsKey(rootCommand)) {
            MenuCommand command = new MenuCommand(rootCommand);
            boolean registered = commandMap.register(rootCommand, FALLBACK_PREFIX, command);
            if (!registered || commandMap.getKnownCommands().get(rootCommand) != command) {
                errorCollector.add("menu command \"/" + rootCommand + "\" in \"" + menu.getSourceFile()
                        + "\" could not be registered");
                return false;
            }

            registeredCommands.put(rootCommand, command);
            syncCommands();
        }

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

    private static void syncCommands() {
        try {
            Method syncCommands = Bukkit.getServer().getClass().getMethod("syncCommands");
            syncCommands.invoke(Bukkit.getServer());
        } catch (ReflectiveOperationException ignored) {
            // Available on CraftBukkit/Paper runtime, absent from the public API.
        }
    }
}
