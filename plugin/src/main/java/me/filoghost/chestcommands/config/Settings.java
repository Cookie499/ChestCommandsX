/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.config;

import me.filoghost.fcommons.config.mapped.MappedConfig;

import java.util.Arrays;
import java.util.List;

public class Settings implements MappedConfig {

    public String default_color__name = "<white>";
    public String default_color__lore = "<gray>";
    public int anti_click_spam_delay = 200;
    public boolean update_notifications = true;
    
    private static Settings instance;

    static void setInstance(Settings instance) {
        Settings.instance = instance;
    }
    
    public static Settings get() {
        return instance;
    }

    @Override
    public List<String> getHeader() {
        return Arrays.asList(
                "ChestCommands main configuration file.",
                "Documentation: https://filoghost.me/docs/chest-commands");
    }

}
