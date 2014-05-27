package com.augustl.pathtravelagent.segment;

import com.augustl.pathtravelagent.ISegmentParametric;
import com.augustl.pathtravelagent.RouteMatchResult;

public class ArbitraryParamSegment implements ISegmentParametric {
    private final String paramName;

    public ArbitraryParamSegment(String paramName) {
        this.paramName = paramName;
    }

    @Override
    public String getSegmentName() {
        return this.paramName;
    }

    @Override
    public RouteMatchResult.IResult matchPathSegment(String pathSegment) {
        return new RouteMatchResult.StringResult(pathSegment);
    }
}
