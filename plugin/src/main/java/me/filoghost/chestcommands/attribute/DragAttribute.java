/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.attribute;

import me.filoghost.chestcommands.icon.InternalConfigurableIcon;

public class DragAttribute implements IconAttribute {

    private final boolean draggable;

    public DragAttribute(boolean draggable, AttributeErrorHandler errorHandler) {
        this.draggable = draggable;
    }

    @Override
    public void apply(InternalConfigurableIcon icon) {
        icon.setDraggable(draggable);
    }
}
