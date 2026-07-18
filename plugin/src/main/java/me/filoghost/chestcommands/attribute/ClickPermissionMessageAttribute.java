/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.attribute;

import me.filoghost.chestcommands.icon.InternalConfigurableIcon;

public class ClickPermissionMessageAttribute implements IconAttribute {

    private final String message;

    public ClickPermissionMessageAttribute(String serializedMessage, AttributeErrorHandler errorHandler) {
        this.message = serializedMessage;
    }
    
    @Override
    public void apply(InternalConfigurableIcon icon) {
        icon.setNoClickPermissionMessage(message);
    }

}
