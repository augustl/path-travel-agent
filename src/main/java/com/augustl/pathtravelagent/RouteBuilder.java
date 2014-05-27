package com.augustl.pathtravelagent;

import com.augustl.pathtravelagent.segment.NumberSegment;
import com.augustl.pathtravelagent.segment.PathSegment;
import com.augustl.pathtravelagent.segment.WildcardSegment;

import java.util.ArrayList;

public class RouteBuilder<T_REQ extends IRequest, T_RES> {
    private ArrayList<ISegment> segments = new ArrayList<ISegment>();

    public RouteBuilder<T_REQ, T_RES> pathSegment(String segment) {
        segments.add(new PathSegment(segment));
        return this;
    }

    public RouteBuilder<T_REQ, T_RES> numberSegment(String paramName) {
        segments.add(new NumberSegment(paramName));
        return this;
    }

    public RouteBuilder<T_REQ, T_RES> wildcardSegment(String paramName) {
        segments.add(new WildcardSegment(paramName));
        return this;
    }


    public RouteBuilder<T_REQ, T_RES> genericSegment(ISegment segment) {
        segments.add(segment);
        return this;
    }

    public Route<T_REQ, T_RES> build(IRouteHandler<T_REQ, T_RES> handler) {
        return new Route<T_REQ, T_RES>(segments, handler);
    }
}
