package com.augustl.pathtravelagent;

class TestSegment implements ISegmentParametric {
    private final String paramName;
    private final String requiredValue;
    public TestSegment(String paramName, String requiredValue) {
        this.paramName = paramName;
        this.requiredValue = requiredValue;
    }

    @Override
    public RouteMatchResult.IResult matchPathSegment(String pathSegment) {
        if (pathSegment.startsWith(this.requiredValue)) {
            return new RouteMatchResult.StringResult(pathSegment);
        } else {
            return null;
        }
    }

    @Override
    public String getSegmentName() {
        return this.paramName;
    }
}
