package com.augustl.pathtravelagent;

public interface ISegment {
    public int getPathHashCode();
    public Object matchPathSegment(String pathSegment);
}