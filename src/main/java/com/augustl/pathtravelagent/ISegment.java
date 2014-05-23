package com.augustl.pathtravelagent;

public interface ISegment {
    public int getPathHashCode();
    public RouteMatchResult.IResult matchPathSegment(String pathSegment);
    public String getSegmentName();
}