/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.parsing;

import me.filoghost.chestcommands.logging.Errors;
import me.filoghost.fcommons.MaterialsHelper;
import me.filoghost.fcommons.Preconditions;
import me.filoghost.fcommons.Strings;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemStackParser {

    private final Material material;
    private int amount = 1;

    /*
     * Reads item in the format "material, amount".
     */
    public ItemStackParser(String input, boolean parseAmount) throws ParseException {
        Preconditions.notNull(input, "input");

        if (parseAmount) {
            // Read the optional amount
            String[] splitAmount = Strings.splitAndTrim(input, ",", 2);

            if (splitAmount.length > 1) {
                try {
                    this.amount = NumberParser.getStrictlyPositiveInteger(splitAmount[1]);
                } catch (ParseException e) {
                    throw new ParseException(Errors.Parsing.invalidAmount(splitAmount[1]), e);
                }

                // Only keep the first part as input
                input = splitAmount[0];
            }
        }

        if (input.contains(":")) {
            throw new ParseException("legacy material data values are no longer supported; use a modern 1.21 material name instead");
        }

        this.material = MaterialParser.parseMaterial(input);
    }

    public void checkNotAir() throws ParseException {
        if (MaterialsHelper.isAir(material)) {
            throw new ParseException(Errors.Parsing.materialCannotBeAir);
        }
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public ItemStack createStack() {
        return new ItemStack(material, amount);
    }

}
