# IFDS-RA
Concurrent implementation of the IFDS [1] algorithm based on Reactive Async [2]

## Dependencies
- [Reactive Async](https://github.com/phaller/reactive-async)
- [Heros](https://github.com/Sable/heros)
- [FlowTwist](https://github.com/johanneslerch/FlowTwist)

## How to run experimental evaluation
```
> sbt
> run ifds-solver static-analysis-lib taint-analysis num-threads path-to-jre
```

Argument | Options
--- | ---
__ifds-solver__ | "ra" or "heros"
__static-analysis-lib__ | "soot" (OPAL currently not supported)
__taint-analysis__ | "fw", "bidi", "gen", or "seq"
__num-threads__ | number of threads to use for the IFDS solver. The number of threads used by SOOT is not affected by this number.
__path-to-jre__ | Path to JRE folder

Taint analysis explanation:
- seq is the forwards IFDS algorithm on the Class.forName experimental setup in FlowTwist
- fw is the forwards IFDS algorithm on the Class.forName experimental setup in FlowTwist
- bidi is is the bidirectional IFDS algorithm on the Class.forName experimental setup in FlowTwist
- gen is the bidirectional IFDS algoirthm on the GenericCallerSensitive experimental setup in FlowTwist

Currently the supported taint analyses are:
- ra soot seq _(does not actually use RA, should probably be rewritten to "seq soot fw" which better reflects which analysis is run)_
- ra soot fw
- ra soot bidi
- ra soot gen
- heros soot fw
- heros soot bidi
- heros soot gen

These arguments and which analysis is run is controlled by `src/main/java/flow/twist/mains/Starter.java` which is the main class of the project.

## How to run tests
Run all tests:
```
> sbt
> test
```
or, for each individual test suite:
```
> sbt
> test-only IFDS.SimpleTestNoOPAL
> test-only IFDS.SimpleTestRANoOPAL
> test-only IFDS.SimpleTestBiDiNoOPAL
> test-only IFDS.SimpleTestBiDiRANoOPAL
```
where
- SimpleTestNoOPAL is the sequential IFDS solver
- SimpleTestRANoOPAL is the RA IFDS solver
- SimpleTestBiDiNoOPAL is the sequential bidirectional IFDS solver
- SimpleTestBiDiRANoOPAL is the bidirectional RA IFDS solver

### References
[1] http://dl.acm.org/citation.cfm?id=199462

[2] http://dl.acm.org/citation.cfm?id=2998396

