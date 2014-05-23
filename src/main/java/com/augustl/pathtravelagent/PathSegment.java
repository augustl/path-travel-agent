package com.augustl.pathtravelagent;

public class PathSegment implements ISegment {
    private final String pathName;

    public PathSegment(String pathName) {
        this.pathName = pathName;
    }

    @Override
    public int getPathHashCode() {
        return this.pathName.hashCode();
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
}
