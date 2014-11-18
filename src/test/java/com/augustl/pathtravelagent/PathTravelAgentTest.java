package com.augustl.pathtravelagent;

import com.augustl.pathtravelagent.segment.NumberSegment;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static com.augustl.pathtravelagent.DefaultRouteMatcher.*;

public class PathTravelAgentTest {
    private DefaultRouteMatcher<TestReq, TestRes> defaultRouteMatcher = new DefaultRouteMatcher<TestReq, TestRes>();
    private RouteTreeBuilderFactory<TestReq, TestRes> rf = new RouteTreeBuilderFactory<TestReq, TestRes>();

    private TestRes match(RouteTreeNode<TestReq, TestRes> r, TestReq req) {
        return defaultRouteMatcher.match(r, req);
    }

    @Test
    public void matchesSinglePath() {
        RouteTreeNode<TestReq, TestRes> r = rf.builder()
            .path("/foo", rf.builder()
                .handler(new TestHandler("Hello, foo!")))
            .build();
        assertEquals(match(r, new TestReq("/foo")), new TestRes("Hello, foo!"));
    }

    @Test
    public void noRoutes() {
        RouteTreeNode<TestReq, TestRes> r = rf.builder()
            .build();

        assertNull(match(r, new TestReq("/foo")));
    }

    @Test
    public void matchesPathWhenMultiple() {
        RouteTreeNode<TestReq, TestRes> r = rf.builder()
            .path("/foo", rf.builder()
                .handler(new TestHandler("Hello, foo!")))
            .path("/bar", rf.builder()
                .handler(new TestHandler("Hello, bar!")))
            .build();

        assertEquals(match(r, new TestReq("/foo")), new TestRes("Hello, foo!"));
        assertEquals(match(r, new TestReq("/bar")), new TestRes("Hello, bar!"));
    }

    @Test
    public void matchesSingleNestedPath() {
        RouteTreeNode<TestReq, TestRes> r = rf.builder()
            .path("/foo", rf.builder()
                .path("/bar", rf.builder()
                    .path("/baz", rf.builder()
                        .handler(new TestHandler("Hello, foobarbaz!")))))
            .build();

        assertNull(match(r, new TestReq("/foo")));
        assertNull(match(r, new TestReq("/foo/bar")));
        assertEquals(match(r, new TestReq("/foo/bar/baz")), new TestRes("Hello, foobarbaz!"));
        assertNull(match(r, new TestReq("/foo/bar/baz/maz")));
    }

    @Test
    public void matchesWithCustomRequest() {
        RouteTreeNode<TestReq, TestRes> r = rf.builder()
            .path("/foo", rf.builder()
                .handler(new IRouteHandler<TestReq, TestRes>() {
                    @Override
                    public IRouteHandler<TestReq, TestRes> merge(IRouteHandler<TestReq, TestRes> other) {
                        return other;
                    }

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
        RouteTreeNode<TestReq, TestRes> r = rf.builder()
            .path("/projects", rf.builder()
                .param(new NumberSegment("projectId"), rf.builder()
                    .handler(new IRouteHandler<TestReq, TestRes>() {
                        @Override
                        public IRouteHandler<TestReq, TestRes> merge(IRouteHandler<TestReq, TestRes> other) {
                            return other;
                        }

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
        RouteTreeNode<TestReq, TestRes> r = rf.builder()
            .path("/foo", rf.builder()
                .handler(new IRouteHandler<TestReq, TestRes>() {
                    @Override
                    public IRouteHandler<TestReq, TestRes> merge(IRouteHandler<TestReq, TestRes> other) {
                        return other;
                    }

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
        RouteTreeNode<TestReq, TestRes> r = rf.builder()
            .path("/projects", rf.builder()
                .param(new TestSegment("projectId", "666"), rf.builder()
                    .handler(new IRouteHandler<TestReq, TestRes>() {
                        @Override
                        public IRouteHandler<TestReq, TestRes> merge(IRouteHandler<TestReq, TestRes> other) {
                            return other;
                        }

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
        RouteTreeNode<TestReq, TestRes> r = rf.builder()
            .path("/projects", rf.builder()
                .handler(new TestHandler("projects list"))
                .param(new NumberSegment("projectId"), rf.builder()
                    .handler(new TestHandler("single project"))
                    .path("/todos", rf.builder()
                        .handler(new TestHandler("todos list"))
                        .param(new NumberSegment("todoId"), rf.builder()
                            .handler(new TestHandler("single todo"))))))
            .build();

        assertEquals(match(r, new TestReq("/projects")), new TestRes("projects list"));
        assertEquals(match(r, new TestReq("/projects/123")), new TestRes("single project"));
        assertEquals(match(r, new TestReq("/projects/123/todos")), new TestRes("todos list"));
        assertEquals(match(r, new TestReq("/projects/123/todos/456")), new TestRes("single todo"));
    }

    @Test
    public void matchesNestedTreepaths() {
        RouteTreeNode<TestReq, TestRes> r = rf.builder()
            .path("/foo", rf.builder()
                .path("/bar", rf.builder()
                    .handler(new TestHandler("bar"))
                    .path("/subbar", rf.builder()
                        .handler(new TestHandler("subbar"))))
                .path("/baz", rf.builder()
                    .handler(new TestHandler("baz"))
                    .path("/subbaz", rf.builder()
                        .handler(new TestHandler("subbaz")))
                    .path("/otherbaz", rf.builder()
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
        RouteTreeNode<TestReq, TestRes> r = rf.builder()
            .handler(new TestHandler("root"))
            .path("/foo", rf.builder()
                .handler(new TestHandler("foo")))
            .build();

        assertEquals(match(r, new TestReq("/")), new TestRes("root"));
        assertEquals(match(r, new TestReq("/foo")), new TestRes("foo"));
    }

    @Test
    public void parametricSegmentAtRoot() {
        RouteTreeNode<TestReq, TestRes> r = rf.builder()
            .handler(new TestHandler("root"))
            .path("/test", rf.builder()
                .handler(new TestHandler("hello, test")))
            .param("/:projectId", rf.builder()
                .handler(new IRouteHandler<TestReq, TestRes>() {
                    @Override
                    public IRouteHandler<TestReq, TestRes> merge(IRouteHandler<TestReq, TestRes> other) {
                        return other;
                    }

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
        RouteTreeNode<TestReq, TestRes> r = rf.builder()
            .handler(new TestHandler("hello, root"))
            .path("/projects", rf.builder()
                .handler(new TestHandler("hello, projects"))
                .param("/:projectId", rf.builder()
                    .handler(new TestHandler("hello, specific project"))))
            .param("/:userShortname", rf.builder()
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
        RouteTreeNode<TestReq, TestRes> r = rf.builder()
            .handler(new TestHandler("hello, root"))
            .path("/test", rf.builder()
                .handler(new TestHandler("hello, test"))
                .param("/:id", rf.builder()
                    .handler(new IRouteHandler<TestReq, TestRes>() {
                        @Override
                        public IRouteHandler<TestReq, TestRes> merge(IRouteHandler<TestReq, TestRes> other) {
                            return other;
                        }

                        @Override
                        public TestRes call(RouteMatch<TestReq> match) {
                            return new TestRes("hello, param " + match.getStringRouteMatchResult("id"));
                        }
                    })))
            .param("/:userShortname", rf.builder()
                .handler(new IRouteHandler<TestReq, TestRes>() {
                    @Override
                    public IRouteHandler<TestReq, TestRes> merge(IRouteHandler<TestReq, TestRes> other) {
                        return other;
                    }

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

        assertEquals(match(r, new TestReq("/?wat")), new TestRes("hello, root"));
        assertEquals(match(r, new TestReq("/test/rawr?hmm/weird?zomg")), new TestRes("hello, param rawr"));
    }

    @Test
    public void testAllTheThings() {
        RouteTreeNode<TestReq, TestRes> r = rf.builder()
            .handler(new TestHandler("Hello, root!"))
            .path("/projects", rf.builder()
                .handler(new TestHandler("Project listing"))
                .param("/:projectId", rf.builder()
                    .handler(new IRouteHandler<TestReq, TestRes>() {
                        @Override
                        public IRouteHandler<TestReq, TestRes> merge(IRouteHandler<TestReq, TestRes> other) {
                            return other;
                        }

                        @Override
                        public TestRes call(RouteMatch<TestReq> match) {
                            return new TestRes("Hello, project " + match.getStringRouteMatchResult("projectId"));
                        }
                    })))
            .path("/foo", rf.builder()
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
        RouteTreeNode<TestReq, TestRes> r = rf.builder()
            .path("/pictures", rf.builder()
                .handler(new TestHandler("Hello, pictures"))
                .wildcard(rf.builder()
                    .handler(new IRouteHandler<TestReq, TestRes>() {
                        @Override
                        public IRouteHandler<TestReq, TestRes> merge(IRouteHandler<TestReq, TestRes> other) {
                            return other;
                        }

                        @Override
                        public TestRes call(RouteMatch<TestReq> match) {
                            return new TestRes("Folder " + match.getWildcardRouteMatchResult());
                        }
                    })))
            .wildcard(rf.builder()
                .handler(new IRouteHandler<TestReq, TestRes>() {
                    @Override
                    public IRouteHandler<TestReq, TestRes> merge(IRouteHandler<TestReq, TestRes> other) {
                        return other;
                    }

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

    @Test
    public void updatingRoutes() {
        RouteTreeNode<TestReq, TestRes> r1 = rf.builder()
            .path("/foo", rf.builder()
                .handler(new TestHandler("Hello, foo!")))
            .path("/baz", rf.builder()
                .handler(new TestHandler("Hello, baz!"))
                .param("/:baz-id", rf.builder()
                    .handler(new IRouteHandler<TestReq, TestRes>() {
                        @Override
                        public IRouteHandler<TestReq, TestRes> merge(IRouteHandler<TestReq, TestRes> other) {
                            return other;
                        }

                        @Override
                        public TestRes call(RouteMatch<TestReq> match) {
                            return new TestRes("Hello from baz with id " + match.getStringRouteMatchResult("baz-id"));
                        }
                    })
                    .param("/:sub-id", rf.builder()
                        .handler(new IRouteHandler<TestReq, TestRes>() {
                            @Override
                            public IRouteHandler<TestReq, TestRes> merge(IRouteHandler<TestReq, TestRes> other) {
                                return other;
                            }

                            @Override
                            public TestRes call(RouteMatch<TestReq> match) {
                                return new TestRes("Hello from baz-sub with baz-id " + match.getStringRouteMatchResult("baz-id") + " and sub-id " + match.getStringRouteMatchResult("sub-id"));
                            }
                        })
                        .path("/zing", rf.builder()
                            .handler(new IRouteHandler<TestReq, TestRes>() {
                                @Override
                                public IRouteHandler<TestReq, TestRes> merge(IRouteHandler<TestReq, TestRes> other) {
                                    return other;
                                }

                                @Override
                                public TestRes call(RouteMatch<TestReq> match) {
                                    return new TestRes("Hello from baz-sub zing with baz-id " + match.getStringRouteMatchResult("baz-id") + " and sub-id " + match.getStringRouteMatchResult("sub-id"));
                                }
                            }))))
                .path("/maz", rf.builder()
                    .handler(new TestHandler("Hello, baz/maz!"))))
            .build();

        RouteTreeNode<TestReq, TestRes> r2 = r1.merge(rf.builder()
            .path("/bar", rf.builder()
                .handler(new TestHandler("Hello, bar!")))
            .path("/baz", rf.builder()
                .param("/:baz-id", rf.builder()
                    .param("/:sub-id", rf.builder()
                        .handler(new IRouteHandler<TestReq, TestRes>() {
                            @Override
                            public IRouteHandler<TestReq, TestRes> merge(IRouteHandler<TestReq, TestRes> other) {
                                return other;
                            }

                            @Override
                            public TestRes call(RouteMatch<TestReq> match) {
                                return new TestRes("Hello from updated baz-sub with baz-id " + match.getStringRouteMatchResult("baz-id") + " and sub-id " + match.getStringRouteMatchResult("sub-id"));
                            }
                        })))
                .path("/maz", rf.builder()
                    .handler(new TestHandler("Hello, updated baz/maz!"))))
            .build());

        assertEquals(new TestRes("Hello, foo!"), match(r1, new TestReq("/foo")));
        assertEquals(new TestRes("Hello, baz!"), match(r1, new TestReq("/baz")));
        assertEquals(new TestRes("Hello, baz/maz!"), match(r1, new TestReq("/baz/maz")));
        assertEquals(new TestRes("Hello from baz with id test-123"), match(r1, new TestReq("/baz/test-123")));
        assertEquals(new TestRes("Hello from baz-sub with baz-id test-123 and sub-id hello"), match(r1, new TestReq("/baz/test-123/hello")));
        assertEquals(new TestRes("Hello from baz-sub zing with baz-id yo and sub-id dawg"), match(r1, new TestReq("/baz/yo/dawg/zing")));
        assertNull(match(r1, new TestReq("/bar")));


        assertEquals(new TestRes("Hello, foo!"), match(r2, new TestReq("/foo")));
        assertEquals(new TestRes("Hello, baz!"), match(r2, new TestReq("/baz")));
        assertEquals(new TestRes("Hello, updated baz/maz!"), match(r2, new TestReq("/baz/maz")));
        assertEquals(new TestRes("Hello from baz with id test-123"), match(r2, new TestReq("/baz/test-123")));
        assertEquals(new TestRes("Hello from updated baz-sub with baz-id test-123 and sub-id hello"), match(r2, new TestReq("/baz/test-123/hello")));
        assertEquals(new TestRes("Hello from baz-sub zing with baz-id yo and sub-id dawg"), match(r2, new TestReq("/baz/yo/dawg/zing")));
        assertEquals(new TestRes("Hello, bar!"), match(r2, new TestReq("/bar")));

    }

    @Test
    public void testSingleRouteBuilder() {
        RouteTreeNode<TestReq, TestRes> r1 = new SingleRouteBuilder<TestReq, TestRes>()
            .build(new TestHandler("Hello, root!"));

        RouteTreeNode<TestReq, TestRes> r2 = new SingleRouteBuilder<TestReq, TestRes>()
            .path("foo")
            .build(new TestHandler("Hello, foo!"));

        RouteTreeNode<TestReq, TestRes> r3 = new SingleRouteBuilder<TestReq, TestRes>()
            .path("foo")
            .path("bar")
            .build(new TestHandler("Hello, foo/bar!"));

        RouteTreeNode<TestReq, TestRes> r4 = new SingleRouteBuilder<TestReq, TestRes>()
            .path("foo")
            .path("bar")
            .path("baz")
            .build(new TestHandler("Hello, foo/bar/baz!"));

        RouteTreeNode<TestReq, TestRes> r5 = new SingleRouteBuilder<TestReq, TestRes>()
            .path("foo")
            .param("foo-id")
            .build(new IRouteHandler<TestReq, TestRes>() {
                @Override
                public IRouteHandler<TestReq, TestRes> merge(IRouteHandler<TestReq, TestRes> other) {
                    return other;
                }

                @Override
                public TestRes call(RouteMatch<TestReq> match) {
                    return new TestRes("Hello, foo with id " + match.getStringRouteMatchResult("foo-id"));
                }
            });

        RouteTreeNode<TestReq, TestRes> r = r1.merge(r2).merge(r3).merge(r4).merge(r5);

        assertEquals(new TestRes("Hello, root!"), match(r, new TestReq("/")));
        assertEquals(new TestRes("Hello, foo!"), match(r, new TestReq("/foo")));
        assertEquals(new TestRes("Hello, foo/bar!"), match(r, new TestReq("/foo/bar")));
        assertEquals(new TestRes("Hello, foo/bar/baz!"), match(r, new TestReq("/foo/bar/baz")));
        assertEquals(new TestRes("Hello, foo with id abc123"), match(r, new TestReq("/foo/abc123")));
    }

    @Test
    public void handlerMerging() {
        class MethodReq implements IRequest {
            private final String method;
            private final List<String> pathSegments;
            public MethodReq(String method, String path) {
                this.method = method;
                this.pathSegments = DefaultPathToPathSegments.parse(path);
            }

            @Override
            public List<String> getPathSegments() {
                return this.pathSegments;
            }

            public String getMethod() {
                return this.method;
            }
        }

        class MergingRouteHandler implements IRouteHandler<MethodReq, TestRes> {
            private final Map<String, TestRes> handlers;

            public MergingRouteHandler(String path, TestRes response) {
                Map<String, TestRes> theHandlers = new HashMap<String, TestRes>();
                theHandlers.put(path, response);
                this.handlers = Collections.unmodifiableMap(theHandlers);
            }

            public MergingRouteHandler(Map<String, TestRes> handlers) {
                this.handlers = Collections.unmodifiableMap(handlers);
            }

            @Override
            public IRouteHandler<MethodReq, TestRes> merge(IRouteHandler<MethodReq, TestRes> other) {
                if (other instanceof MergingRouteHandler) {
                    Map<String, TestRes> handlers = new HashMap<String, TestRes>();
                    handlers.putAll(this.handlers);
                    handlers.putAll(((MergingRouteHandler)other).handlers);
                    return new MergingRouteHandler(handlers);
                } else {
                    return null;
                }
            }

            @Override
            public TestRes call(RouteMatch<MethodReq> match) {
                if (this.handlers.containsKey(match.getRequest().getMethod())) {
                    return this.handlers.get(match.getRequest().getMethod());
                }

                return null;
            }
        }

        DefaultRouteMatcher<MethodReq, TestRes> drm = new DefaultRouteMatcher<MethodReq, TestRes>();



        RouteTreeNode<MethodReq, TestRes> r1 = new RouteTreeBuilder<MethodReq, TestRes>()
            .path("/foo", new RouteTreeBuilder<MethodReq, TestRes>()
                .handler(new MergingRouteHandler("GET", new TestRes("Responding to get"))))
            .build();

        RouteTreeNode<MethodReq, TestRes> r2 = new RouteTreeBuilder<MethodReq, TestRes>()
            .path("/foo", new RouteTreeBuilder<MethodReq, TestRes>()
                .handler(new MergingRouteHandler("POST", new TestRes("Responding to post"))))
            .build();

        RouteTreeNode<MethodReq, TestRes> r = r1.merge(r2);

        assertEquals(new TestRes("Responding to get"), drm.match(r, new MethodReq("GET", "/foo")));
        assertEquals(new TestRes("Responding to post"), drm.match(r, new MethodReq("POST", "/foo")));
    }
}
