package com.augustl.pathtravelagent;

import com.augustl.pathtravelagent.segment.IParametricSegment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteMatchResult {
    private final HashMap<String, Integer> integerMatches = new HashMap<String, Integer>();
    private final HashMap<String, String> stringMatches = new HashMap<String, String>();
    private final ArrayList<String> wildcardMatches = new ArrayList<String>();

    public boolean addParametricSegment(IParametricSegment parametricSegment, String rawValue) {
        IResult value = parametricSegment.getValue(rawValue);
        if (value == null) {
            return false;
        }

        value.addToMatchResult(parametricSegment.getParamName(), this);
        return value.isSuccess();
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

    public void addToWildcardMatches(String pathSegment) {
        this.wildcardMatches.add(pathSegment);
    }

    public List<String> getWildcardMatches() {
        return this.wildcardMatches;
    }

    public Map<String,Integer> getIntegerMatches() {
        return new HashMap<String, Integer>(this.integerMatches);
    }

    public Map<String, String> getStringMatches() {
        return new HashMap<String, String>(this.stringMatches);
    }

    public static interface IResult {
        public boolean isSuccess();
        public void addToMatchResult(String paramName, RouteMatchResult res);
    }

//    private static class SuccessResult implements IResult {
//        @Override
//        public boolean isSuccess() {
//            return true;
//        }
//
//        @Override
//        public void addToMatchResult(String paramName, RouteMatchResult res) {
//
//        }
//    }

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
        public void addToMatchResult(String paramName, RouteMatchResult res) {
            res.addToIntegerMatches(paramName, this.val);
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
