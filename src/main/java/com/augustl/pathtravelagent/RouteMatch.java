package com.augustl.pathtravelagent;

import java.util.List;

public class RouteMatch<T_REQ extends IRequest> {
    private final T_REQ req;

    public RouteMatch(T_REQ req, List<String> pathSegments) {
        this.req = req;
    }

    public T_REQ getRequest() {
        return this.req;
    }
}
