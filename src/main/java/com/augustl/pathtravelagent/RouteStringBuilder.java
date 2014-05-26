package com.augustl.pathtravelagent;

public class RouteStringBuilder<T_REQ extends IRequest, T_RES> {
    public Route<T_REQ, T_RES> build(String unparsedPath, IRouteHandler<T_REQ, T_RES> handler) {
        if (unparsedPath.startsWith("/")) {
            unparsedPath = unparsedPath.substring(1);
        }

        RouteBuilder<T_REQ, T_RES> routeBuilder = new RouteBuilder<T_REQ, T_RES>();

        if (unparsedPath.length() == 0) {
            return routeBuilder.build(handler);
        }

        String[] pathSegments = unparsedPath.split("/");
        for (String pathSegment : pathSegments) {
            if (pathSegment.startsWith("$")) {
                routeBuilder = routeBuilder.numberSegment(pathSegment.substring(1));
            } else {
                routeBuilder = routeBuilder.pathSegment(pathSegment);
            }
        }

        return routeBuilder.build(handler);
    }
}
