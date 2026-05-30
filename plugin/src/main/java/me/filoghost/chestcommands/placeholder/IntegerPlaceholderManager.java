/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.placeholder;

import me.filoghost.chestcommands.parsing.NumberParser;
import me.filoghost.chestcommands.parsing.ParseException;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntegerPlaceholderManager {

    private static final Pattern PLACEHOLDER_PATTERN =
            Pattern.compile("%(?:(private|public)_)?integer(?:_(plus|minus|default|reset))?:([^%]+)%");
    private static final Map<String, IntegerValue> publicValues = new HashMap<>();
    private static final Map<Object, Map<String, IntegerValue>> privateValuesByScope = new WeakHashMap<>();
    private static final ThreadLocal<Object> privateScope = new ThreadLocal<>();

    public static boolean hasPlaceholders(String text) {
        return PLACEHOLDER_PATTERN.matcher(text).find();
    }

    public static String replacePlaceholders(String text) {
        return replacePlaceholders(text, null);
    }

    public static String replacePlaceholders(String text, Integer maxValue) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuffer output = new StringBuffer();

        while (matcher.find()) {
            String scope = matcher.group(1);
            String operation = matcher.group(2);
            IntegerReference reference = IntegerReference.parse(matcher.group(3).trim());
            String replacement = applyOperation(scope, reference, operation, maxValue);
            matcher.appendReplacement(output, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(output);
        return output.toString();
    }

    public static void withPrivateScope(Object scope, Runnable runnable) {
        Object previousScope = privateScope.get();
        privateScope.set(scope);
        try {
            runnable.run();
        } finally {
            if (previousScope != null) {
                privateScope.set(previousScope);
            } else {
                privateScope.remove();
            }
        }
    }

    public static void clearPrivateScope(Object scope) {
        privateValuesByScope.remove(scope);
    }

    private static String applyOperation(String scope, IntegerReference reference, String operation, Integer maxValue) {
        Map<String, IntegerValue> values = getValues(scope);
        IntegerValue value = values.computeIfAbsent(reference.name, ignored -> createDefaultValue(reference));

        if ("plus".equalsIgnoreCase(operation)) {
            value = value.shift(1);
            value = value.clamp(maxValue);
            values.put(reference.name, value);
        } else if ("minus".equalsIgnoreCase(operation)) {
            value = value.shift(-1);
            value = value.clamp(maxValue);
            values.put(reference.name, value);
        } else if ("default".equalsIgnoreCase(operation) || "reset".equalsIgnoreCase(operation)) {
            value = value.getDefaultValue();
            value = value.clamp(maxValue);
            values.put(reference.name, value);
        }

        return value.clamp(maxValue).toString();
    }

    private static Map<String, IntegerValue> getValues(String scope) {
        if ("private".equalsIgnoreCase(scope)) {
            Object currentScope = privateScope.get();
            if (currentScope != null) {
                return privateValuesByScope.computeIfAbsent(currentScope, ignored -> new HashMap<>());
            }
        }

        return publicValues;
    }

    private static IntegerValue createDefaultValue(IntegerReference reference) {
        String defaultValue = reference.defaultValue != null ? reference.defaultValue : reference.name;
        String replacement = PlaceholderManager.replaceStaticPlaceholders(defaultValue);
        if (!replacement.equals(defaultValue) || reference.defaultValue != null) {
            try {
                return IntegerValue.parse(replacement);
            } catch (ParseException ignored) {
                // Fall back to 1 below.
            }
        }

        return IntegerValue.single(1);
    }

    private static class IntegerReference {

        private final String name;
        private final String defaultValue;

        static IntegerReference parse(String input) {
            int equalsIndex = input.indexOf('=');
            if (equalsIndex >= 0) {
                String name = input.substring(0, equalsIndex).trim();
                String defaultValue = input.substring(equalsIndex + 1).trim();
                return new IntegerReference(name, defaultValue);
            }

            return new IntegerReference(input, null);
        }

        private IntegerReference(String name, String defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }
    }

    private static class IntegerValue {

        private final int first;
        private final int last;
        private final IntegerValue defaultValue;

        static IntegerValue single(int value) {
            return new IntegerValue(value, value, null);
        }

        static IntegerValue parse(String input) throws ParseException {
            String[] parts = input.split("-", -1);
            if (parts.length == 1) {
                return single(NumberParser.getStrictlyPositiveInteger(parts[0].trim()));
            } else if (parts.length == 2) {
                int first = NumberParser.getStrictlyPositiveInteger(parts[0].trim());
                int last = NumberParser.getStrictlyPositiveInteger(parts[1].trim());
                if (first > last) {
                    throw new ParseException("integer range start cannot be greater than its end");
                }
                return new IntegerValue(first, last, null);
            } else {
                throw new ParseException("invalid integer range");
            }
        }

        private IntegerValue(int first, int last, IntegerValue defaultValue) {
            this.first = first;
            this.last = last;
            this.defaultValue = defaultValue != null ? defaultValue : this;
        }

        IntegerValue shift(int amount) {
            int shiftedFirst = first + amount;
            int shiftedLast = last + amount;
            if (shiftedFirst < 1) {
                int correction = 1 - shiftedFirst;
                shiftedFirst += correction;
                shiftedLast += correction;
            }

            return new IntegerValue(shiftedFirst, shiftedLast, defaultValue);
        }

        IntegerValue clamp(Integer maxValue) {
            if (maxValue == null) {
                return this;
            }

            int width = last - first;
            int clampedFirst = first;
            int clampedLast = last;
            if (clampedLast > maxValue) {
                clampedLast = maxValue;
                clampedFirst = Math.max(1, clampedLast - width);
            }
            if (clampedFirst < 1) {
                clampedFirst = 1;
                clampedLast = Math.min(maxValue, clampedFirst + width);
            }

            return new IntegerValue(clampedFirst, clampedLast, defaultValue);
        }

        IntegerValue getDefaultValue() {
            return defaultValue;
        }

        @Override
        public String toString() {
            if (first == last) {
                return String.valueOf(first);
            }

            return first + "-" + last;
        }
    }
}
