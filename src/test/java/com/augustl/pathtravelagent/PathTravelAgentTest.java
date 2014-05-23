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
            .newRoute().pathSegment("foo").buildRoute(new RouteHandler<TestReq, TestRes>() {
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
            .newRoute().pathSegment("projects").numberSegment("projectId").buildRoute(new RouteHandler<TestReq, TestRes>() {
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
            .newRouteString("/projects/$projectId", new RouteHandler<TestReq, TestRes>() {
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
            .newRoute().pathSegment("foo").buildRoute(new RouteHandler<TestReq, TestRes>() {
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

    private class TestHandler implements RouteHandler<TestReq, TestRes> {
        private final String ret;
        public TestHandler(String ret) {
            this.ret = ret;
        }

        @Override
        public TestRes call(RouteMatch<TestReq> match) {
            return new TestRes(this.ret);
        }
    }
}
