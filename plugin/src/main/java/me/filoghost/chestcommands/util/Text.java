/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public final class Text {

    private static final LegacyComponentSerializer LEGACY_SECTION = LegacyComponentSerializer.legacySection();

    private Text() {}

    public static Component component(@Nullable String text) {
        if (text == null) {
            return Component.empty();
        }
        return LEGACY_SECTION.deserialize(text.replace('&', '§'));
    }

    public static List<Component> components(List<String> lines) {
        return lines.stream().map(Text::component).collect(Collectors.toList());
    }

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(component(message));
    }
}
