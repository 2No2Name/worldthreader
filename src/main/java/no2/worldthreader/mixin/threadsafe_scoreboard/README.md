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
- 