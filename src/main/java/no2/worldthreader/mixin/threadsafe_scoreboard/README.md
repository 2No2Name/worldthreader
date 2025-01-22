# Thread-safe Scoreboard Concept

## Considerations

- The scoreboard is shared server-wide
- Entities (players and non-players) can have scores
- Each entity can have one score for each Objective
- Some Objectives require scores to be updated on certain events, e.g. the blocks mined objective when a player mines a
  block
- Scores are integers that support get, set, arithmetic operations and swapping on two scores
- For each combination of objective and entity there can be one score
- All scores belonging to an objective can be displayed, requiring update propagation
- Teams are sets of Entities and have certain properties, e.g. whether friendly fire is allowed

## Implementation Decisions

- Teams: Make each write operation (add to team, remove from team, change team color, change team ally, etc.) require
  exclusive access. This may slow down the tick that the operation is performed in. However, in normal gameplay, teams
  are used rarely. This implementation choice allows fast scoreboard team membership reads during the multithreading.
  This is crucial as many mobs cannot attack members of their own scoreboard team, which requires checking their own
  and possibly the opponent's scoreboard team.
- Objectives: Adding / Removing / Modifying objectives requires exclusive world access. These operations are very rare.
- Entity scores:
  - Adding / Removing scores, e.g. entities are removed from the world, is handled by using a ConcurrentHashMap. This
    might lead to wrong results if the same score is added / removed from multiple worlds, but that should be rare and
    no big issue if it ever happens.
  - Changing an existing score is handled as atomic operation or a non-atomic combination of atomic operations. This
    is a middle ground between fast scoreboard operations and meaningful semantics of the operations. If the score is
    modified from multiple dimensions, the order of the operations is not known, but there is no interleaving
    for the supported atomic operations. However, it is still not recommended to use the same scores with command
    blocks / command block minecarts from multiple dimensions in the same tick. Otherwise, some operations
    (e.g. swapping two score values) may lead to wrong results.