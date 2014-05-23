package com.augustl.pathtravelagent;

import java.util.List;

public class Route<T_REQ extends IRequest, T_RES> {
    private final List<ISegment> segments;
    private final RouteHandler<T_REQ, T_RES> handler;

    public Route(List<ISegment> segments, RouteHandler<T_REQ, T_RES> handler) {
        this.segments = segments;
        this.handler = handler;
    }

    public int getRouteHashCode() {
        return this.segments.get(0).getPathHashCode();
    }

    public List<ISegment> getSegments() {
        return this.segments;
    }

    public T_RES match(T_REQ req, RouteMatchResult routeMatchResult) {
        return this.handler.call(new RouteMatch<T_REQ>(req, routeMatchResult));
    }
}
