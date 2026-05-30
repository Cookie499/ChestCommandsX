/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.menu;

import com.google.common.collect.ImmutableList;
import me.filoghost.chestcommands.ChestCommands;
import me.filoghost.chestcommands.Permissions;
import me.filoghost.chestcommands.action.Action;
import me.filoghost.chestcommands.api.MenuView;
import me.filoghost.chestcommands.attribute.PositionAttribute;
import me.filoghost.chestcommands.config.Lang;
import me.filoghost.chestcommands.logging.Errors;
import me.filoghost.chestcommands.parsing.icon.AttributeType;
import me.filoghost.chestcommands.parsing.icon.IconSettings;
import me.filoghost.chestcommands.util.Text;
import me.filoghost.fcommons.collection.CollectionUtils;
import me.filoghost.fcommons.logging.ErrorCollector;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

public class InternalMenu extends BaseMenu {

    private final Path sourceFile;
    private final String openPermission;

    private ImmutableList<Action> openActions;
    private ImmutableList<IconSettings> iconSettingsList;
    private int refreshTicks;

    public InternalMenu(@NotNull String title, int rows, @NotNull Path sourceFile) {
        super(title, rows);
        this.sourceFile = sourceFile;
        this.openPermission = Permissions.OPEN_MENU_PREFIX + sourceFile.getFileName();
    }

    public @NotNull Path getSourceFile() {
        return sourceFile;
    }

    public void setOpenActions(List<Action> openAction) {
        this.openActions = CollectionUtils.newImmutableList(openAction);
    }

    public void setIconSettingsList(List<IconSettings> iconSettingsList) {
        this.iconSettingsList = CollectionUtils.newImmutableList(iconSettingsList);
    }

    public void rebuildConfiguredIcons(ErrorCollector errorCollector) {
        if (iconSettingsList == null) {
            return;
        }

        clearIcons();
        for (IconSettings iconSettings : iconSettingsList) {
            addConfiguredIcon(iconSettings, errorCollector);
        }
    }

    public void rebuildConfiguredIcons() {
        if (iconSettingsList == null) {
            return;
        }

        clearIcons();
        for (IconSettings iconSettings : iconSettingsList) {
            try {
                addConfiguredIcon(iconSettings, null);
            } catch (RuntimeException ignored) {
                // Dynamic positions can become invalid at runtime. Keep the current refresh quiet.
            }
        }
    }

    private void addConfiguredIcon(IconSettings iconSettings, ErrorCollector errorCollector) {
        PositionAttribute positionX = (PositionAttribute) iconSettings.getAttributeValue(AttributeType.POSITION_X);
        PositionAttribute positionY = (PositionAttribute) iconSettings.getAttributeValue(AttributeType.POSITION_Y);

        if (positionX == null || positionY == null) {
            return;
        }

        int firstRow;
        int lastRow;
        int firstColumn;
        int lastColumn;
        try {
            firstRow = positionY.getFirstPosition(getRows()) - 1;
            lastRow = positionY.getLastPosition(getRows()) - 1;
            firstColumn = positionX.getFirstPosition(getColumns()) - 1;
            lastColumn = positionX.getLastPosition(getColumns()) - 1;
        } catch (RuntimeException e) {
            if (errorCollector != null) {
                errorCollector.add(Errors.Menu.invalidAttribute(iconSettings, AttributeType.POSITION_X));
            }
            return;
        }

        boolean invalidPosition = false;

        if (firstRow < 0 || lastRow >= getRows()) {
            if (errorCollector != null) {
                errorCollector.add(
                        Errors.Menu.invalidAttribute(iconSettings, AttributeType.POSITION_Y),
                        "it must be between 1 and " + getRows());
            }
            invalidPosition = true;
        }
        if (firstColumn < 0 || lastColumn >= getColumns()) {
            if (errorCollector != null) {
                errorCollector.add(
                        Errors.Menu.invalidAttribute(iconSettings, AttributeType.POSITION_X),
                        "it must be between 1 and " + getColumns());
            }
            invalidPosition = true;
        }

        if (invalidPosition) {
            return;
        }

        for (int row = firstRow; row <= lastRow; row++) {
            for (int column = firstColumn; column <= lastColumn; column++) {
                if (errorCollector != null && getIcon(row, column) != null) {
                    errorCollector.add(Errors.Menu.iconOverridesAnother(iconSettings));
                }

                setIcon(row, column, iconSettings.createIcon());
            }
        }
    }

    public String getOpenPermission() {
        return openPermission;
    }

    public int getRefreshTicks() {
        return refreshTicks;
    }

    public void setRefreshTicks(int refreshTicks) {
        this.refreshTicks = refreshTicks;
    }

    @Override
    public @NotNull MenuView open(@NotNull Player player) {
        if (openActions != null) {
            for (Action openAction : openActions) {
                openAction.execute(player);
            }
        }

        return super.open(player);
    }

    @Override
    public Plugin getPlugin() {
        return ChestCommands.getInstance();
    }

    public void openCheckingPermission(Player player) {
        if (player.hasPermission(openPermission)) {
            open(player);
        } else {
            sendNoOpenPermissionMessage(player);
        }
    }

    public void sendNoOpenPermissionMessage(CommandSender sender) {
        String noPermMessage = Lang.get().no_open_permission;
        if (noPermMessage != null && !noPermMessage.isEmpty()) {
            Text.send(sender, noPermMessage.replace("{permission}", this.openPermission));
        }
    }

}
