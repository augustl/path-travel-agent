package com.augustl.pathtravelagent;

public interface ISegmentParametric extends ISegment {
    public RouteMatchResult.IResult matchPathSegment(String pathSegment);
}
