Worldthreader 3.0.0 for 26.1.x introduces a new system for yielding level access. Expect more issues compared to previous releases, since this change has not been tested a lot.
Please report any issues you encounter to the [issue tracker of worldthreader](https://github.com/2No2Name/worldthreader/issues). As worldthreader likely comes with massive mod compatibility issues, please do not report crashes and issues to other mods' issue trackers before confirming the issue without worldthreader.


## Changes
- Allow non-level threads to acquire exclusive level access
- Add threading safe point where level threads will yield access to threads waiting for exclusive level access
- Improve crash handling, print which threads are not finishing gracefully