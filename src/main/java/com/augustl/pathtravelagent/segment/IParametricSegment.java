package com.augustl.pathtravelagent.segment;

import com.augustl.pathtravelagent.RouteMatchResult;

public interface IParametricSegment {
    public String getParamName();
    public RouteMatchResult.IResult getValue(String rawValue);
}
