# Path Travel Agent

A path matcher. Well suited for HTTP, but also works with any request/response system.

*Only* does paths, *not* method/verb, content-type, etc.

Made to be embedded by other engines that wants fast and type-safe request/response path matching.

## Installing

Install from maven central.

```xml
<dependency>
  <groupId>com.augustl</groupId>
  <artifactId>path-travel-agent</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Example of integration

There's a runnable test that demonstrates how to integrate path-travel-agent in a HTTP environment. You can find it here:

https://github.com/augustl/path-travel-agent/blob/master/src/test/java/com/augustl/pathtravelagent/HttpExampleTest.java

## API documentation

The javadoc is hosted here:

http://docs.augustl.com/com.augustl.pathtravelagent/0.1.0/

## Performance

Path Travel Agent is *very* fast.

```
PathTravelAgentBenchmark.largeRouter: [measured 10 out of 15 rounds, threads: 1 (sequential)]
 round: 0.04 [+- 0.01], round.block: 0.00 [+- 0.00], round.gc: 0.00 [+- 0.00], \
 GC.calls: 2, GC.time: 0.02, time.total: 0.74, time.warmup: 0.37, time.bench: 0.37

PathTravelAgentBenchmark.sparkRouter: [measured 10 out of 15 rounds, threads: 1 (sequential)]
 round: 0.66 [+- 0.03], round.block: 0.00 [+- 0.00], round.gc: 0.00 [+- 0.00], \
 GC.calls: 11, GC.time: 0.02, time.total: 10.30, time.warmup: 3.72, time.bench: 6.58
```

We spend 0.37 seconds, while [spark](https://github.com/perwendel/spark/) spends 6.58 seconds.

This is because the routes is stored in a radix tree (almost). Traditionally, routers (spark included) stores the routes in an array. This means that we at worst have to look through all the paths to find a match. A trie exploits the fact that most URLs share a structure (for example, /people, /people/$personId and /people/$personId/friends). Going through the chunks (the stuff between the slashes) and only touching a chunk once is therefore much more efficient.

Why almost a radix tree? Some of the nodes are "parametric", i.e. they don't have a value, but are functions that have to be invoked in linear time for a step in a tree - it is possible to create both `/people/:person-id` and `/people/:wat-id`. These will be called in sequence by order of appearance.

[My benchmark](https://github.com/augustl/path-travel-agent/blob/ffe911e8cdb8eefa6ff0a706642dffc8d4a8ed75/src/test/java/com/augustl/pathtravelagent/PathTravelAgentBenchmark.java) is quite possibly naive, and is deliberatly constructed to make array-based routers look bad. Better benchmarks are welcome :)

## License

3-clause BSD License

## Copyright

Copyright 2014, August Lilleaas
