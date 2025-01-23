Worldthreader 2.0.0 is the first release for Minecraft 1.21.4 and includes a massive refactor and extension of the
codebase.
Please report any issues you encounter to the issue tracker of worldthreader. As worldthreader likely comes with massive
mod compatibility issues, please do not report crashes and issues to other mods' issue trackers before confirming the
issue without worldthreader.

## Additions:

- Overhaul of interdimensional transport, support for passenger teleports
- Thread-safe handling of cross-dimensional ender pearls
- Avoidance of interdimensional player references created by changing dimensions
- Enhanced thread safety of scoreboard and scores, most commands
- Improved crash handling

## Fixes:

- Entity-id based wireless redstone

## Changes:

- Remove all entities create nether portals gamerule
- Improved system for automatically detecting and handling cross-dimensional accesses

## Known Issues

- Minor: Adding a player to the whitelist using a command block is not a threadsafe command. For now, use the chat or
  the server console to edit the whitelist.