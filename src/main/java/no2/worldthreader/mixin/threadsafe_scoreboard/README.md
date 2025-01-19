# Thread-safe Scoreboard Concept

## Implementation Choices

### Possible ways
- Acquire scoreboard exclusive access until tick end
- Make each scoreboard objective exclusively acquirable until tick end
- For each objective make each player's score exclusively acquirable until tick end
- Make each operation (e.g. add 1) atomic

## Implementation Decision

TODO IMPLEMENT