/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.attribute;

import me.filoghost.chestcommands.parsing.ParseException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PositionAttributeTest {

    @Test
    void parsesSinglePosition() throws ParseException {
        PositionAttribute position = new PositionAttribute("3", null);

        assertThat(position.getFirstPosition()).isEqualTo(3);
        assertThat(position.getLastPosition()).isEqualTo(3);
        assertThat(position.getPosition()).isEqualTo(3);
    }

    @Test
    void parsesPositionRange() throws ParseException {
        PositionAttribute position = new PositionAttribute("3-4", null);

        assertThat(position.getFirstPosition()).isEqualTo(3);
        assertThat(position.getLastPosition()).isEqualTo(4);
    }

    @Test
    void rejectsReversedPositionRange() {
        assertThatExceptionOfType(ParseException.class).isThrownBy(() -> {
            new PositionAttribute("4-3", null);
        });
    }

}
