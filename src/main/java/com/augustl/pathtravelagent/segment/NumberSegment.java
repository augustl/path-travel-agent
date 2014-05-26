package com.augustl.pathtravelagent.segment;

import com.augustl.pathtravelagent.ISegmentParametric;
import com.augustl.pathtravelagent.RouteMatchResult;

public class NumberSegment implements ISegmentParametric {
    private final String paramName;

    public NumberSegment(String paramName) {
        this.paramName = paramName;
    }

    @Override
    public String getSegmentName() {
        return this.paramName;
    }

    @Override
    public RouteMatchResult.IResult matchPathSegment(String pathSegment) {
        try {
            Integer parsed = Integer.parseInt(pathSegment, 10);
            return new RouteMatchResult.IntegerResult(parsed);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
