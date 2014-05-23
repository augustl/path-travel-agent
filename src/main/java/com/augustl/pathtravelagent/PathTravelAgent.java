package com.augustl.pathtravelagent;

import java.util.*;

public class PathTravelAgent<T_REQ extends IRequest, T_RES> {
    private final HashMap<Integer, RouteSet<Route<T_REQ, T_RES>, T_REQ, T_RES>> routes;

    public PathTravelAgent(List<Route<T_REQ, T_RES>> routes) {
        this.routes = new HashMap<Integer, RouteSet<Route<T_REQ, T_RES>, T_REQ, T_RES>>(routes.size());
        for (Route<T_REQ, T_RES> route : routes) {
            int hashCode = route.getRouteHashCode();
            if (!this.routes.containsKey(hashCode)) {
                this.routes.put(hashCode, new RouteSet<Route<T_REQ, T_RES>, T_REQ, T_RES>());
            }

            this.routes.get(hashCode).addRoute(route);
        }
    }

    public T_RES match(T_REQ req) {
        String[] pathSegmentsAry = req.getPath().split("/");
        List<String> pathSegments = Arrays.asList(pathSegmentsAry).subList(1, pathSegmentsAry.length);

        RouteSet<Route<T_REQ, T_RES>, T_REQ, T_RES> routeSet = this.routes.get(pathSegments.get(0).hashCode());
        if (routeSet == null) return null;

        return routeSet.match(req, pathSegments);
    }

    public static class Builder<TT_REQ extends IRequest, TT_RES> {
        private final List<Route<TT_REQ, TT_RES>> routes = new ArrayList<Route<TT_REQ, TT_RES>>();
        private RouteBuilder<TT_REQ, TT_RES> currentRouteBuilder;
        private Builder(){}

        static public <TTT_REQ extends IRequest, TTT_RES> Builder<TTT_REQ, TTT_RES> start() {
            return new Builder<TTT_REQ, TTT_RES>();
        }

        public Builder<TT_REQ, TT_RES> addRoute(Route<TT_REQ, TT_RES> route) {
            this.routes.add(route);
            return this;
        }

        public Builder<TT_REQ, TT_RES> newRoute() {
            if (this.currentRouteBuilder != null) {
                throw new IllegalStateException("Cannot create new route when route building is in progress");
            }

            this.currentRouteBuilder = new RouteBuilder<TT_REQ, TT_RES>();
            return this;
        }

        public Builder<TT_REQ, TT_RES> pathSegment(String seg) {
            this.ensureIsBuildingRoute();
            this.currentRouteBuilder = this.currentRouteBuilder.pathSegment(seg);
            return this;
        }

        public Builder<TT_REQ, TT_RES> buildRoute(RouteHandler<TT_REQ, TT_RES> handler) {
            this.ensureIsBuildingRoute();
            this.routes.add(this.currentRouteBuilder.build(handler));
            this.currentRouteBuilder = null;
            return this;
        }

        public PathTravelAgent<TT_REQ, TT_RES> build() {
            return new PathTravelAgent<TT_REQ, TT_RES>(this.routes);
        }

        private void ensureIsBuildingRoute() {
            if (this.currentRouteBuilder == null) {
                throw new IllegalStateException("Cannot call build route without an active route builder.");
            }
        }
    }
}
