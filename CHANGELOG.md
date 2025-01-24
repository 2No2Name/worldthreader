Worldthreader 2.0.1 for Minecraft 1.21.4 fixes a few thread-safety issues and adds a new Debug gamerule.
Please report any issues you encounter to
the [issue tracker of worldthreader](https://github.com/2No2Name/worldthreader/issues). As worldthreader likely comes
with massive mod compatibility issues, please do not report crashes and issues to other mods' issue trackers before
confirming the issue without worldthreader.

## Additions:

- Thread safety for commands related to scheduled events, whitelist, bans etc.
- Debug gamerule to detect both performance and correctness issues. However, there is no guarantee that all issues are
  found.

## Fixes:

- Fix ticking time, weather and sleeping players order by updating them in the overworld before updating them in the
  other dimension
- Fix non-world threads being able to request exclusive world access during world tick incorrectly
- Fix F3 debug screen unsafely accessing the server world in singleplayer by not showing that information
- Fix Server console unsafely accessing the overworld to get the spawn position by delaying the access

## Known issues:

- Leashed entities going through a portal with the player behaves different from vanilla. When the player goes through
  the portal first, it fails to leash the mobs following the player.
  - Workaround: Let the leashed entities go through the portal first and go through the portal within 5 seconds.
- Ender pearls in dimensions different from the player slow down the game by waiting for exclusive world access to check
  if they should despawn
  - Workaround: `/gamerule enderPearlsVanishOnDeath false`
