# Design

## Threading
Each dimension has its own thread. There are multiple synchronization points to keep the behavior as in vanilla.
At the end of each tick dimensions wait for each other to finish processing and exchange teleported entities safely.

## Interdimensional Teleportation
As in vanilla, entities disappear when they teleport. In contrast to vanilla, the entities don't appear in the destination dimension immediately, but only at the end of the current tick.

## Scoreboard
Threadsafe scoreboard is not implemented at this time, which is a major issue.

## Non Vanilla Behaviors
All of these are very niche and are unlikely to affect players and most technical contraptions. However, this document tries to achieve high transparency and list all changes.
- Server side players are recreated when changing dimensions to avoid many thread-safety issues, e.g. where mobs or projectiles keep a reference to the player entity even after switching dimensions.
  - In vanilla this manifests in a few behaviors, all of worldthreader breaks:
    - Arrows shot by a player that switched dimensions after shooting make the player take thorns damage when hitting a thorns equipped mob. (Tested in 1.21.4)
    - Dogs (and possibly other pets like cats and parrots) can follow the coordinates of the player that switched dimensions (https://www.youtube.com/watch?v=cWOVszGRc9E)
    - Tridents with loyalty follow the player's coordinates after switching dimensions (not tested)
    - Mob AI that targets a player that switched dimensions can still target the player after switching dimensions (https://youtu.be/v7bn6lvCDX8?t=205)

In the following, entities means non-player entities. In vanilla, players teleport outside the world tick, avoiding potential thread safety issues already.

- Entities that switch dimension via portal arrive at the end of the tick for thread safety reasons. This might affect contraptions that depend on precise timings of switching dimensions.
- Entities that are teleported to another dimension via command blocks or command block minecarts similarly arrive at the end of the tick. This of course also affects other commands which might try to access the entity immediately after the teleport command is executed.
- Entities that fail to teleport to another dimension due to a missing destination portal or similar are first removed from the world and then added back to the world at the end of the tick after successfully teleported entities arrived. This might affect several game mechanics, but failed teleport attempts are rare as since Minecraft 1.21 all entities that can teleport through nether portals can create nether portals.
- Vanilla's dispenser bug where the dispenser failure sound is played incorrectly when using a brush on an armadillo after a dispenser failed to brush an armadillo behaves differently, as the condition "after a dispenser failed to brush an armadillo" is evaluated per-dimension with worldthreader.