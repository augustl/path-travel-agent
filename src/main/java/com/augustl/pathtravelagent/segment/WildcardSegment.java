package com.augustl.pathtravelagent.segment;

import com.augustl.pathtravelagent.ISegment;
import com.augustl.pathtravelagent.ISegmentParametric;
import com.augustl.pathtravelagent.RouteMatchResult;

public class WildcardSegment implements ISegmentParametric {
    private final String name;
    public WildcardSegment(String name) {
        if (name == null) {
            this.name = "*";
        } else {
            this.name = name;
        }
    }

    @Override
    public String getSegmentName() {
        return this.name;
    }

    @Override
    public RouteMatchResult.IResult matchPathSegment(String pathSegment) {
        if (this.name.equals("*")) {
            return RouteMatchResult.successResult;
        } else {
            return new RouteMatchResult.StringResult(pathSegment);
        }
    }
}
