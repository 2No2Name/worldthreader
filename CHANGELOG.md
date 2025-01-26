Worldthreader 2.0.2 for Minecraft 1.21.4 fixes a few teleportation issues.
Please report any issues you encounter to the [issue tracker of worldthreader](https://github.com/2No2Name/worldthreader/issues). As worldthreader likely comes with massive mod compatibility issues, please do not report crashes and issues to other mods' issue trackers before confirming the issue without worldthreader.

## Additions:


## Fixes:

- Fix debug gamerule incorrectly warning about world generation worker threads
- Fix using ender pearls causing crashes
- Fix teleporting a boat with a player passenger across dimensions with a command block causing crashes
- Fix more teleportation bugs


## Known issues:

- Leashed entities going through a portal with the player behaves different from vanilla. When the player goes through
  the portal first, it fails to leash the mobs following the player.
  - Workaround: Let the leashed entities go through the portal first and go through the portal within 5 seconds.
- Ender pearls in dimensions different from the player slow down the game by waiting for exclusive world access to check
  if they should despawn
  - Workaround: `/gamerule enderPearlsVanishOnDeath false`
