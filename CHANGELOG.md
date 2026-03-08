Worldthreader 2.6.1 for Minecraft 1.21.10 backports several fixes and compatibility improvements from 1.21.11 releases.
Please report any issues you encounter to the [issue tracker of worldthreader](https://github.com/2No2Name/worldthreader/issues). As worldthreader likely comes with massive mod compatibility issues, please do not report crashes and issues to other mods' issue trackers before confirming the issue without worldthreader.

## Fixes

- Fix a barrier synchronization issue that could leave waiting threads parked.
- Fix fishing rod owner and state being lost during dimension changes.
- Work around "Player swapping is already happening!" (issue #44).
- Preserve teleported entity movement relation using `PositionMoveRelation` in `TeleportedEntityInfo`.

## Changes

- Improve compatibility with mods that add and remove worlds dynamically (for example `mc-worlds`).
- Improve compatibility with alternative `DerivedLevelData` implementations.
- Handle cross-world entity references with fewer exclusive access requests.
- Move post-teleport additional tick handling into a dedicated tick phase.
