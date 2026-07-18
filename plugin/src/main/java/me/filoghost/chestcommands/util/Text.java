/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public final class Text {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private Text() {}

    public static Component parseMiniMessage(@Nullable String input) {
        if (input == null) {
            return Component.empty();
        }
        return MINI_MESSAGE.deserialize(input);
    }

    public static List<Component> components(List<String> lines) {
        return lines.stream().map(Text::parseMiniMessage).collect(Collectors.toList());
    }

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(parseMiniMessage(message));
    }
}
