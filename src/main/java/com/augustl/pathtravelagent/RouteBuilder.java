package com.augustl.pathtravelagent;

import java.util.ArrayList;

public class RouteBuilder<T_REQ extends IRequest, T_RES> {
    private ArrayList<ISegment> segments = new ArrayList<ISegment>();

    public RouteBuilder<T_REQ, T_RES> pathSegment(String segment) {
        segments.add(new PathSegment(segment));
        return this;
    }

    public Route<T_REQ, T_RES> build(RouteHandler<T_REQ, T_RES> handler) {
        return new Route<T_REQ, T_RES>(segments, handler);
    }
}
