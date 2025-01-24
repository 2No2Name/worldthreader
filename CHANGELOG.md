Worldthreader 2.0.1 for Minecraft 1.21.4 adds thread-safety for commands it was not implemented for previously.
Please report any issues you encounter to
the [issue tracker of worldthreader](https://github.com/2No2Name/worldthreader/issues). As worldthreader likely comes
with massive mod compatibility issues, please do not report crashes and issues to other mods' issue trackers before
confirming the issue without worldthreader.

## Additions:

- Thread safety for commands related to scheduled events, whitelist, bans etc.

## Fixes:

- Ticking time, weather and sleeping players order ensured by updating in overworld before updating in the other
  dimension
- Using the server console accessed the overworld
- Non-world threads could incorrectly request exclusive world access during world tick

## Known issues:

- Leashed entities going through a portal with the player behaves different from vanilla. When the player goes through
  the
  portal first, it fails to leash the mobs following the player.
  - Workaround: Let the leashed entities go through the portal first and go through the portal within 5 seconds.
- Ender pearls in dimensions different from the player slow down the game by waiting for exclusive world access to check
  if they should despawn
  - Workaround: `/gamerule enderPearlsVanishOnDeath false`
