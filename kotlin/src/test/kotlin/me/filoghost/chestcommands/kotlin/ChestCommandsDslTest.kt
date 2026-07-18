package me.filoghost.chestcommands.kotlin

import me.filoghost.chestcommands.api.ConfigurableIcon
import me.filoghost.chestcommands.api.Icon
import me.filoghost.chestcommands.api.Menu
import org.assertj.core.api.Assertions.assertThat
import org.bukkit.Material
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class ChestCommandsDslTest {

    @Test
    fun `menu builder places and removes icons`() {
        val menu = mock(Menu::class.java)
        val icon = mock(Icon::class.java)

        menu.applyMenu {
            icon(1, 2, icon)
            remove(0, 4)
        }

        verify(menu).setIcon(1, 2, icon)
        verify(menu).setIcon(0, 4, null)
    }

    @Test
    fun `linear slots use menu column count`() {
        val menu = mock(Menu::class.java)
        val icon = mock(Icon::class.java)
        `when`(menu.columns).thenReturn(9)

        menu.applyMenu { this[20] = icon }

        verify(menu).setIcon(2, 2, icon)
    }

    @Test
    fun `configurable icon builder delegates properties`() {
        val icon = mock(ConfigurableIcon::class.java)
        val builder = ConfigurableIconBuilder(icon)

        builder.amount = 3
        builder.name = "Shop"
        builder.lore("First", "Second")
        builder.placeholders = true
        builder.material = Material.DIAMOND

        verify(icon).amount = 3
        verify(icon).name = "Shop"
        verify(icon).setLore("First", "Second")
        verify(icon).isPlaceholdersEnabled = true
        verify(icon).material = Material.DIAMOND
        assertThat(builder.icon).isSameAs(icon)
    }
}
