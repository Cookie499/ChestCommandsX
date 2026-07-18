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
import me.filoghost.chestcommands.util.Text;
import me.filoghost.fcommons.Strings;
import net.kyori.adventure.bossbar.BossBar;
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

        this.message = PlaceholderString.of(message);
    }

    @Override
    public void execute(Player player) {
        BossBar bossBar = BossBar.bossBar(
                Text.parseMiniMessage(message.getValue(player)),
                1.0f,
                BossBar.Color.GREEN,
                BossBar.Overlay.PROGRESS);
        player.showBossBar(bossBar);

        FoliaScheduler.runAtPlayerLater(player, () -> player.hideBossBar(bossBar), seconds * 20L);
    }

}
