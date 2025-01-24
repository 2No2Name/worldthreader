# Worldthreader Mod

This project is a free and open-source Minecraft mod which optimizes the processing of multiple dimensions by ticking
dimensions in parallel. Thread safety is ensured with fallbacks to serial execution if no other solution can be found.
After each tick, the dimensions wait for each other to ensure that everything stays in sync, avoiding breaking many
advanced redstone contraptions. Mod incompatibilities are expected, please report any issues to
the [worldthreader issue tracker](https://github.com/2No2Name/worldthreader/issues).

The mod works on both the **client (singleplayer) and server**, and **does not** require the mod to be installed on both
sides.

## Installation

The mod must be installed on the server to work in multiplayer. For use in singleplayer worlds, the mod must be
installed on the client.

1. Download the mod from [Modrinth](https://modrinth.com/mod/worldthreader)
   or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/worldthreader).
2. Place the downloaded `.jar` file into the `mods` folder of your Minecraft directory.
3. Launch Minecraft with the Fabric mod loader.

### Configuration

To use the gamerules, you need the Fabric API. Without the Fabric API, Worldthreader will use the default settings.

- `/gamerule worldthreader_Active <true/false>` (default true) enables/disables the mod
- `/gamerule worldthreader_AdditionalEntityTickAfterTeleport <true/false>` (default false) enables/disables ticking
  entities immediately after teleporting from the main overworld to the nether or end to simulate the timings of vanilla
  portal use
- `/gamerule worldthreader_Debug <true/false>` (default false) enables/disables debug logging and illegal world access
  detection. Use this to find issues, both correctness and performance related. If nothing shows up in the logs, that
  only means that no issues were found, not that none can exist.

---

### FAQ

##### Does the mod change Vanilla behaviour?

Worldthreader aims to conserve vanilla-parity in most points.

Behavior based on the timing of entities going through portals might be delayed by a game tick when the entity is
leaving the overworld. However, `/gamerule worldthreader_AdditionalEntityTickAfterTeleport` ticks entities that are not
teleporting to the overworld once after being placed in the world. This should allow them to catch up on the tick they
missed. However, this happens at the end of the tick, meaning that no other mobs or pistons were able to push or damage
the entity in that tick because it hasn't been there yet.

Interdimensional commands in command blocks or shared scoreboard accesses from different dimensions can be observed to
be in a different order since there are no interdimensional ordering guarantees within a single game tick for
interdimensional commands. The order of commands within one dimension remains the same as in vanilla.

##### Can my dimensions get de-synchronized?

No. Worldthreader will always synchronize the dimensions with each other, setting the overall MSPT to that of the
slowest individual dimension.

##### Are dimension counts above 3 supported?

Yes.

##### Can I use Worldthreader if I have fewer threads on my CPU than dimensions?

Yes, but that may reduce the performance.

##### How is the compatibility with other mods?

Compatibility issues with other mods may be very common. Please report all issues you encounter to Worldthreader
directly to avoid giving other mod authors headaches. The author of Worldthreader is willing to cooperate and suggest
changes to other mods for compatibility.

##### What about older versions of Minecraft?

There is the DimThread mod which supports a few older versions: https://github.com/WearBlackAllDay/DimensionalThreading

---

### License

Worldthreader is licensed under the MIT License, a free and open-source license. For more information, please read
the [license file](LICENSE).