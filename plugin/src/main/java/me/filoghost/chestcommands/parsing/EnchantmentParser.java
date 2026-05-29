/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.parsing;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import me.filoghost.chestcommands.logging.Errors;
import me.filoghost.fcommons.Strings;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EnchantmentParser {

    private static final Map<String, Enchantment> ENCHANTMENTS = new HashMap<>();

    static {
        Registry<Enchantment> enchantments = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        for (Enchantment enchantment : enchantments) {
            ENCHANTMENTS.put(normalize(enchantments.getKeyOrThrow(enchantment).getKey()), enchantment);
            ENCHANTMENTS.put(normalize(enchantments.getKeyOrThrow(enchantment).toString()), enchantment);
        }

        // Add aliases
        putAlias("Protection", Enchantment.PROTECTION);
        putAlias("Fire Protection", Enchantment.FIRE_PROTECTION);
        putAlias("Feather Falling", Enchantment.FEATHER_FALLING);
        putAlias("Blast Protection", Enchantment.BLAST_PROTECTION);
        putAlias("Projectile Protection", Enchantment.PROJECTILE_PROTECTION);
        putAlias("Respiration", Enchantment.RESPIRATION);
        putAlias("Aqua Affinity", Enchantment.AQUA_AFFINITY);
        putAlias("Thorns", Enchantment.THORNS);
        putAlias("Sharpness", Enchantment.SHARPNESS);
        putAlias("Smite", Enchantment.SMITE);
        putAlias("Bane Of Arthropods", Enchantment.BANE_OF_ARTHROPODS);
        putAlias("Knockback", Enchantment.KNOCKBACK);
        putAlias("Fire Aspect", Enchantment.FIRE_ASPECT);
        putAlias("Looting", Enchantment.LOOTING);
        putAlias("Efficiency", Enchantment.EFFICIENCY);
        putAlias("Silk Touch", Enchantment.SILK_TOUCH);
        putAlias("Unbreaking", Enchantment.UNBREAKING);
        putAlias("Fortune", Enchantment.FORTUNE);
        putAlias("Power", Enchantment.POWER);
        putAlias("Punch", Enchantment.PUNCH);
        putAlias("Flame", Enchantment.FLAME);
        putAlias("Infinity", Enchantment.INFINITY);
        putAlias("Lure", Enchantment.LURE);
        putAlias("Luck Of The Sea", Enchantment.LUCK_OF_THE_SEA);
    }

    public static EnchantmentDetails parseEnchantment(String input) throws ParseException {
        int level = 1;

        if (input.contains(",")) {
            String[] levelSplit = Strings.splitAndTrim(input, ",", 2);

            try {
                level = NumberParser.getStrictlyPositiveInteger(levelSplit[1]);
            } catch (ParseException e) {
                throw new ParseException(Errors.Parsing.invalidEnchantmentLevel(levelSplit[1]), e);
            }
            input = levelSplit[0];
        }

        Enchantment enchantment = ENCHANTMENTS.get(normalize(input));

        if (enchantment != null) {
            return new EnchantmentDetails(enchantment, level);
        } else {
            throw new ParseException(Errors.Parsing.unknownEnchantmentType(input));
        }
    }

    private static void putAlias(String alias, Enchantment enchantment) {
        ENCHANTMENTS.put(normalize(alias), enchantment);
    }

    private static String normalize(String input) {
        return input.toLowerCase(Locale.ROOT)
                .replace("minecraft:", "")
                .replace(' ', '_')
                .replace('-', '_');
    }


    public static class EnchantmentDetails {

        private final Enchantment enchantment;
        private final int level;

        private EnchantmentDetails(Enchantment enchantment, int level) {
            this.enchantment = enchantment;
            this.level = level;
        }

        public Enchantment getEnchantment() {
            return enchantment;
        }

        public int getLevel() {
            return level;
        }

    }

}
