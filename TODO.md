# TODOS
- Find a conceptually good solution for scoreboards
- Make it possible for users to find out when and why threads have to wait for the other worlds, i.e. what is not implemented in the mod yet
- Find a way to systematically determine whether there are thread-safety issues, including with other mods
- Run some tests to see if there is a performance benefit to using the mod (or is there always a fallback to serial execution in every tick)

## Update Mod to newer versions
- Check tickrate manager -> probably fine, if it is broken the game never recover as soon as tick freeze is used
- Serverlevel.emptyTime is reset by teleports, probably slightly different from vanilla (off by one tick? does it matter -> not really)

## Thread-safety
- Ender pearls, everything about them
- tick world border? world border commands -> DelegateBorderChangeListener
- does cross world player pet teleportation exist?
- calculatePassengerTransition - probably done already?
- LevelData / Derived LevelData, e.g. command block using /difficulty
- Scoreboard uses broadcastAll -> player list is not threadsafe to access -> make player list threadsafe!
- All levels use broadcastAll during weather tick ???

## Check consequences of fixes
- ServerPlayer is not newly created unless first time leaving the end
- Projectiles can have reference to ServerPlayer as owner
- FollowOwnerGoal of Wolves, Cats and Parrots keep a cross-dimensional reference in some cases, maybe also other goals affected.
- Thorns knockback from arrows that hit a thorns equipped zombie is interdimensional if the player was in the same dimension as the arrow when the arrow was spawned (maybe other conditions, but spawned is enough)
