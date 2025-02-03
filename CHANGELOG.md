Worldthreader 2.0.4 for Minecraft 1.21.4 fixes a crash and a minor thread safety issue.
Please report any issues you encounter to the [issue tracker of worldthreader](https://github.com/2No2Name/worldthreader/issues). As worldthreader likely comes with massive mod compatibility issues, please do not report crashes and issues to other mods' issue trackers before confirming the issue without worldthreader.

## Fixes:

- Fix crash when player with potion effect changes dimension
- Add thread-safety to iron golem, snow golem, end portal and wither block patterns used in spawning

## Known issues

- When worldthreader is disabled, a player holding a leash going through a portal before the leashed entity causes the leash to break.
