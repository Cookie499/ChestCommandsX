/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.attribute;

import me.filoghost.chestcommands.icon.InternalConfigurableIcon;

public class UnbreakableAttribute implements IconAttribute {

    private final boolean unbreakable;

    public UnbreakableAttribute(boolean unbreakable, AttributeErrorHandler errorHandler) {
        this.unbreakable = unbreakable;
    }

    @Override
    public void apply(InternalConfigurableIcon icon) {
        icon.setUnbreakable(unbreakable);
    }
}
