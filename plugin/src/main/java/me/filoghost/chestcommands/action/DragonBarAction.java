/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.action;

import me.filoghost.chestcommands.logging.Errors;
import me.filoghost.chestcommands.parsing.NumberParser;
import me.filoghost.chestcommands.parsing.ParseException;
import me.filoghost.chestcommands.placeholder.PlaceholderString;
import me.filoghost.chestcommands.util.FoliaScheduler;
import me.filoghost.fcommons.Colors;
import me.filoghost.fcommons.Strings;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class DragonBarAction implements Action {

    private final PlaceholderString message;
    private final int seconds;

    public DragonBarAction(String serialiazedAction) throws ParseException {
        String message;

        String[] split = Strings.splitAndTrim(serialiazedAction, "|", 2); // Max of 2 pieces
        if (split.length > 1) {
            try {
                seconds = NumberParser.getStrictlyPositiveInteger(split[0]);
                message = split[1];
            } catch (ParseException e) {
                throw new ParseException(Errors.Parsing.invalidBossBarTime(split[0]), e);
            }
        } else {
            seconds = 1;
            message = serialiazedAction;
        }

        this.message = PlaceholderString.of(Colors.addColors(message));
    }

    @Override
    public void execute(Player player) {
        BossBar bossBar = Bukkit.createBossBar(message.getValue(player), BarColor.GREEN, BarStyle.SOLID);
        bossBar.addPlayer(player);
        bossBar.setVisible(true);

        FoliaScheduler.runAtPlayerLater(player, () -> {
            bossBar.removePlayer(player);
            bossBar.setVisible(false);
        }, seconds * 20L);
    }

}
