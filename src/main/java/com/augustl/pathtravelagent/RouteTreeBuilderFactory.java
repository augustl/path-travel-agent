package com.augustl.pathtravelagent;

public class RouteTreeBuilderFactory<T_REQ extends IRequest, T_RES> {
    public RouteTreeBuilder<T_REQ, T_RES> builder() {
        return new RouteTreeBuilder<T_REQ, T_RES>();
    }
}
