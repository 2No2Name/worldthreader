# Design

## Threading
Each dimension has its own thread. There are multiple synchronization points to keep the behavior as in vanilla.
At the end of each tick dimensions wait for each other to finish processing and exchange teleported entities safely.

The field Entity#level is treated as effectively final for entities that have been added to worlds, allowing unsynchronized reads.
The server player teleportation code is modified such that the level field is not modified.

## Interdimensional Teleportation
As in vanilla, entities disappear when they teleport. In contrast to vanilla, the entities don't appear in the destination dimension immediately, but only at the end of the current tick.

## Scoreboard

Threadsafe implementation described
in [README.md](src/main/java/no2/worldthreader/mixin/threadsafe_scoreboard/README.md)

## Non Vanilla Behaviors
All of these are very niche and are unlikely to affect players and most technical contraptions. However, this document tries to achieve high transparency and list all changes.
- Server side players are recreated when changing dimensions to avoid many thread-safety issues, e.g. where mobs or projectiles keep a reference to the player entity even after switching dimensions.
  - In vanilla this manifests in a few behaviors, all of which worldthreader breaks:
    - Arrows shot by a player that switched dimensions after shooting make the player take thorns damage when hitting a thorns equipped mob. (Tested in 1.21.4)
    - Dogs (and possibly other pets like cats and parrots) can follow the coordinates of the player that switched dimensions (https://www.youtube.com/watch?v=cWOVszGRc9E)
    - Tridents with loyalty follow the player's coordinates after switching dimensions (not tested)
    - Mob AI that targets a player that switched dimensions can still target the player after switching dimensions (https://youtu.be/v7bn6lvCDX8?t=205)
- Interdimensional ender pearls and leads may react the death of the player one tick later than in vanilla

- Entities that switch dimension via portal arrive at the end of the tick for thread safety reasons. This might affect contraptions that depend on precise timings of switching dimensions.
- Entities that are teleported to another dimension via command blocks or command block minecarts similarly arrive at the end of the tick. This of course also affects other commands which might try to access the entity immediately after the teleport command is executed.
- Entities that fail to teleport to another dimension due to a missing destination portal or similar are first removed
  from the world and then added back to the world at the end of the tick after successfully teleported entities arrived.
  This might affect several game mechanics, but failed teleport attempts are rare, as since Minecraft 1.21 all entities
  that can teleport through nether portals can create nether portals.
- Vanilla's dispenser bug where the dispenser failure sound is played incorrectly when using a brush on an armadillo after a dispenser failed to brush an armadillo behaves differently, as the condition "after a dispenser failed to brush an armadillo" is evaluated per-dimension with worldthreader.

## Considerations

Several commands are made thread-safe for use in command blocks and command block minecarts. Entering the command in
chat or in the server console does not happen during the world ticking, therefore no additional synchronization is
needed.

- Setting time requires exclusive world access
- Setting gamerules requires exclusive world access
- Setting world border requires exclusive world access
- Setting weather requires exclusive world access
- Using the schedule command requires exclusive world access
- Changing the whitelist, banlist, oplist requires exclusive world access

## Observations

- DimensionDataStorage is per-dimension in vanilla, but maps and some others are tied to overworld
  - Crafter block scaling or locking a map in the nether or end takes exclusive world access
- Network connections have a non-threadsafe integer counter for tracking network statistics
  - Currently still non-threadsafe in worldthreader, leading to possibly wrong packet counts being displayed
- WardenAi.DIG_COOLDOWN_SETTER and ARMADILLO_ROLLING_OUT are not threadsafe, but should not cause any issues.

## Vanilla issues fixed

- Fix F3 debug screen unsafely accessing the server world in singleplayer by not showing that information
- Fix Server console unsafely accessing the overworld to get the spawn position by delaying the access