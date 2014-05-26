# Path Travel Agent

A path matcher. Well suited for HTTP, but also works with any request/response system.

*Only* does paths, *not* method/verb, content-type, etc.

Made to be embedded by other engines that wants fast and type-safe request/response path matching.

## Installing

Install from maven central. **Note**: current version is not actually present in maven central. It's still a unpublished snapshot release.

```xml
<dependency>
  <groupId>com.augustl</groupId>
  <artifactId>path-travel-agent</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Usage

### Generics for type safety

```java
 PathTravelAgent<MyReq, MyRes> pta;
```

An instance of `PathTravelAgent` needs to specify the type of both the request and response objects.

### The request object

`MyReq` has to implement `com.augustl.pathtravelagent.IRequest`.

```java
class MyReq implements IRequest {
    private final String path;
    // A bare bones request implementation
    public MyReq(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return this.path;
    }
}
```

`MyReq` can have any additional data you want to represent a request. In a HTTP scenario, you might pass in the verb/method and the value of the Accept-header.


```java
// A bunch of optional fields that you can use for whatever you want in your handlers.
class MyReq implements IRequest {
    private final String path;
    private final String method;
    private final String accept;
    public MyReq(String path, String method, String accept) {
        this.path = path;
        this.method = method;
        this.accept = accept;
    }

    @Override
    public String getPath() {
        return this.path;
    }
}
```

### The response object.

```java
class MyRes {
}
```

`MyRes` can be anything you want, we don't care. The generic is only there for type safety, and we return your response as-is, without any processing.

`MyRes` will probably contain information returned from calling business logic in your app, so you will probably have a field called `body` and `status` on it. But this is completely up to you.

### Creating a router

```java
PathTravelAgent<MyReq, MyRes> pta = PathTravelAgent.Builder.<MyReq,MyRes>start()
    .newRoute().buildRoute(homePageHandler)
    .newRoute().pathSegment("projects").buildRoute(listProjectsHandler)
    .newRoute().pathSegment("projects").numberSegment("projectId").buildRoute(showProjectHandler)
    .newRoute().pathSegment("projects").numberSegment("projectId").pathSegment("todos").buildRoute(listTodosHandler)
    .build();

MyRes res = pta.match(new MyReq("/projects/1/todos", "GET", "text/html"));
```

There are many ways to create a router, and the builder is the most flexible one.

### Creating handlers

```java
IRouteHandler<MyReq, MyRes> listTodosHandler = new IRoutesHandler<MyReq, MyRes>() {
    @Override
    public MyRes call(RouteMatch<MyReq> match) {
        // You call your own getMethod(), we don't care, we just call your handler
        // when the path matches.
        if (match.getRequest().getMethod() == "GET") {
            // Fetch the value from the `numberSegment` in the route.
            Integer projectId = match.getIntegerRouteMatchResult("projectId");
            return new MyRes("Call business logic to get actual list of todos for " + projectId);
        } else {
            return null;
        }
    }
}
```

Your handler gets called when the route matches, and you're respondible for doing any additional routing. Here we can see that getRequest on MyReq is called. Do whatever you want here, all we care about is that you return an instance of MyRes or null.

### Creating a router with the builder (as mentioned above)

```java
PathTravelAgent.Builder.<MyReq,MyRes>start()
    .newRoute().buildRoute(homePageHandler)
    .newRoute().pathSegment("projects").numberSegment("projectId").pathSegment("todos").buildRoute(listTodosHandler)
    .build();
```

### Creating a router from strings

```java
PathTravelAgent.Builder.<MyReq,MyRes>start()
    .newRouteString("/", homePageHandler)
    .newRouteString("/projects/$projectId/todos", listTodosHandler)
    .build();
```

### Creating a router from data structures

```java
List<Route<MyReq, MyRes>> routes = new ArrayList<Route<MyReq, MyRes>();

List<ISegment> myHomeRouteSegments = new ArrayList<ISegment>();
routes.add(new Route<MyReq, MyRes>(myHomeRouteSegments, homePageHandler));

myTodosRouteSegments = new ArrayList<ISegment>();
myTodosRoutesSegments.add(new PathSegment("projects"));
myTodosRoutesSegments.add(new NumberSegment("projectId"));
myTodosRoutesSegments.add(new PathSegment("todos"));
routes.add(new Route<MyReq, MyRes>(myTodosRouteSegments, listTodosHandler));

new PathTravelAgent<MyReq, MyRes>(routes);
```

### Creating a router by combining builder and data structures

```java
PathTravelAgent.Builder.<MyReq,MyRes>start()
    .addRoute(new Route<MyReq, MyRes>(myHomeRouteSegments, homePageHandler))
    .addRoute(new Route<MyReq, MyRes>(myTodosRouteSegments, listTodosHandler))
    .build();
```

## Performance

Path Travel Agent is *very* fast.

```
PathTravelAgentBenchmark.largeRouter: [measured 10 out of 15 rounds, threads: 1 (sequential)]
 round: 0.04 [+- 0.01], round.block: 0.00 [+- 0.00], round.gc: 0.00 [+- 0.00], GC.calls: 2, GC.time: 0.02, time.total: 0.74, time.warmup: 0.37, time.bench: 0.37

PathTravelAgentBenchmark.sparkRouter: [measured 10 out of 15 rounds, threads: 1 (sequential)]
 round: 0.66 [+- 0.03], round.block: 0.00 [+- 0.00], round.gc: 0.00 [+- 0.00], GC.calls: 11, GC.time: 0.02, time.total: 10.30, time.warmup: 3.72, time.bench: 6.58
```

Comparing Path Travel Agent against spark, using a route set with 25 paths, we spend 0.37 seconds while spark spends 6.58 seconds.

This is because the routes is stored in a trie-like tree structure. Traditionally, routers (spark included) stores the routes in an array. This means that we at worst have to look through all the paths to find a match. A trie exploits the fact that most URLs share a structure (for example, /people, /people/$personId and /people/$personId/friends). Going through the chunks (the stuff between the slashes) and only touching a chunk once is therefore much more efficient.

## License

3-clause BSD License

## Copyright

Copyright 2014, August Lilleaas
