package com.augustl.pathtravelagent;

import com.augustl.pathtravelagent.segment.NumberSegment;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;
import static com.augustl.pathtravelagent.DefaultRouteMatcher.*;

public class PathTravelAgentTest {
    @Test
    public void matchesSinglePath() {
        RouteTreeNode<TestReq, TestRes> r = new RouteTreeBuilder<TestReq, TestRes>()
            .path("/foo", new RouteTreeBuilder<TestReq, TestRes>()
                .handler(new TestHandler("Hello, foo!")))
            .build();
        assertEquals(match(r, new TestReq("/foo")), new TestRes("Hello, foo!"));
    }

    @Test
    public void noRoutes() {
        RouteTreeNode<TestReq, TestRes> r = new RouteTreeBuilder<TestReq, TestRes>()
            .build();

        assertNull(match(r, new TestReq("/foo")));
    }

    @Test
    public void matchesPathWhenMultiple() {
        RouteTreeNode<TestReq, TestRes> r = new RouteTreeBuilder<TestReq, TestRes>()
            .path("/foo", new RouteTreeBuilder<TestReq, TestRes>()
                .handler(new TestHandler("Hello, foo!")))
            .path("/bar", new RouteTreeBuilder<TestReq, TestRes>()
                .handler(new TestHandler("Hello, bar!")))
            .build();

        assertEquals(match(r, new TestReq("/foo")), new TestRes("Hello, foo!"));
        assertEquals(match(r, new TestReq("/bar")), new TestRes("Hello, bar!"));
    }

    @Test
    public void matchesSingleNestedPath() {
        RouteTreeNode<TestReq, TestRes> r = new RouteTreeBuilder<TestReq, TestRes>()
            .path("/foo", new RouteTreeBuilder<TestReq, TestRes>()
                .path("/bar", new RouteTreeBuilder<TestReq, TestRes>()
                    .path("/baz", new RouteTreeBuilder<TestReq, TestRes>()
                        .handler(new TestHandler("Hello, foobarbaz!")))))
            .build();

        assertNull(match(r, new TestReq("/foo")));
        assertNull(match(r, new TestReq("/foo/bar")));
        assertEquals(match(r, new TestReq("/foo/bar/baz")), new TestRes("Hello, foobarbaz!"));
        assertNull(match(r, new TestReq("/foo/bar/baz/maz")));
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

        assertEquals(match(r, new TestReq("/foo", "to you!")), new TestRes("Hello to you!"));
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

        assertEquals(match(r, new TestReq("/projects/1")), new TestRes("Hello 2"));
        assertEquals(match(r, new TestReq("/projects/1321")), new TestRes("Hello 1322"));
        assertNull(match(r, new TestReq("/projects/123abc")));
        assertNull(match(r, new TestReq("/projects/123/doesnotexist")));

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

        assertEquals(match(r, new TestReq("/foo", "yay")), new TestRes("we got yay"));
        assertNull(match(r, new TestReq("/foo", "not yay")));
        assertNull(match(r, new TestReq("/bar", "yay")));
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

        assertEquals(match(r, new TestReq("/projects/666")), new TestRes("hello 666"));
        assertEquals(match(r, new TestReq("/projects/666123")), new TestRes("hello 666123"));
        assertNull(match(r, new TestReq("/projects/1")));
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

        assertEquals(match(r, new TestReq("/projects")), new TestRes("projects list"));
        assertEquals(match(r, new TestReq("/projects/123")), new TestRes("single project"));
        assertEquals(match(r, new TestReq("/projects/123/todos")), new TestRes("todos list"));
        assertEquals(match(r, new TestReq("/projects/123/todos/456")), new TestRes("single todo"));
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

        assertNull(match(r, new TestReq("/foo")));
        assertEquals(match(r, new TestReq("/foo/bar")), new TestRes("bar"));
        assertEquals(match(r, new TestReq("/foo/baz")), new TestRes("baz"));
        assertEquals(match(r, new TestReq("/foo/bar/subbar")), new TestRes("subbar"));
        assertEquals(match(r, new TestReq("/foo/baz/subbaz")), new TestRes("subbaz"));
        assertEquals(match(r, new TestReq("/foo/baz/otherbaz")), new TestRes("otherbaz"));
    }

    @Test
    public void rootRoute() {
        RouteTreeNode<TestReq, TestRes> r = new RouteTreeBuilder<TestReq, TestRes>()
            .handler(new TestHandler("root"))
            .path("/foo", new RouteTreeBuilder<TestReq, TestRes>()
                .handler(new TestHandler("foo")))
            .build();

        assertEquals(match(r, new TestReq("/")), new TestRes("root"));
        assertEquals(match(r, new TestReq("/foo")), new TestRes("foo"));
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

        assertEquals(match(r, new TestReq("/666")), new TestRes("hello, parametric 666"));
        assertEquals(match(r, new TestReq("/test")), new TestRes("hello, test"));
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

            match(r, new TestReq(url));
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

        assertEquals(match(r, new TestReq("/")), new TestRes("hello, root"));
        assertEquals(match(r, new TestReq("//")), new TestRes("hello, root"));
        assertEquals(match(r, new TestReq("/test")), new TestRes("hello, test"));
        assertEquals(match(r, new TestReq("/test/")), new TestRes("hello, test"));
        assertEquals(match(r, new TestReq("/test//")), new TestRes("hello, test"));
        assertEquals(match(r, new TestReq("/test/wat")), new TestRes("hello, param wat"));
        assertEquals(match(r, new TestReq("/test/wat/")), new TestRes("hello, param wat"));
        assertEquals(match(r, new TestReq("/test/wat//")), new TestRes("hello, param wat"));
        assertEquals(match(r, new TestReq("/hmmm")), new TestRes("hello, user hmmm"));
        assertEquals(match(r, new TestReq("/hmmm/")), new TestRes("hello, user hmmm"));
        assertEquals(match(r, new TestReq("/hmmm///")), new TestRes("hello, user hmmm"));


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

        assertEquals(new TestRes("Hello, root!"), match(r, new TestReq("/")));
        assertEquals(new TestRes("Project listing"), match(r, new TestReq("/projects")));
        assertEquals(new TestRes("Hello, project 123"), match(r, new TestReq("/projects/123")));
        assertEquals(new TestRes("Hello, foo!"), match(r, new TestReq("/foo")));
        assertNull(match(r, new TestReq("/bar")));
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

        assertEquals(new TestRes("Hello, pictures"), match(r, new TestReq("/pictures")));
        assertEquals(new TestRes("Folder [foo]"), match(r, new TestReq("/pictures/foo")));
        assertEquals(new TestRes("Folder [foo, test, 123]"), match(r, new TestReq("/pictures/foo/test/123")));
        assertEquals(new TestRes("Here goes [foo]"), match(r, new TestReq("/foo")));
        assertEquals(new TestRes("Here goes [foo, bar, baz]"), match(r, new TestReq("/foo/bar/baz")));
    }
}
