# Path Travel Agent

A path matcher. Well suited for HTTP, but also works with any request/response system.

*Only* does paths, *not* method/verb, content-type, etc.

Made to be embedded by other engines that wants fast and type-safe request/response path matching.

## Installing

Install from maven central.

```xml
<dependency>
  <groupId>com.augustl</groupId>
  <artifactId>pathtravelagent</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Examples

There's a number of unit tests that showcases the various ways to build routes and match them.

https://github.com/augustl/path-travel-agent/blob/master/src/test/java/com/augustl/pathtravelagent/PathTravelAgentTest.java

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

This is because the routes is stored in a bastardized radix tree. Traditionally, routers (spark included) stores the routes in an array. This means linear worst case performance. A radix trie exploits the fact that most URLs share a structure (for example, /people, /people/$personId and /people/$personId/friends), and yields O(log N) performance.

The bastardized radix tree is a normal radix tree doesn't contain "parametric" nodes. When we have routes like `/people/:person-id` we can't store the dynamic parameter :person-id in the tree. Let's say we have the following routes:

* `/people/important`
* `/people/:person-id`
* `/people/:wat-id`

A request to `"/people/important" will match the first route. A request to "/people/123" cannot be routed using the radix tree, so each node has a list of "parametric" nodes that will be called linearly in order of appearance.

[My benchmark](https://github.com/augustl/path-travel-agent/blob/ffe911e8cdb8eefa6ff0a706642dffc8d4a8ed75/src/test/java/com/augustl/pathtravelagent/PathTravelAgentBenchmark.java) is quite possibly naive, and is deliberatly constructed to make array-based routers look bad. Better benchmarks are welcome :)

## License

3-clause BSD License

## Copyright

Copyright 2014, August Lilleaas
