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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

final class MenuCommandRegistry {

    private static final String FALLBACK_PREFIX = "chestcommands";
    private static final Pattern VALID_COMMAND = Pattern.compile("[a-z0-9_-]+");

    private final CommandMap commandMap = Bukkit.getCommandMap();
    private final Map<String, Command> registeredCommands = new HashMap<>();

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
        syncCommands();
    }

    boolean register(String rawCommand, InternalMenu menu, ErrorCollector errorCollector) {
        String commandName = normalize(rawCommand);
        if (commandName.isEmpty()) {
            return false;
        }

        if (!VALID_COMMAND.matcher(commandName).matches()) {
            errorCollector.add("invalid menu command \"" + rawCommand + "\" in \"" + menu.getSourceFile()
                    + "\": use only letters, numbers, underscores, and hyphens");
            return false;
        }

        if (registeredCommands.containsKey(commandName)) {
            return false;
        }

        Command existingCommand = commandMap.getKnownCommands().get(commandName);
        if (existingCommand != null) {
            errorCollector.add("menu command \"/" + commandName + "\" in \"" + menu.getSourceFile()
                    + "\" conflicts with an already registered command");
            return false;
        }

        MenuCommand command = new MenuCommand(commandName, menu);
        boolean registered = commandMap.register(commandName, FALLBACK_PREFIX, command);
        if (!registered || commandMap.getKnownCommands().get(commandName) != command) {
            errorCollector.add("menu command \"/" + commandName + "\" in \"" + menu.getSourceFile()
                    + "\" could not be registered");
            return false;
        }

        registeredCommands.put(commandName, command);
        syncCommands();
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

        int firstSpace = commandName.indexOf(' ');
        if (firstSpace >= 0) {
            commandName = commandName.substring(0, firstSpace);
        }

        return commandName.toLowerCase(Locale.ROOT);
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
