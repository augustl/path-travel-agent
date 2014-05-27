package com.augustl.pathtravelagent;

public class RouteStringBuilder<T_REQ extends IRequest, T_RES> {
    private final String paramIndicator;

    public RouteStringBuilder(String paramIndicator) {
        this.paramIndicator = paramIndicator;
    }

    public Route<T_REQ, T_RES> build(String unparsedPath, IRouteHandler<T_REQ, T_RES> handler) {
        RouteBuilder<T_REQ, T_RES> routeBuilder = new RouteBuilder<T_REQ, T_RES>();

        if (unparsedPath.startsWith("/")) {
            unparsedPath = unparsedPath.substring(1);
        }

        if (unparsedPath.length() == 0) {
            return routeBuilder.build(handler);
        }

        String[] pathSegments = unparsedPath.split("/");
        for (String pathSegment : pathSegments) {
            if (pathSegment.startsWith(this.paramIndicator)) {
                routeBuilder = routeBuilder.arbitraryParamSegment(pathSegment.substring(this.paramIndicator.length()));
            } else if (pathSegment.startsWith("*")) {
                if (pathSegment.length() == 1) {
                    routeBuilder = routeBuilder.wildcardSegment(null);
                } else {
                    routeBuilder = routeBuilder.wildcardSegment(pathSegment.substring(1));
                }
            } else {
                routeBuilder = routeBuilder.pathSegment(pathSegment);
            }
        }

        return routeBuilder.build(handler);
    }
}
