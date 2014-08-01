package com.augustl.pathtravelagent.segment;

import com.augustl.pathtravelagent.RouteMatchResult;

public class StringSegment implements IParametricSegment {
    private final String paramName;

    public StringSegment(String paramName) {
        this.paramName = paramName;
    }

    @Override
    public String getParamName() {
        return this.paramName;
    }

    @Override
    public RouteMatchResult.IResult getValue(String rawValue) {
        return new RouteMatchResult.StringResult(rawValue);
    }
}
