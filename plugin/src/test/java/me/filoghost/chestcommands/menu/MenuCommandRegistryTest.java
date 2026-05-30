/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.menu;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MenuCommandRegistryTest {

    @Test
    void normalizesRootCommand() {
        assertThat(MenuCommandRegistry.normalize("/Shop")).isEqualTo("shop");
    }

    @Test
    void normalizesSubCommand() {
        assertThat(MenuCommandRegistry.normalize("/Shop   1")).isEqualTo("shop 1");
    }

    @Test
    void splitsCommandParts() {
        assertThat(MenuCommandRegistry.split(" shop   sell  1 "))
                .containsExactly("shop", "sell", "1");
    }

}
