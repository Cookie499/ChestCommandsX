ChestCommands
===================

Modernized for Paper/Folia 1.21.x and Java 21.

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

The plugin jar is created at `plugin/build/libs/chestcommands-plugin-5.0.0.jar`.

## Gradle
```groovy
repositories {
    maven { url = uri("https://repo.codemc.io/repository/maven-public/") }
}
```

```groovy
dependencies {
    compileOnly "me.filoghost.chestcommands:chestcommands-api:5.0.0"
}
```

## Configuration Notes
- Use modern material names such as `DIAMOND_SWORD`, `WHITE_WOOL`, and `REDSTONE_LAMP`.
- Commands under `menu-settings.commands` are dynamically registered. Use `menu`, not `/menu`, and avoid names already used by other plugins.
- Folia is declared as supported. Player inventory/menu work is scheduled on the player entity scheduler, while global tasks use Folia's global scheduler. The legacy update checker and bStats scheduler hooks are disabled on Folia.
- Use `DAMAGE` for item damage. Legacy material data values like `WOOL:14` are not supported.
- `NBT-DATA` is no longer supported. Use supported metadata keys such as `COLOR`, `SKULL-OWNER`, `ENCHANTMENTS`, `CUSTOM-MODEL-DATA`, `UNBREAKABLE`, and `ITEM-FLAGS`.
- `boss-bar:` uses the native Bukkit boss bar API. The old `dragon-bar:` prefix still works as an alias.

## License
Chest Commands is free software/open source, and is distributed under the [GPL 3.0 License](https://opensource.org/licenses/GPL-3.0). It contains third-party code, see the included THIRD-PARTY.txt file for the license information on third-party code.
