package com.augustl.pathtravelagent;

import org.junit.Test;

import static org.junit.Assert.*;

public class PathTravelAgentTest {
    @Test
    public void matchesSinglePath() {
        PathTravelAgent<TestReq, TestRes> pta = PathTravelAgent.Builder.<TestReq, TestRes>start()
            .newRoute().pathSegment("foo").buildRoute(new TestHandler("Hello, foo!"))
            .build();

        assertEquals(pta.match(new TestReq("/foo")).getBody(), "Hello, foo!");
    }

    @Test
    public void noRoutes() {
        PathTravelAgent<TestReq, TestRes> pta = PathTravelAgent.Builder.<TestReq, TestRes>start().build();

        assertNull(pta.match(new TestReq("/foo")));
    }

    @Test
    public void matchesPathWhenMultiple() {
        PathTravelAgent<TestReq, TestRes> pta = PathTravelAgent.Builder.<TestReq, TestRes>start()
            .newRoute().pathSegment("foo").buildRoute(new TestHandler("Hello, foo!"))
            .newRoute().pathSegment("bar").buildRoute(new TestHandler("Hello, bar!"))
            .build();

        assertEquals(pta.match(new TestReq("/foo")).getBody(), "Hello, foo!");
        assertEquals(pta.match(new TestReq("/bar")).getBody(), "Hello, bar!");
    }

    @Test
    public void matchesSingleNestedPath() {
        PathTravelAgent<TestReq, TestRes> pta = PathTravelAgent.Builder.<TestReq, TestRes>start()
            .newRoute().pathSegment("foo").pathSegment("bar").pathSegment("baz").buildRoute(new TestHandler("Hello, foobarbaz!"))
            .build();

        assertNull(pta.match(new TestReq("/foo")));
        assertNull(pta.match(new TestReq("/foo/bar")));
        assertEquals(pta.match(new TestReq("/foo/bar/baz")).getBody(), "Hello, foobarbaz!");
        assertNull(pta.match(new TestReq("/foo/bar/baz/maz")));
    }

    @Test
    public void matchesWithCustomRequest() {
        PathTravelAgent<TestReq, TestRes> pta = PathTravelAgent.Builder.<TestReq, TestRes>start()
            .newRoute().pathSegment("foo").buildRoute(new IRouteHandler<TestReq, TestRes>() {
                @Override
                public TestRes call(RouteMatch<TestReq> match) {
                    return new TestRes("Hello " + match.getRequest().extras);
                }
            })
            .build();

        assertEquals(pta.match(new TestReq("/foo", "to you!")).getBody(), "Hello to you!");
    }

    @Test
    public void matchesWithNumberSegment() {
        PathTravelAgent<TestReq, TestRes> pta = PathTravelAgent.Builder.<TestReq, TestRes>start()
            .newRoute().pathSegment("projects").numberSegment("projectId").buildRoute(new IRouteHandler<TestReq, TestRes>() {
                @Override
                public TestRes call(RouteMatch<TestReq> match) {
                    return new TestRes("Hello " + (match.getIntegerRouteMatchResult("projectId") + 1));
                }
            })
            .build();

        assertEquals(pta.match(new TestReq("/projects/1")).getBody(), "Hello 2");
        assertEquals(pta.match(new TestReq("/projects/1321")).getBody(), "Hello 1322");
        assertNull(pta.match(new TestReq("/projects/123abc")));
        assertNull(pta.match(new TestReq("/projects/123/doesnotexist")));

    }

    @Test
    public void matchesWithStringConvenienceApi() {
        PathTravelAgent<TestReq, TestRes> pta = PathTravelAgent.Builder.<TestReq, TestRes>start()
            .newRouteString("/foo", new TestHandler("hello, foo"))
            .newRouteString("/projects/$projectId", new IRouteHandler<TestReq, TestRes>() {
                @Override
                public TestRes call(RouteMatch<TestReq> match) {
                    return new TestRes("Hello " + match.getIntegerRouteMatchResult("projectId"));
                }
            })
            .build();

        assertEquals(pta.match(new TestReq("/foo")).getBody(), "hello, foo");
        assertEquals(pta.match(new TestReq("/projects/1")).getBody(), "Hello 1");
    }

    @Test
    public void routeBasedOnRequest() {
        PathTravelAgent<TestReq, TestRes> pta = PathTravelAgent.Builder.<TestReq, TestRes>start()
            .newRoute().pathSegment("foo").buildRoute(new IRouteHandler<TestReq, TestRes>() {
                @Override
                public TestRes call(RouteMatch<TestReq> match) {
                    if (match.getRequest().extras == "yay") {
                        return new TestRes("we got yay");
                    } else {
                        return null;
                    }
                }
            })
            .build();

        assertEquals(pta.match(new TestReq("/foo", "yay")).getBody(), "we got yay");
        assertNull(pta.match(new TestReq("/foo", "not yay")));
        assertNull(pta.match(new TestReq("/bar", "yay")));
    }

    @Test
    public void customSegment() {
        PathTravelAgent<TestReq, TestRes> pta = PathTravelAgent.Builder.<TestReq, TestRes>start()
            .newRoute().pathSegment("projects").segment(new TestSegment("projectId", "666")).buildRoute(new IRouteHandler<TestReq, TestRes>() {
                @Override
                public TestRes call(RouteMatch<TestReq> match) {
                    return new TestRes("hello " + match.getStringRouteMatchResult("projectId"));
                }
            })
            .build();

        assertEquals(pta.match(new TestReq("/projects/666")).getBody(), "hello 666");
        assertEquals(pta.match(new TestReq("/projects/666123")).getBody(), "hello 666123");
        assertNull(pta.match(new TestReq("/projects/1")));
    }

    @Test
    public void matchesNestedPathWithParams() {
        PathTravelAgent<TestReq, TestRes> pta = PathTravelAgent.Builder.<TestReq, TestRes>start()
            .newRoute().pathSegment("projects").buildRoute(new TestHandler("projects list"))
            .newRoute().pathSegment("projects").numberSegment("projectId").buildRoute(new TestHandler("single project"))
            .newRoute().pathSegment("projects").numberSegment("projectId").pathSegment("todos").buildRoute(new TestHandler("todos list"))
            .newRoute().pathSegment("projects").numberSegment("projectId").pathSegment("todos").numberSegment("todoId").buildRoute(new TestHandler("single todo"))
            .build();

        assertEquals(pta.match(new TestReq("/projects")).getBody(), "projects list");
        assertEquals(pta.match(new TestReq("/projects/123")).getBody(), "single project");
        assertEquals(pta.match(new TestReq("/projects/123/todos")).getBody(), "todos list");
        assertEquals(pta.match(new TestReq("/projects/123/todos/456")).getBody(), "single todo");
    }

    @Test
    public void matchesNestedTreepaths() {
        PathTravelAgent<TestReq, TestRes> pta = PathTravelAgent.Builder.<TestReq, TestRes>start()
            .newRoute().pathSegment("foo").pathSegment("bar").buildRoute(new TestHandler("bar"))
            .newRoute().pathSegment("foo").pathSegment("bar").pathSegment("subbar").buildRoute(new TestHandler("subbar"))
            .newRoute().pathSegment("foo").pathSegment("baz").buildRoute(new TestHandler("baz"))
            .newRoute().pathSegment("foo").pathSegment("baz").pathSegment("subbaz").buildRoute(new TestHandler("subbaz"))
            .newRoute().pathSegment("foo").pathSegment("baz").pathSegment("otherbaz").buildRoute(new TestHandler("otherbaz"))
            .build();

        assertNull(pta.match(new TestReq("/foo")));
        assertEquals(pta.match(new TestReq("/foo/bar")).getBody(), "bar");
        assertEquals(pta.match(new TestReq("/foo/baz")).getBody(), "baz");
        assertEquals(pta.match(new TestReq("/foo/bar/subbar")).getBody(), "subbar");
        assertEquals(pta.match(new TestReq("/foo/baz/subbaz")).getBody(), "subbaz");
        assertEquals(pta.match(new TestReq("/foo/baz/otherbaz")).getBody(), "otherbaz");
    }

    private class TestReq implements IRequest {
        private final String path;
        private final Object extras;

        public TestReq(String path) {
            this.path = path;
            this.extras = null;
        }

        public TestReq(String path, Object extras) {
            this.path = path;
            this.extras = extras;
        }


        @Override
        public String getPath() {
            return this.path;
        }
    }

    private class TestRes {
        private final String body;

        public TestRes(String body) {
            this.body = body;
        }

        public String getBody() {
            return this.body;
        }
    }

    private class TestHandler implements IRouteHandler<TestReq, TestRes> {
        private final String ret;
        public TestHandler(String ret) {
            this.ret = ret;
        }

        @Override
        public TestRes call(RouteMatch<TestReq> match) {
            return new TestRes(this.ret);
        }
    }

    private class TestSegment implements ISegmentParametric {
        private final String paramName;
        private final String requiredValue;
        public TestSegment(String paramName, String requiredValue) {
            this.paramName = paramName;
            this.requiredValue = requiredValue;
        }

        @Override
        public RouteMatchResult.IResult matchPathSegment(String pathSegment) {
            if (pathSegment.startsWith(this.requiredValue)) {
                return new RouteMatchResult.StringResult(pathSegment);
            } else {
                return null;
            }
        }

        @Override
        public String getSegmentName() {
            return this.paramName;
        }
    }
}
