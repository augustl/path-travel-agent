package com.augustl.pathtravelagent;

public class NumberSegment implements ISegment {
    private final String paramName;

    public NumberSegment(String paramName) {
        this.paramName = paramName;
    }

    @Override
    public int getPathHashCode() {
        return this.paramName.hashCode();
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
