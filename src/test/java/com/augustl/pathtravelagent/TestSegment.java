package com.augustl.pathtravelagent;

import com.augustl.pathtravelagent.segment.IParametricSegment;

class TestSegment implements IParametricSegment {
    private final String paramName;
    private final String requiredValue;
    public TestSegment(String paramName, String requiredValue) {
        this.paramName = paramName;
        this.requiredValue = requiredValue;
    }

    @Override
    public String getParamName() {
        return this.paramName;
    }

    @Override
    public RouteMatchResult.IResult getValue(String rawValue) {
        if (rawValue.startsWith(this.requiredValue)) {
            return new RouteMatchResult.StringResult(rawValue);
        } else {
            return null;
        }
    }
}
