ChestCommandsXL
===================

Fork of ChestCommandsX, modernized for 1.21+ minimessage and Components.

API Javadoc for developers: https://ci.codemc.io/job/filoghost/job/ChestCommands/javadoc/index.html?me/filoghost/chestcommands/api/ChestCommandsAPI.html

## Requirements
- Paper, Folia, or a compatible Bukkit implementation based on Minecraft 1.21.x or newer.
- Java 21 or newer.
- Vault is optional and only required for economy features.
- PlaceholderAPI is optional and only required for external placeholders.

## Build
```powershell
gradle build
```

The plugin jar is created at `plugin/build/libs/chestcommands-plugin-6.0.1.jar`.

## Gradle
```groovy
repositories {
    maven { url = uri('https://nmb-court-team.github.io/Maven/repository') }
}
```

```groovy
dependencies {
    compileOnly 'net.astrorbits.plugin:chestcommands-api:6.0.0'
}
```

## Kotlin DSL

Kotlin plugins can use the optional `chestcommands-kotlin` artifact:

```kotlin
dependencies {
    compileOnly("net.astrorbits.plugin:chestcommands-kotlin:6.0.1")
}
```

```kotlin
val shop = plugin.menu(Component.text("Shop"), rows = 3) {
    icon(row = 1, column = 4, material = Material.DIAMOND) {
        name = "<aqua>Diamond"
        lore("<gray>Click to buy")
        amount = 3
        placeholders = true
        onClick { view, player ->
            player.sendMessage(Component.text("Purchased!"))
            view.refresh()
        }
    }
}
```

Rows, columns, and linear slots are zero-based, like the Java API. The DSL artifact
is separate from the plugin jar, so server owners do not need the Kotlin runtime.

## Configuration Notes
- Use modern material names such as `DIAMOND_SWORD`, `WHITE_WOOL`, and `REDSTONE_LAMP`.
- Commands under `menu-settings.commands` are dynamically registered. Use `menu`, not `/menu`, and avoid names already used by other plugins. Sub-commands such as `shop 1` are supported.
- Use `/cc sound <sound> [pitch] [volume]` to test available Minecraft sounds in-game. The sound argument supports tab completion.
- Folia is declared as supported. Player inventory/menu work is scheduled on the player entity scheduler, while global tasks use Folia's global scheduler. The legacy update checker and bStats scheduler hooks are disabled on Folia.
- Use `DAMAGE` for item damage. Legacy material data values like `WOOL:14` are not supported.
- `NBT-DATA` is no longer supported. Use supported metadata keys such as `COLOR`, `SKULL-OWNER`, `ENCHANTMENTS`, `CUSTOM-MODEL-DATA`, `UNBREAKABLE`, and `ITEM-FLAGS`.
- `boss-bar:` uses the native Bukkit boss bar API. The old `dragon-bar:` prefix still works as an alias.
- `POSITION-X` and `POSITION-Y` support ranges such as `3-4` and dynamic integer placeholders.
- `MATERIAL: AIR` can be used for invisible clickable slots, and `DRAG: true` allows a slot to accept normal item movement.

## License
Chest Commands is free software/open source, and is distributed under the [GPL 3.0 License](https://opensource.org/licenses/GPL-3.0). It contains third-party code, see the included THIRD-PARTY.txt file for the license information on third-party code.
