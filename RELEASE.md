# ChestCommandsX 5.1.1

Fork of ChestCommands by CadsMC. This release adds more flexible menu commands, dynamic slots, and interactive item input support for Paper/Folia 1.21.x.

## Added

- Added support for sub-commands in `menu-settings.commands`, such as `shop 1` and `shop 2`.
- Added tab completion for dynamic menu sub-commands.
- Added `/cc sound <sound> [pitch] [volume]` for testing Minecraft sounds in-game.
- Added tab completion for `/cc sound` sound names.
- Added range support for `POSITION-X` and `POSITION-Y`, for example `3-4` and `1-9`.
- Added support for `MATERIAL: AIR` icons so empty slots can still run actions.
- Added `DRAG: true` to allow normal item movement in selected menu slots.
- Added drag placeholders:
  - `%drag_item%`
  - `%drag_item_amount%`
  - `{drag_item}`
  - `{drag_item_amount}`
- Added integer placeholders for dynamic menu values:
  - `%integer:{name}%`
  - `%integer_plus:{name}%`
  - `%integer_minus:{name}%`
  - `%integer_default:{name}%`
  - `%integer_reset:{name}%`
- Added public and private integer scopes:
  - `%public_integer:{name}%` is shared across players.
  - `%private_integer:{name}%` is stored per open menu view and resets when the menu is closed or changed.
- Added default integer values with `name=default`, for example `%private_integer:amount=1%` and `%public_integer:range_x=1-2%`.

## Changed

- Menu refresh now rebuilds dynamic configured icons in the same inventory instead of requiring the menu to close and reopen.
- Dynamic integer placeholders can be used in `AMOUNT`, `POSITION-X`, and `POSITION-Y`.
- `POSITION-X`, `POSITION-Y`, and `AMOUNT` clamp dynamic integer values to valid menu/item limits.
- Updated the default `example.yml` with examples for sub-commands, AIR actions, draggable slots, range positions, and integer controls.

## Notes

- Public integer values are in-memory and reset when the plugin/server restarts.
- Private integer values reset when the player closes the menu or moves to another menu.
