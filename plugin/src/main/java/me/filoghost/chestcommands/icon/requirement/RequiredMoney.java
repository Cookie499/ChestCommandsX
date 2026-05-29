/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.icon.requirement;

import com.google.common.base.Preconditions;
import me.filoghost.chestcommands.config.Lang;
import me.filoghost.chestcommands.hook.VaultEconomyHook;
import me.filoghost.chestcommands.logging.Errors;
import me.filoghost.chestcommands.util.Text;
import org.bukkit.entity.Player;

public class RequiredMoney implements Requirement {

    private final double moneyAmount;

    public RequiredMoney(double moneyAmount) {
        Preconditions.checkArgument(moneyAmount > 0.0, "money amount must be positive");
        this.moneyAmount = moneyAmount;
    }

    @Override
    public boolean hasCost(Player player) {
        if (!VaultEconomyHook.INSTANCE.isEnabled()) {
            Text.send(player, Errors.User.configurationError(
                    "the item has a price, but Vault with a compatible economy plugin was not found. "
                    + "For security, the action has been blocked"));
            return false;
        }

        if (!VaultEconomyHook.hasMoney(player, moneyAmount)) {
            Text.send(player, Lang.get().no_money.replace("{money}", VaultEconomyHook.formatMoney(moneyAmount)));
            return false;
        }

        return true;
    }

    @Override
    public boolean takeCost(Player player) {
        boolean success = VaultEconomyHook.takeMoney(player, moneyAmount);

        if (!success) {
            Text.send(player, Errors.User.configurationError("a money transaction couldn't be executed"));
        }

        return success;
    }

}
