package com.augustl.pathtravelagent;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import org.junit.BeforeClass;
import org.junit.Test;
import spark.route.HttpMethod;
import spark.route.SimpleRouteMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PathTravelAgentBenchmark extends AbstractBenchmark {
    static PathTravelAgent<TestReq, TestRes> largeRouter;
    static SimpleRouteMatcher sparkRouter;

    private static final String[] routePaths = {
        "/",
        "/projects",
        "/projects/$projectId",
        "/projects/$projectId/todos",
        "/projects/$projectId/todos/$todoId",
        "/projects/$projectId/files",
        "/projects/$projectId/files/$fileid",
        "/projects/$projectId/messages",
        "/projects/$projectId/messages/$messageId",
        "/projects/$projectId/messages/$messageId/replies",
        "/projects/$projectId/messages/$messageId/replies/$replyId",
        "/projects/$projectId/permissions",
        "/projects/$projectId/permissions/$permissionId",
    };
    private static final int COUNT = 50000;
    private static final List<TestReq> requests = new ArrayList<TestReq>(COUNT);

    @BeforeClass
    public static void createLargeRouter() {
        List<Route<TestReq, TestRes>> routes = new ArrayList<Route<TestReq, TestRes>>();
        for (String routePath : routePaths) {
            routes.add(new RouteStringBuilder<TestReq, TestRes>().build(routePath, new TestHandler()));
        }
        largeRouter = new PathTravelAgent<TestReq, TestRes>(routes);

        final Random random = new Random();
        for (int i = 0; i < COUNT; i++) {
            requests.add(i, new TestReq(routePaths[random.nextInt(routePaths.length)].replaceAll("\\/\\$\\w+", "/123")));
        }
    }

    @BeforeClass
    public static void createSparkRouter() {
        sparkRouter = new SimpleRouteMatcher();
        for (String routePath : routePaths) {
            sparkRouter.parseValidateAddRoute("GET'" + routePath.replaceAll("\\$", "\\:"), null, null);
        }
    }

    @Test
    public void largeRouter() {
        for (TestReq req : requests) {
            largeRouter.match(req);
        }
    }

    @Test
    public void sparkRouter() {
        for (TestReq req : requests) {
            sparkRouter.findTargetForRequestedRoute(HttpMethod.get, req.getPath(), null);
        }
    }
}
