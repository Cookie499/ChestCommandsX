/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.parsing.menu;

import me.filoghost.fcommons.Preconditions;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

public class MenuOpenItem {

    private final Material material;
    private final ClickType clickType;

    public MenuOpenItem(Material material, ClickType clickType) {
        Preconditions.checkArgumentNotAir(material, "material");
        Preconditions.notNull(clickType, "clickType");
        
        this.material = material;
        this.clickType = clickType;
    }

    public boolean matches(ItemStack item, Action action) {
        if (item == null) {
            return false;
        }

        if (this.material != item.getType()) {
            return false;
        }
        return clickType.isValidInteract(action);
    }
}
