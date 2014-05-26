package com.augustl.pathtravelagent.segment;

import com.augustl.pathtravelagent.ISegment;
import com.augustl.pathtravelagent.RouteMatchResult;

public class PathSegment implements ISegment {
    private final String pathName;

    public PathSegment(String pathName) {
        this.pathName = pathName;
    }

    @Override
    public String getSegmentName() {
        return this.pathName;
    }
}
