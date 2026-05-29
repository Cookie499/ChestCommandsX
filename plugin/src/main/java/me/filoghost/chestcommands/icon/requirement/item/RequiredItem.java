/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.icon.requirement.item;

import me.filoghost.fcommons.Preconditions;
import org.bukkit.Material;

public class RequiredItem {

    private final Material material;
    private final int amount;

    public RequiredItem(Material material, int amount) {
        Preconditions.checkArgumentNotAir(material, "material");

        this.material = material;
        this.amount = amount;
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isMatchingType(RemainingItem item) {
        return item != null && item.getMaterial() == material;
    }
    
}
