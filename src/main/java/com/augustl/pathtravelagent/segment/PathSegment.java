package com.augustl.pathtravelagent.segment;

import com.augustl.pathtravelagent.ISegment;
import com.augustl.pathtravelagent.RouteMatchResult;

public class PathSegment implements ISegment {
    private final String pathName;

    public PathSegment(String pathName) {
        this.pathName = pathName;
    }

    @Override
    public String getSegmentName() {
        return this.pathName;
    }

    @Override
    public RouteMatchResult.IResult matchPathSegment(String pathSegment) {
        if (pathSegment.equals(this.pathName)) {
            return RouteMatchResult.successResult;
        } else {
            return null;
        }
    }

    @Override
    public boolean isParametric() {
        return false;
    }
}
