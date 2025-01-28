Worldthreader 2.0.2 for Minecraft 1.21.4 fixes a few teleportation issues.
Please report any issues you encounter to the [issue tracker of worldthreader](https://github.com/2No2Name/worldthreader/issues). As worldthreader likely comes with massive mod compatibility issues, please do not report crashes and issues to other mods' issue trackers before confirming the issue without worldthreader.

## Additions:

- Add threadsafe dead player check for leads and ender pearls

## Fixes:

- Fix debug gamerule incorrectly warning about world generation worker threads
- Fix using ender pearls causing crashes
- Fix teleporting a boat with a player passenger across dimensions with a command block causing crashes
- Fix more teleportation bugs
- Fix leads detaching when using portals
- Fix ender pearls ticking in different dimension than the player requiring exclusive world access

## Known issues

- When worldthreader is disabled, a player holding a leash going through a portal before the leashed entity causes the leash to break.
