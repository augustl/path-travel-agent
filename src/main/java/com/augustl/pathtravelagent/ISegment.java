package com.augustl.pathtravelagent;

public interface ISegment {
    public RouteMatchResult.IResult matchPathSegment(String pathSegment);
    public String getSegmentName();
    public boolean isParametric();
}