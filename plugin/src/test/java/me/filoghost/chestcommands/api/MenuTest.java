/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.api;

import me.filoghost.chestcommands.test.BukkitMocks;
import me.filoghost.chestcommands.util.Text;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MenuTest {

    @Test
    void setIcon() {
        Menu menu = createMenu(1);
        menu.setIcon(0, 0, StaticIcon.create(createItemStack()));

        assertThat(menu.getIcon(0, 0)).isNotNull();
    }

    @Test
    void unsetIcon() {
        Menu menu = createMenu(1);
        menu.setIcon(0, 0, StaticIcon.create(createItemStack()));
        menu.setIcon(0, 0, null);

        assertThat(menu.getIcon(0, 0)).isNull();
    }

    @Test
    void iterationRowsColumns() {
        Menu menu = createMenu(3);

        for (int row = 0; row < menu.getRows(); row++) {
            for (int column = 0; column < menu.getColumns(); column++) {
                menu.setIcon(row, column, StaticIcon.create(createItemStack()));
            }
        }
    }

    @Test
    void invalidEmptyMenu() {
        assertThatIllegalArgumentException().isThrownBy(() -> {
            createMenu(0);
        });
    }

    @ParameterizedTest
    @CsvSource({
            "9, 0",
            "0, 9",
            "-1, 0",
            "0, -1",
    })
    void iconOutOfBounds(int row, int column) {
        Menu menu = createMenu(1);
        assertThatExceptionOfType(IndexOutOfBoundsException.class).isThrownBy(() -> {
            menu.getIcon(row, column);
        });
    }

    private Menu createMenu(int rowCount) {
        return Menu.create(BukkitMocks.PLUGIN, Text.parseMiniMessage("Test menu"), rowCount);
    }

    private ItemStack createItemStack() {
        return mock(ItemStack.class);
    }


}
