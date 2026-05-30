/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.attribute;

import me.filoghost.chestcommands.icon.InternalConfigurableIcon;
import me.filoghost.chestcommands.logging.Errors;
import me.filoghost.chestcommands.parsing.NumberParser;
import me.filoghost.chestcommands.parsing.ParseException;
import me.filoghost.chestcommands.placeholder.PlaceholderManager;
import me.filoghost.chestcommands.placeholder.PlaceholderString;

public class AmountAttribute implements IconAttribute {

    private final Integer amount;
    private final PlaceholderString amountExpression;

    public AmountAttribute(int amount, AttributeErrorHandler errorHandler) throws ParseException {
        if (amount < 0) {
            throw new ParseException(Errors.Parsing.zeroOrPositive);
        }
        this.amount = amount;
        this.amountExpression = null;
    }

    public AmountAttribute(String amountExpression, AttributeErrorHandler errorHandler) throws ParseException {
        if (PlaceholderManager.hasDynamicPlaceholders(amountExpression)) {
            if (amountExpression.contains("-")) {
                throw new ParseException("amount cannot be a range");
            }

            this.amount = null;
            this.amountExpression = PlaceholderString.of(amountExpression);
            return;
        }

        if (amountExpression.contains("-")) {
            throw new ParseException("amount cannot be a range");
        }

        this.amount = NumberParser.getStrictlyPositiveInteger(amountExpression);
        this.amountExpression = null;
    }
    
    @Override
    public void apply(InternalConfigurableIcon icon) {
        if (amount != null) {
            icon.setAmount(amount);
        } else {
            icon.setAmountExpression(amountExpression);
        }
    }

}
