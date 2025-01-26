# TODOS
- Find a conceptually good solution for scoreboards
- Make it possible for users to find out when and why threads have to wait for the other worlds, i.e. what is not implemented in the mod yet
- Find a way to systematically determine whether there are thread-safety issues, including with other mods
- Run some tests to see if there is a performance benefit to using the mod (or is there always a fallback to serial execution in every tick)
- Avoid heavy usage of accesswidener

## Things to check
- Serverlevel.emptyTime is reset by teleports, probably slightly different from vanilla (off by one tick? does it matter -> not really)
- Check the list of all commands for possible cross-world access or writes to shared data
- Passenger / Non passenger /player / player passenger / non player / non player passenger:
  - ender pearls and command blocks requesting entities to teleport over from other dimensions
  - command blocks teleporting entities within other dimensions
  - command blocks teleporting entities within two other dimensions (all 3 worlds involved)

## Thread-safety

- Connections have a packet counter that is not threadsafe but it only used to display statistics

## Crashes

- No known ones

## Bugs

- Leashed entities going through a portal with the player behaves different from vanilla. When the player goes through the portal first, it fails to leash the mobs following the player.
- Ender pearls in dimensions different from the player slow down the game by waiting for exclusive world access to check
  if they should despawn
  - Workaround: `/gamerule enderPearlsVanishOnDeath false`

## Check consequences of fixes
- ServerPlayer is not newly created unless first time leaving the end
- Projectiles can have reference to ServerPlayer as owner
- FollowOwnerGoal of Wolves, Cats and Parrots keep a cross-dimensional reference in some cases, maybe also other goals affected.
- Thorns knockback from arrows that hit a thorns equipped zombie is interdimensional if the player was in the same dimension as the arrow when the arrow was spawned (maybe other conditions, but spawned is enough)
