package com.augustl.pathtravelagent;

import java.util.List;

public class RouteMatch<T_REQ extends IRequest> {
    private final T_REQ req;
    private final RouteMatchResult routeMatchResult;

    public RouteMatch(T_REQ req, RouteMatchResult routeMatchResult) {
        this.req = req;
        this.routeMatchResult = routeMatchResult;
    }

    public T_REQ getRequest() {
        return this.req;
    }

    public Integer getIntegerRouteMatchResult(String paramName) {
        return this.routeMatchResult.getIntegerMatch(paramName);
    }

    public String getStringRouteMatchResult(String paramName) {
        return this.routeMatchResult.getStringMatch(paramName);
    }

    public List<String> getWildcardRouteMatchResult() {
        return this.routeMatchResult.getWildcardMatches();
    }
}
