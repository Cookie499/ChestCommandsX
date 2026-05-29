/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.attribute;

import me.filoghost.chestcommands.icon.InternalConfigurableIcon;
import me.filoghost.chestcommands.logging.Errors;
import me.filoghost.chestcommands.parsing.ParseException;

public class DamageAttribute implements IconAttribute {

    private final int damage;

    public DamageAttribute(int damage, AttributeErrorHandler errorHandler) throws ParseException {
        if (damage < 0) {
            throw new ParseException(Errors.Parsing.zeroOrPositive);
        }
        this.damage = damage;
    }

    @Override
    public void apply(InternalConfigurableIcon icon) {
        icon.setDamage(damage);
    }
}
