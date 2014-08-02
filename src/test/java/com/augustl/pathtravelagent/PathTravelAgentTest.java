package com.augustl.pathtravelagent;

import com.augustl.pathtravelagent.segment.NumberSegment;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class PathTravelAgentTest {
    @Test
    public void matchesSinglePath() {
        RouteTreeNode<TestReq, TestRes> r = new RouteTreeBuilder<TestReq, TestRes>()
            .path("/foo", new RouteTreeBuilder<TestReq, TestRes>()
                .handler(new TestHandler("Hello, foo!")))
            .build();
        assertEquals(r.match(new TestReq("/foo")).getBody(), "Hello, foo!");
    }

    @Test
    public void noRoutes() {
        RouteTreeNode<TestReq, TestRes> r = new RouteTreeBuilder<TestReq, TestRes>()
            .build();

        assertNull(r.match(new TestReq("/foo")));
    }

    @Test
    public void matchesPathWhenMultiple() {
        RouteTreeNode<TestReq, TestRes> r = new RouteTreeBuilder<TestReq, TestRes>()
            .path("/foo", new RouteTreeBuilder<TestReq, TestRes>()
                .handler(new TestHandler("Hello, foo!")))
            .path("/bar", new RouteTreeBuilder<TestReq, TestRes>()
                .handler(new TestHandler("Hello, bar!")))
            .build();

        assertEquals(r.match(new TestReq("/foo")).getBody(), "Hello, foo!");
        assertEquals(r.match(new TestReq("/bar")).getBody(), "Hello, bar!");
    }

    @Test
    public void matchesSingleNestedPath() {
        RouteTreeNode<TestReq, TestRes> r = new RouteTreeBuilder<TestReq, TestRes>()
            .path("/foo", new RouteTreeBuilder<TestReq, TestRes>()
                .path("/bar", new RouteTreeBuilder<TestReq, TestRes>()
                    .path("/baz", new RouteTreeBuilder<TestReq, TestRes>()
                        .handler(new TestHandler("Hello, foobarbaz!")))))
            .build();

        assertNull(r.match(new TestReq("/foo")));
        assertNull(r.match(new TestReq("/foo/bar")));
        assertEquals(r.match(new TestReq("/foo/bar/baz")).getBody(), "Hello, foobarbaz!");
        assertNull(r.match(new TestReq("/foo/bar/baz/maz")));
    }

    @Test
    public void matchesWithCustomRequest() {
        RouteTreeNode<TestReq, TestRes> r = new RouteTreeBuilder<TestReq, TestRes>()
            .path("/foo", new RouteTreeBuilder<TestReq, TestRes>()
                .handler(new IRouteHandler<TestReq, TestRes>() {
                    @Override
                    public TestRes call(RouteMatch<TestReq> match) {
                        return new TestRes("Hello " + match.getRequest().getExtras());
                    }
                }))
            .build();

        assertEquals(r.match(new TestReq("/foo", "to you!")).getBody(), "Hello to you!");
    }

    @Test
    public void matchesWithNumberSegment() {
        RouteTreeNode<TestReq, TestRes> r = new RouteTreeBuilder<TestReq, TestRes>()
            .path("/projects", new RouteTreeBuilder<TestReq, TestRes>()
                .param(new NumberSegment("projectId"), new RouteTreeBuilder<TestReq, TestRes>()
                    .handler(new IRouteHandler<TestReq, TestRes>() {
                        @Override
                        public TestRes call(RouteMatch<TestReq> match) {
                            return new TestRes("Hello " + (match.getIntegerRouteMatchResult("projectId") + 1));
                        }
                    })))
            .build();

        assertEquals(r.match(new TestReq("/projects/1")).getBody(), "Hello 2");
        assertEquals(r.match(new TestReq("/projects/1321")).getBody(), "Hello 1322");
        assertNull(r.match(new TestReq("/projects/123abc")));
        assertNull(r.match(new TestReq("/projects/123/doesnotexist")));

    }

    @Test
    public void matchesWithStringConvenienceApi() {
        // TODO: Write me.
    }

    @Test
    public void routeBasedOnRequest() {
        RouteTreeNode<TestReq, TestRes> r = new RouteTreeBuilder<TestReq, TestRes>()
            .path("/foo", new RouteTreeBuilder<TestReq, TestRes>()
                .handler(new IRouteHandler<TestReq, TestRes>() {
                    @Override
                    public TestRes call(RouteMatch<TestReq> match) {
                        if (match.getRequest().getExtras() == "yay") {
                            return new TestRes("we got yay");
                        } else {
                            return null;
                        }
                    }
                }))
            .build();

        assertEquals(r.match(new TestReq("/foo", "yay")).getBody(), "we got yay");
        assertNull(r.match(new TestReq("/foo", "not yay")));
        assertNull(r.match(new TestReq("/bar", "yay")));
    }

    @Test
    public void customSegment() {
        RouteTreeNode<TestReq, TestRes> r = new RouteTreeBuilder<TestReq, TestRes>()
            .path("/projects", new RouteTreeBuilder<TestReq, TestRes>()
                .param(new TestSegment("projectId", "666"), new RouteTreeBuilder<TestReq, TestRes>()
                    .handler(new IRouteHandler<TestReq, TestRes>() {
                        @Override
                        public TestRes call(RouteMatch<TestReq> match) {
                            return new TestRes("hello " + match.getStringRouteMatchResult("projectId"));
                        }
                    })))
            .build();

        assertEquals(r.match(new TestReq("/projects/666")).getBody(), "hello 666");
        assertEquals(r.match(new TestReq("/projects/666123")).getBody(), "hello 666123");
        assertNull(r.match(new TestReq("/projects/1")));
    }

    @Test
    public void matchesNestedPathWithParams() {
        RouteTreeNode<TestReq, TestRes> r = new RouteTreeBuilder<TestReq, TestRes>()
            .path("/projects", new RouteTreeBuilder<TestReq, TestRes>()
                .handler(new TestHandler("projects list"))
                .param(new NumberSegment("projectId"), new RouteTreeBuilder<TestReq, TestRes>()
                    .handler(new TestHandler("single project"))
                    .path("/todos", new RouteTreeBuilder<TestReq, TestRes>()
                        .handler(new TestHandler("todos list"))
                        .param(new NumberSegment("todoId"), new RouteTreeBuilder<TestReq, TestRes>()
                            .handler(new TestHandler("single todo"))))))
            .build();

        assertEquals(r.match(new TestReq("/projects")).getBody(), "projects list");
        assertEquals(r.match(new TestReq("/projects/123")).getBody(), "single project");
        assertEquals(r.match(new TestReq("/projects/123/todos")).getBody(), "todos list");
        assertEquals(r.match(new TestReq("/projects/123/todos/456")).getBody(), "single todo");
    }

    @Test
    public void matchesNestedTreepaths() {
        RouteTreeNode<TestReq, TestRes> r = new RouteTreeBuilder<TestReq, TestRes>()
            .path("/foo", new RouteTreeBuilder<TestReq, TestRes>()
                .path("/bar", new RouteTreeBuilder<TestReq, TestRes>()
                    .handler(new TestHandler("bar"))
                    .path("/subbar", new RouteTreeBuilder<TestReq, TestRes>()
                        .handler(new TestHandler("subbar"))))
                .path("/baz", new RouteTreeBuilder<TestReq, TestRes>()
                    .handler(new TestHandler("baz"))
                    .path("/subbaz", new RouteTreeBuilder<TestReq, TestRes>()
                        .handler(new TestHandler("subbaz")))
                    .path("/otherbaz", new RouteTreeBuilder<TestReq, TestRes>()
                        .handler(new TestHandler("otherbaz")))))
            .build();

        assertNull(r.match(new TestReq("/foo")));
        assertEquals(r.match(new TestReq("/foo/bar")).getBody(), "bar");
        assertEquals(r.match(new TestReq("/foo/baz")).getBody(), "baz");
        assertEquals(r.match(new TestReq("/foo/bar/subbar")).getBody(), "subbar");
        assertEquals(r.match(new TestReq("/foo/baz/subbaz")).getBody(), "subbaz");
        assertEquals(r.match(new TestReq("/foo/baz/otherbaz")).getBody(), "otherbaz");
    }

    @Test
    public void rootRoute() {
        RouteTreeNode<TestReq, TestRes> r = new RouteTreeBuilder<TestReq, TestRes>()
            .handler(new TestHandler("root"))
            .path("/foo", new RouteTreeBuilder<TestReq, TestRes>()
                .handler(new TestHandler("foo")))
            .build();

        assertEquals(r.match(new TestReq("/")).getBody(), "root");
        assertEquals(r.match(new TestReq("/foo")).getBody(), "foo");
    }

    @Test
    public void parametricSegmentAtRoot() {
        RouteTreeNode<TestReq, TestRes> r = new RouteTreeBuilder<TestReq, TestRes>()
            .handler(new TestHandler("root"))
            .path("/test", new RouteTreeBuilder<TestReq, TestRes>()
                .handler(new TestHandler("hello, test")))
            .param("/:projectId", new RouteTreeBuilder<TestReq, TestRes>()
                .handler(new IRouteHandler<TestReq, TestRes>() {
                    @Override
                    public TestRes call(RouteMatch<TestReq> match) {
                        return new TestRes("hello, parametric " + match.getStringRouteMatchResult("projectId"));
                    }
                }))
            .build();

        assertEquals(r.match(new TestReq("/666")).getBody(), "hello, parametric 666");
        assertEquals(r.match(new TestReq("/test")).getBody(), "hello, test");
    }

    @Test
    public void randomizedOddInput() {
        RouteTreeNode<TestReq, TestRes> r = new RouteTreeBuilder<TestReq, TestRes>()
            .handler(new TestHandler("hello, root"))
            .path("/projects", new RouteTreeBuilder<TestReq, TestRes>()
                .handler(new TestHandler("hello, projects"))
                .param("/:projectId", new RouteTreeBuilder<TestReq, TestRes>()
                    .handler(new TestHandler("hello, specific project"))))
            .param("/:userShortname", new RouteTreeBuilder<TestReq, TestRes>()
                .handler(new TestHandler("hello, user page")))
            .build();

        String[] symbols = {"/", "/", "/", "/", "/",
            "$", "\\", "?",
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l"};

        Random rand = new Random();
        for (int i = 0; i < 10000; i++) {
            String url = "/";
            for (int j = 0; j < rand.nextInt(100); j++) {
                url = url + symbols[rand.nextInt(symbols.length)];
            }

            r.match(new TestReq(url));
        }
    }

    @Test
    public void oddInput() {
        RouteTreeNode<TestReq, TestRes> r = new RouteTreeBuilder<TestReq, TestRes>()
            .handler(new TestHandler("hello, root"))
            .path("/test", new RouteTreeBuilder<TestReq, TestRes>()
                .handler(new TestHandler("hello, test"))
                .param("/:id", new RouteTreeBuilder<TestReq, TestRes>()
                    .handler(new IRouteHandler<TestReq, TestRes>() {
                        @Override
                        public TestRes call(RouteMatch<TestReq> match) {
                            return new TestRes("hello, param " + match.getStringRouteMatchResult("id"));
                        }
                    })))
            .param("/:userShortname", new RouteTreeBuilder<TestReq, TestRes>()
                .handler(new IRouteHandler<TestReq, TestRes>() {
                    @Override
                    public TestRes call(RouteMatch<TestReq> match) {
                        return new TestRes("hello, user " + match.getStringRouteMatchResult("userShortname"));
                    }
                }))
            .build();

        assertEquals(r.match(new TestReq("/")).getBody(), "hello, root");
        assertEquals(r.match(new TestReq("//")).getBody(), "hello, root");
        assertEquals(r.match(new TestReq("/test")).getBody(), "hello, test");
        assertEquals(r.match(new TestReq("/test/")).getBody(), "hello, test");
        assertEquals(r.match(new TestReq("/test//")).getBody(), "hello, test");
        assertEquals(r.match(new TestReq("/test/wat")).getBody(), "hello, param wat");
        assertEquals(r.match(new TestReq("/test/wat/")).getBody(), "hello, param wat");
        assertEquals(r.match(new TestReq("/test/wat//")).getBody(), "hello, param wat");
        assertEquals(r.match(new TestReq("/hmmm")).getBody(), "hello, user hmmm");
        assertEquals(r.match(new TestReq("/hmmm/")).getBody(), "hello, user hmmm");
        assertEquals(r.match(new TestReq("/hmmm///")).getBody(), "hello, user hmmm");


    }

    @Test
    public void testAllTheThings() {
        RouteTreeNode<TestReq, TestRes> r = new RouteTreeBuilder<TestReq, TestRes>()
            .handler(new TestHandler("Hello, root!"))
            .path("/projects", new RouteTreeBuilder<TestReq, TestRes>()
                .handler(new TestHandler("Project listing"))
                .param("/:projectId", new RouteTreeBuilder<TestReq, TestRes>()
                    .handler(new IRouteHandler<TestReq, TestRes>() {
                        @Override
                        public TestRes call(RouteMatch<TestReq> match) {
                            return new TestRes("Hello, project " + match.getStringRouteMatchResult("projectId"));
                        }
                    })))
            .path("/foo", new RouteTreeBuilder<TestReq, TestRes>()
                .handler(new TestHandler("Hello, foo!")))
            .build();

        assertEquals(new TestRes("Hello, root!"), r.match(new TestReq("/")));
        assertEquals(new TestRes("Project listing"), r.match(new TestReq("/projects")));
        assertEquals(new TestRes("Hello, project 123"), r.match(new TestReq("/projects/123")));
        assertEquals(new TestRes("Hello, foo!"), r.match(new TestReq("/foo")));
        assertNull(r.match(new TestReq("/bar")));
    }

    @Test
    public void testWildcard() {
        RouteTreeNode<TestReq, TestRes> r = new RouteTreeBuilder<TestReq, TestRes>()
            .path("/pictures", new RouteTreeBuilder<TestReq, TestRes>()
                .handler(new TestHandler("Hello, pictures"))
                .wildcard("*path", new RouteTreeBuilder<TestReq, TestRes>()
                    .handler(new IRouteHandler<TestReq, TestRes>() {
                        @Override
                        public TestRes call(RouteMatch<TestReq> match) {
                            return new TestRes("Folder " + match.getWildcardRouteMatchResult());
                        }
                    })))
            .wildcard("*anythingGoes", new RouteTreeBuilder<TestReq, TestRes>()
                .handler(new IRouteHandler<TestReq, TestRes>() {
                    @Override
                    public TestRes call(RouteMatch<TestReq> match) {
                        return new TestRes("Here goes " + match.getWildcardRouteMatchResult());
                    }
                }))
            .build();

        assertEquals(new TestRes("Hello, pictures"), r.match(new TestReq("/pictures")));
        assertEquals(new TestRes("Folder [foo]"), r.match(new TestReq("/pictures/foo")));
        assertEquals(new TestRes("Folder [foo, test, 123]"), r.match(new TestReq("/pictures/foo/test/123")));
        assertEquals(new TestRes("Here goes [foo]"), r.match(new TestReq("/foo")));
        assertEquals(new TestRes("Here goes [foo, bar, baz]"), r.match(new TestReq("/foo/bar/baz")));
    }
}
