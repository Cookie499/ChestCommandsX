/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.attribute;

import me.filoghost.chestcommands.icon.InternalConfigurableIcon;
import me.filoghost.chestcommands.parsing.ParseException;

public class NBTDataAttribute implements IconAttribute {

    public NBTDataAttribute(String nbtData, AttributeErrorHandler errorHandler) throws ParseException {
        throw new ParseException("NBT-DATA is no longer supported on Minecraft 1.21; use DAMAGE, CUSTOM-MODEL-DATA, COLOR, SKULL-OWNER, ENCHANTMENTS, UNBREAKABLE, and ITEM-FLAGS instead");
    }

    @Override
    public void apply(InternalConfigurableIcon icon) {
        // Constructor always rejects this attribute.
    }

}
