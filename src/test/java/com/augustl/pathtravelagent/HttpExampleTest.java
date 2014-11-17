package com.augustl.pathtravelagent;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class HttpExampleTest {
    private DefaultRouteMatcher<HttpReq, HttpRes> defaultRouteMatcher = new DefaultRouteMatcher<HttpReq, HttpRes>();
    private RouteTreeBuilderFactory<HttpReq, HttpRes> rf = new RouteTreeBuilderFactory<HttpReq, HttpRes>();

    private HttpRes match(RouteTreeNode<HttpReq, HttpRes> r, HttpReq req) {
        return defaultRouteMatcher.match(r, req);
    }

    @Test
    public void testVerbMatching() {
        RouteTreeNode<HttpReq, HttpRes> r = rf.builder()
            .path("/projects", rf.builder()
                .handler(new HttpHandler(new HashMap<String, HttpLambda>() {{
                    put("GET", new HttpLambda() {
                        @Override
                        public HttpRes call(RouteMatch<HttpReq> req) {
                            return new HttpRes("Hello, project list");
                        }
                    });
                    put("POST", new HttpLambda() {
                        @Override
                        public HttpRes call(RouteMatch<HttpReq> req) {
                            return new HttpRes("Created project");
                        }
                    });
                }}))
                .param("/:projectId", rf.builder()
                    .handler(new HttpHandler(new HashMap<String, HttpLambda>() {{
                        put("GET", new HttpLambda() {
                            @Override
                            public HttpRes call(RouteMatch<HttpReq> req) {
                                return new HttpRes("Hello, project " + req.getStringRouteMatchResult("projectId"));
                            }
                        });
                        put("PUT", new HttpLambda() {
                            @Override
                            public HttpRes call(RouteMatch<HttpReq> req) {
                                return new HttpRes("Updated project " + req.getStringRouteMatchResult("projectId"));
                            }
                        });
                    }}))))
            .build();

        assertEquals(new HttpRes("Hello, project list"), match(r, new HttpReq("GET", "/projects")));
        assertEquals(new HttpRes("Created project"), match(r, new HttpReq("POST", "/projects")));
        assertNull(match(r, new HttpReq("PUT", "/projects")));
        assertEquals(new HttpRes("Hello, project abc123"), match(r, new HttpReq("GET", "/projects/abc123")));
        assertEquals(new HttpRes("Updated project 789"), match(r, new HttpReq("PUT", "/projects/789")));
        assertNull(match(r, new HttpReq("POST", "/projects/789")));

    }

    private class HttpReq implements IRequest {
        private final String httpMethod;
        private final List<String> pathSegments;

        public HttpReq(String httpMethod, String path) {
            this.httpMethod = httpMethod;
            this.pathSegments = DefaultPathToPathSegments.parse(path);
        }

        public String getHttpMethod() {
            return this.httpMethod;
        }

        @Override
        public List<String> getPathSegments() {
            return this.pathSegments;
        }
    }

    private class HttpRes {
        private final String body;
        public HttpRes(String body) {
            this.body = body;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof HttpRes) {
                return this.body.equals(((HttpRes)obj).body);
            }

            return super.equals(obj);
        }

        @Override
        public String toString() {
            return super.toString() + ": " + this.body;
        }
    }

    private interface HttpLambda {
        public HttpRes call(RouteMatch<HttpReq> req);
    }

    private class HttpHandler implements IRouteHandler<HttpReq, HttpRes> {
        private final Map<String,HttpLambda> handlerByMethod;
        public HttpHandler(Map<String,HttpLambda> handlerByMethod) {
            this.handlerByMethod = handlerByMethod;
        }

        @Override
        public IRouteHandler<HttpReq, HttpRes> merge(IRouteHandler<HttpReq, HttpRes> other) {
            return other;
        }

        @Override
        public HttpRes call(RouteMatch<HttpReq> match) {
            String method = match.getRequest().getHttpMethod();
            if (handlerByMethod.containsKey(method)) {
                return handlerByMethod.get(method).call(match);
            }
            return null;
        }
    }
}
