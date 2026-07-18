/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
@file:JvmName("ChestCommandsDsl")

package me.filoghost.chestcommands.kotlin

import me.filoghost.chestcommands.api.ClickHandler
import me.filoghost.chestcommands.api.ConfigurableIcon
import me.filoghost.chestcommands.api.Icon
import me.filoghost.chestcommands.api.Menu
import me.filoghost.chestcommands.api.MenuView
import me.filoghost.chestcommands.api.StaticIcon
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.block.banner.Pattern
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

/** Marks receivers belonging to the Chest Commands Kotlin DSL. */
@DslMarker
public annotation class ChestCommandsMarker

/** Creates a menu and configures it with a type-safe Kotlin DSL. */
public inline fun Plugin.menu(
    title: Component,
    rows: Int,
    block: MenuBuilder.() -> Unit = {},
): Menu = Menu.create(this, title, rows).applyMenu(block)

/** Creates a menu whose title is a plain-text component. */
public inline fun Plugin.menu(
    title: String,
    rows: Int,
    block: MenuBuilder.() -> Unit = {},
): Menu = menu(Component.text(title), rows, block)

/** Configures an existing menu. */
public inline fun Menu.applyMenu(block: MenuBuilder.() -> Unit): Menu =
    also { MenuBuilder(it).block() }

/** Creates a configurable icon. */
public inline fun configurableIcon(
    material: Material,
    block: ConfigurableIconBuilder.() -> Unit = {},
): ConfigurableIcon = ConfigurableIcon.create(material).applyIcon(block)

/** Configures an existing configurable icon. */
public inline fun ConfigurableIcon.applyIcon(
    block: ConfigurableIconBuilder.() -> Unit,
): ConfigurableIcon = also { ConfigurableIconBuilder(it).block() }

/** Creates a static item-stack icon. */
public inline fun staticIcon(
    itemStack: ItemStack,
    block: ClickableIconBuilder.() -> Unit = {},
): StaticIcon = StaticIcon.create(itemStack).also { ClickableIconBuilder(it).block() }

/** Menu layout receiver. Row and column indexes are zero-based, matching the Java API. */
@ChestCommandsMarker
public class MenuBuilder @PublishedApi internal constructor(public val menu: Menu) {

    /** Places [icon] at a zero-based row and column. */
    public fun icon(row: Int, column: Int, icon: Icon) {
        menu.setIcon(row, column, icon)
    }

    /** Creates and places a configurable icon. */
    public inline fun icon(
        row: Int,
        column: Int,
        material: Material,
        block: ConfigurableIconBuilder.() -> Unit = {},
    ): ConfigurableIcon = configurableIcon(material, block).also { icon(row, column, it) }

    /** Creates and places a static item-stack icon. */
    public inline fun icon(
        row: Int,
        column: Int,
        itemStack: ItemStack,
        block: ClickableIconBuilder.() -> Unit = {},
    ): StaticIcon = staticIcon(itemStack, block).also { icon(row, column, it) }

    /** Removes the icon at a zero-based row and column. */
    public fun remove(row: Int, column: Int) {
        menu.setIcon(row, column, null)
    }

    /** Places an icon using a zero-based linear inventory slot. */
    public operator fun set(slot: Int, icon: Icon) {
        require(slot >= 0) { "slot must be non-negative" }
        menu.setIcon(slot / menu.columns, slot % menu.columns, icon)
    }
}

/** Common click configuration shared by static and configurable icons. */
@ChestCommandsMarker
public open class ClickableIconBuilder @PublishedApi internal constructor(
    private val icon: me.filoghost.chestcommands.api.ClickableIcon,
) {
    /** Registers the icon click callback. */
    public fun onClick(handler: (view: MenuView, player: Player) -> Unit) {
        icon.clickHandler = ClickHandler(handler)
    }
}

/** Receiver for configurable item properties. */
@ChestCommandsMarker
public class ConfigurableIconBuilder @PublishedApi internal constructor(
    public val icon: ConfigurableIcon,
) : ClickableIconBuilder(icon) {

    public var material: Material
        get() = icon.material
        set(value) { icon.material = value }

    public var amount: Int
        get() = icon.amount
        set(value) { icon.amount = value }

    public var name: String?
        get() = icon.name
        set(value) { icon.name = value }

    public var lore: List<String>?
        get() = icon.lore
        set(value) { icon.lore = value }

    public var nbtData: String?
        get() = icon.nbtData
        set(value) { icon.nbtData = value }

    public var leatherColor: Color?
        get() = icon.leatherColor
        set(value) { icon.leatherColor = value }

    public var skullOwner: String?
        get() = icon.skullOwner
        set(value) { icon.skullOwner = value }

    public var bannerColor: DyeColor?
        get() = icon.bannerColor
        set(value) { icon.bannerColor = value }

    public var bannerPatterns: List<Pattern>?
        get() = icon.bannerPatterns
        set(value) { icon.bannerPatterns = value }

    public var placeholders: Boolean
        get() = icon.isPlaceholdersEnabled
        set(value) { icon.isPlaceholdersEnabled = value }

    /** Replaces the lore with the supplied lines. */
    public fun lore(vararg lines: String) {
        icon.setLore(*lines)
    }

    /** Adds an enchantment and optional level. */
    public fun enchant(enchantment: Enchantment, level: Int = 1) {
        icon.addEnchantment(enchantment, level)
    }
}
