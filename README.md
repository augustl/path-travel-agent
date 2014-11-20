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
  <version>0.1.1</version>
</dependency>
```

## API documentation

The javadoc is hosted here:

http://docs.augustl.com/com.augustl.pathtravelagent/0.1.1/

## Examples

There's a number of unit tests that showcases the various ways to build routes and match them.

https://github.com/augustl/path-travel-agent/blob/master/src/test/java/com/augustl/pathtravelagent/PathTravelAgentTest.java

## Example of integration

There's a runnable test that demonstrates how to integrate path-travel-agent in a HTTP environment. You can find it here:

https://github.com/augustl/path-travel-agent/blob/master/src/test/java/com/augustl/pathtravelagent/HttpExampleTest.java

## Benchmark

Path Travel Agent is *very* fast.

```
PathTravelAgentBenchmark.largeRouter: [measured 10 out of 15 rounds, threads: 1 (sequential)]
 round: 0.04 [+- 0.01], round.block: 0.00 [+- 0.00], round.gc: 0.00 [+- 0.00], \
 GC.calls: 2, GC.time: 0.02, time.total: 0.74, time.warmup: 0.37, time.bench: 0.37

PathTravelAgentBenchmark.sparkRouter: [measured 10 out of 15 rounds, threads: 1 (sequential)]
 round: 0.66 [+- 0.03], round.block: 0.00 [+- 0.00], round.gc: 0.00 [+- 0.00], \
 GC.calls: 11, GC.time: 0.02, time.total: 10.30, time.warmup: 3.72, time.bench: 6.58
```

path-travel-agent spends 0.37 seconds, while [spark](https://github.com/perwendel/spark/) spends 6.58 seconds.

[My benchmark](https://github.com/augustl/path-travel-agent/blob/ffe911e8cdb8eefa6ff0a706642dffc8d4a8ed75/src/test/java/com/augustl/pathtravelagent/PathTravelAgentBenchmark.java) is quite possibly naive, and is deliberatly constructed to make array-based routers look bad. Better benchmarks are welcome :)

## How it works

Most routing libraries, such as Spark and [Express](http://techblog.netflix.com/2014/11/nodejs-in-flames.html), is based on an array of regular expressions. This has a performance characteristic O(N), as every regexp in the system has to be tested against the path to be matched.

path-travel-agent uses a bastardized radix tree. The routes are represented as a tree of hash maps. Each node has a handler, a hash map of known sub-nodes, and a single parameterized node.

Let's assume the following route set

* `/`
* `/people`
* `/people/important`
* `/people/:person-id`
* `/people/:person-id/friends`
* `/people/:person-id/friends/:friend-id`
* `/about`

path-travel-agent will store these routes as:

    {
      handler: IRouteHandler,
      pathSegmentChildNodes: {
        "people": {
            handler: IRouteHandler,
            parametricChild: {
                paramName: "person-id",
                handler: IRouteHandler,
                pathSegmentChildNodes: {
                    "friends": {
                        handler: IRouteHandler,
                        parametricChild: {
                            paramName: "friend-id",
                            handler: IRouteHandler
                        }
                    }
                }
            },
            pathSegmentChildNodes: {
                "important": {handler: IRouteHandler}
            },
        }
        "about": {
            handler: IRouteHandler
        }
      }
    }
    
When matching, the flow is:

* The path `/people/1/friends/2` will get parsed into `["people", "1", "friends", "2"]`.
* We ask the root node to recognize "people".
* We look up "people" in `pathSegmentChildNodes`.
* We find a match.
* We ask that child node to route "1"
* We look up "1" in pathSegmentChildNodes
* Nothing was found. We call the `parametricChild`
* We ask that parametric child node (which is just a regular node) to recognize "friends"
* We find "friends" in `pathSegmentChildNodes`
* We ask that child node to recognize "2"
* It does not find any match in `pathSegmentChildNodes`, so we ask the `parametricChild`

One operation per path segment in a tree structure, is much more efficient than looping through an array of tens or hundreds of routes and matching with a regexp.

The reason this is called a "bastardized radix tree" is that it's not really a radix tree, it's just nested hash maps. Also, the parametricChild makes it even less of a radix tree.

## License

3-clause BSD License

## Copyright

Copyright 2014, August Lilleaas
