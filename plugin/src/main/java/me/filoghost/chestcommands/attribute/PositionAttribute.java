/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.attribute;

import me.filoghost.chestcommands.icon.InternalConfigurableIcon;
import me.filoghost.chestcommands.parsing.NumberParser;
import me.filoghost.chestcommands.parsing.ParseException;
import me.filoghost.chestcommands.placeholder.PlaceholderManager;

public class PositionAttribute implements IconAttribute {
    
    private final int firstPosition;
    private final int lastPosition;
    private final String serializedPosition;

    public PositionAttribute(int position, AttributeErrorHandler errorHandler) {
        this.firstPosition = position;
        this.lastPosition = position;
        this.serializedPosition = null;
    }

    public PositionAttribute(String serializedPosition, AttributeErrorHandler errorHandler) throws ParseException {
        int[] parsedRange = parseRange(PlaceholderManager.replaceIntegerPlaceholders(serializedPosition));
        this.firstPosition = parsedRange[0];
        this.lastPosition = parsedRange[1];
        this.serializedPosition = serializedPosition;
    }

    private static int[] parseRange(String serializedPosition) throws ParseException {
        String[] parts = serializedPosition.split("-", -1);
        if (parts.length == 1) {
            int position = NumberParser.getStrictlyPositiveInteger(parts[0].trim());
            return new int[] {position, position};
        } else if (parts.length == 2) {
            int firstPosition = NumberParser.getStrictlyPositiveInteger(parts[0].trim());
            int lastPosition = NumberParser.getStrictlyPositiveInteger(parts[1].trim());
            if (firstPosition > lastPosition) {
                throw new ParseException("position range start cannot be greater than its end");
            }
            return new int[] {firstPosition, lastPosition};
        } else {
            throw new ParseException("invalid position range");
        }
    }
    
    public int getPosition() {
        return firstPosition;
    }

    public int getFirstPosition() {
        return getCurrentRange()[0];
    }

    public int getFirstPosition(int maxPosition) {
        return getCurrentRange(maxPosition)[0];
    }

    public int getLastPosition() {
        return getCurrentRange()[1];
    }

    public int getLastPosition(int maxPosition) {
        return getCurrentRange(maxPosition)[1];
    }

    private int[] getCurrentRange() {
        return getCurrentRange(null);
    }

    private int[] getCurrentRange(Integer maxPosition) {
        if (serializedPosition == null) {
            return new int[] {firstPosition, lastPosition};
        }

        try {
            String replacedPosition = maxPosition != null
                    ? PlaceholderManager.replaceIntegerPlaceholders(serializedPosition, maxPosition)
                    : PlaceholderManager.replaceIntegerPlaceholders(serializedPosition);
            return parseRange(replacedPosition);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void apply(InternalConfigurableIcon icon) {
        // Position has no effect on the icon itself
    }
}
