/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.config;

import me.filoghost.chestcommands.logging.Errors;
import me.filoghost.fcommons.config.mapped.MappedConfig;
public class Lang implements MappedConfig {

    public String no_open_permission = "<red>You don't have permission <yellow>{permission}</yellow> to use this menu.";
    public String default_no_icon_permission = "<red>You don't have permission for this icon.";
    public String no_required_item = "<red>You must have <yellow>{amount}x {material}</yellow> for this.";
    public String no_money = "<red>You need {money}$ for this.";
    public String no_exp = "<red>You need {levels} XP levels for this.";
    public String menu_not_found = "<red>Menu not found! " + Errors.User.notifyStaffRequest;
    public String any = "any";
    
    private static Lang instance;
    
    static void setInstance(Lang instance) {
        Lang.instance = instance;
    }

    public static Lang get() {
        return instance;
    }
    
}
