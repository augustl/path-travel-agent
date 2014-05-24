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

The `com.augustl.pathtravelagent.PathTravelAgent` has two generics, the types that represents requests and responses.

```java
 PathTravelAgent<MyReq, MyRes> pta;
```

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
class MyReq implements IRequest {
    private final String path;
    private final String method;
    private final String accept;
    // Only getPath() is used by the system - other fields are metadata for yourself.
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

`MyRes` can be anything you want, we don't care. The generic is only there for type safety, and we return your response as-is, without any processing.

```java
class MyRes {
}
```

`MyRes` will probably contain information returned from calling business logic in your app, so you will probably have a field called `body` and `status` on it. But this is completely up to you.

There are many ways to create a router. Here's the most flexible one, the builder.

```java
PathTravelAgent<MyReq, MyRes> pta = PathTravelAgent.Builder.<MyReq,MyRes>start()
    .newRoute().pathSegment("projects").buildRoute(listProjectsHandler)
    .newRoute().pathSegment("projects").numberSegment("projectId").buildRoute(showProjectHandler)
    .newRoute().pathSegment("projects").numberSegment("projectId").pathSegment("todos").buildRoute(listTodosHandler)
    .build();

MyRes res = pta.match(new MyReq("/projects/1/todos", "GET", "text/html"));
```

Again, `PathTravelAgent` only cares about `gePath` on the requst, so the other values passed to the constructor are for your own usage.

Let's look at handler, here's `listTodosHandler`.

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

There are convenience methods for building routes. Here are a bunch of equivalents.

```java
PathTravelAgent.Builder.<MyReq,MyRes>start()
    .newRoute().pathSegment("projects").numberSegment("projectId").pathSegment("todos").buildRoute(listTodosHandler)
    .build();

// Which is the same as...
PathTravelAgent.Builder.<MyReq,MyRes>start()
    .newRouteString("/projects/$projectId/todos", listTodosHandler)
    .build();

// Which is the same as...
List<Route<MyReq, MyRes>> routes = new ArrayList<Route<MyReq, MyRes>();
List<ISegment> myRouteSegments = new ArrayList<ISegment>();
myRoutesSegments.add(new PathSegment("projects"));
myRoutesSegments.add(new NumberSegment("projectId"));
myRoutesSegments.add(new PathSegment("todos"));
routes.add(new Route<MyReq, MyRes>(myRouteSegments, listTodosHandler));
new PathTravelAgent<MyReq, MyRes>(routes);
```

You can use the manual list building API as well as the builder (just `segment` instead of `pathSegment` etc) to add any segment you want, as long as it implements ISegment. This allows custom parsing and handling of path segment.

And that's it! Have fun!

## License

3-clause BSD License

## Copyright

Copyright 2014, August Lilleaas
