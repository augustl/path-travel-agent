package com.augustl.pathtravelagent;

import java.util.HashMap;

public class RouteMatchResult {
    public static SuccessResult successResult = new SuccessResult();
    private final HashMap<String, Integer> integerMatches = new HashMap<String, Integer>();
    private final HashMap<String, String> stringMatches = new HashMap<String, String>();

    public boolean addPossibleMatch(String segmentName, IResult result) {
        if (result == null) {
            return false;
        }

        result.addToMatchResult(segmentName, this);
        return result.isSuccess();
    }

    public void addToIntegerMatches(String pathSegment, Integer val) {
        this.integerMatches.put(pathSegment, val);
    }

    public Integer getIntegerMatch(String pathSegment) {
        return this.integerMatches.get(pathSegment);
    }

    public void addToStringMatches(String pathSegment, String val) {
        this.stringMatches.put(pathSegment, val);
    }

    public String getStringMatch(String pathSegment) {
        return this.stringMatches.get(pathSegment);
    }

    public static interface IResult {
        public boolean isSuccess();
        public void addToMatchResult(String segmentName, RouteMatchResult res);
    }

    private static class SuccessResult implements IResult {
        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public void addToMatchResult(String segmentName, RouteMatchResult res) {

        }
    }

    public static class IntegerResult implements IResult {
        private final Integer val;
        public IntegerResult(Integer val) {
            this.val = val;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public void addToMatchResult(String segmentName, RouteMatchResult res) {
            res.addToIntegerMatches(segmentName, this.val);
        }
    }

    public static class StringResult implements IResult {
        private final String val;
        public StringResult(String val) {
            this.val = val;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public void addToMatchResult(String segmentName, RouteMatchResult res) {
            res.addToStringMatches(segmentName, this.val);
        }
    }
}
