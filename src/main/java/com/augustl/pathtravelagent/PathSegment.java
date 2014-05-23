package com.augustl.pathtravelagent;

public class PathSegment implements ISegment {
    private final String pathName;

    public PathSegment(String pathName) {
        this.pathName = pathName;
    }

    @Override
    public int getPathHashCode() {
        return this.pathName.hashCode();
    }

    @Override
    public Object matchPathSegment(String pathSegment) {
        return pathSegment.equals(this.pathName);
    }
}
