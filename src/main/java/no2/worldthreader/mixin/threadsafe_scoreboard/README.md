# Thread-safe Scoreboard Concept

## Implementation Choices

### Possible ways
- Acquire scoreboard exclusive access until tick end
- Make each scoreboard objective exclusively acquirable until tick end
- For each objective make each player's score exclusively acquirable until tick end
- Make each operation (e.g. add 1) atomic
- Scoreboard teams are queried often in collision / target calculations of mobs

## Implementation Decisions

- Teams: Make each write operation (add to team, remove from team, change team color, change team ally, etc.) require
  exclusive access. This may slow down the tick that the operation is performed in. However, in normal gameplay, teams
  are used rarely. This implementation choice allows fast scoreboard team membership reads during the multithreading.
  This is crucial as many mobs cannot attack members of their own scoreboard team, which requires checking their own
  and the opponent's scoreboard team.
- Objectives: Adding / Removing / Modifying objectives requires exclusive world access. These operations are very rare.
- Entity scores:
  - Adding / Removing scores is handled by using a ConcurrentHashMap
  - Changing an existing score is handled as atomic operation or a non-atomic combination of atomic operations. This
    is a middle ground between fast scoreboard operations and meaningful semantics of the operations. It is not
    recommended to use the same scores with command blocks / command block minecarts from multiple dimensions in the
    same tick. Otherwise, some operations (e.g. swapping two score values) may lead to wrong results.
- Sending updates after modification: This seems broken at the moment, unclear why. However, this seems to be a visual
  issue only.