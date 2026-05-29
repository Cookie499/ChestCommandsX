/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.attribute;

import me.filoghost.chestcommands.icon.InternalConfigurableIcon;
import me.filoghost.chestcommands.parsing.ParseException;
import me.filoghost.fcommons.collection.EnumLookupRegistry;
import me.filoghost.fcommons.collection.LookupRegistry;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemFlagsAttribute implements IconAttribute {

    private static final LookupRegistry<ItemFlag> ITEM_FLAGS = EnumLookupRegistry.fromEnumValues(ItemFlag.class);

    private final List<ItemFlag> flags;

    public ItemFlagsAttribute(List<String> serializedFlags, AttributeErrorHandler errorHandler) {
        this.flags = new ArrayList<>();

        for (String serializedFlag : serializedFlags) {
            if (serializedFlag == null || serializedFlag.isEmpty()) {
                continue;
            }

            if ("all".equalsIgnoreCase(serializedFlag)) {
                flags.addAll(Arrays.asList(ItemFlag.values()));
                continue;
            }

            ItemFlag flag = ITEM_FLAGS.lookup(serializedFlag);
            if (flag != null) {
                flags.add(flag);
            } else {
                errorHandler.onListElementError(serializedFlag, new ParseException("unknown item flag \"" + serializedFlag + "\""));
            }
        }
    }

    @Override
    public void apply(InternalConfigurableIcon icon) {
        icon.setItemFlags(flags);
    }
}
