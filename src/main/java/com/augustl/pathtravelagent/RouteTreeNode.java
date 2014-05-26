package com.augustl.pathtravelagent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RouteTreeNode<T_ROUTE extends Route<T_REQ, T_RES>, T_REQ extends IRequest, T_RES> {
    private final HashMap<String, RouteTreeNode<T_ROUTE, T_REQ, T_RES>> namedChildren = new HashMap<String, RouteTreeNode<T_ROUTE, T_REQ, T_RES>>();
    private final HashMap<String, Pair<ISegmentParametric, RouteTreeNode<T_ROUTE, T_REQ, T_RES>>> parametricChildren = new HashMap<String, Pair<ISegmentParametric, RouteTreeNode<T_ROUTE, T_REQ, T_RES>>>();
    private Route<T_REQ, T_RES> route;

    private class Pair<A, B> {
        private final A a;
        private final B b;
        public Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }
        public A getA() { return this.a; }
        public B getB() { return this.b; }
    }

    public void addRoute(Route<T_REQ, T_RES> route) {
        RouteTreeNode<T_ROUTE, T_REQ, T_RES> deepNode = this;
        for (ISegment segment : route.getSegments()) {
            deepNode = deepNode.proceed(segment);
        }

        if (deepNode.hasRoute()) {
            throw new IllegalStateException("There is already a route for " + route.getSegments());
        }
        deepNode.setRoute(route);
    }

    public RouteTreeNode<T_ROUTE, T_REQ, T_RES> proceed(ISegment segment) {
        String segmentName = segment.getSegmentName();

        if (segment instanceof ISegmentParametric) {
            if (parametricChildren.containsKey(segmentName)) {
                return parametricChildren.get(segmentName).b;
            } else {
                RouteTreeNode<T_ROUTE, T_REQ, T_RES> child = new RouteTreeNode<T_ROUTE, T_REQ, T_RES>();
                parametricChildren.put(segmentName, new Pair<ISegmentParametric, RouteTreeNode<T_ROUTE, T_REQ, T_RES>>((ISegmentParametric)segment, child));
                return child;
            }
        } else {
            if (namedChildren.containsKey(segmentName)) {
                return namedChildren.get(segmentName);
            } else {
                RouteTreeNode<T_ROUTE, T_REQ, T_RES> child = new RouteTreeNode<T_ROUTE, T_REQ, T_RES>();
                namedChildren.put(segmentName, child);
                return child;
            }
        }
    }

    private void setRoute(Route<T_REQ, T_RES> route) {
        this.route = route;
    }

    private Route<T_REQ, T_RES> getRoute() {
        return this.route;
    }

    private boolean hasRoute() {
        return this.route != null;
    }

    public T_RES match(T_REQ req, List<String> pathSegments) {
        RouteMatchResult routeMatchResult = new RouteMatchResult();

        RouteTreeNode<T_ROUTE, T_REQ, T_RES> deepNode = this;
        for (String pathSegment : pathSegments) {
            if (deepNode.namedChildren.containsKey(pathSegment)) {
                deepNode = deepNode.namedChildren.get(pathSegment);
            } else {
                deepNode = deepNode.getParametricChildrenMatch(pathSegment, routeMatchResult);
            }
            if (deepNode == null) {
                return null;
            }
        }

        Route<T_REQ, T_RES> route = deepNode.getRoute();
        if (route == null) {
            return null;
        }

        return route.match(req, routeMatchResult);
    }

    private  RouteTreeNode<T_ROUTE, T_REQ, T_RES> getParametricChildrenMatch(String pathSegment, RouteMatchResult routeMatchResult) {
        for (String key : parametricChildren.keySet()) {
            Pair<ISegmentParametric, RouteTreeNode<T_ROUTE, T_REQ, T_RES>> pair = parametricChildren.get(key);
            RouteMatchResult.IResult matchResult = pair.a.matchPathSegment(pathSegment);
            boolean matched = routeMatchResult.addPossibleMatch(pair.a.getSegmentName(), matchResult);
            if (matched) {
                return pair.b;
            } else {
                return null;
            }
        }

        return null;
    }
}
