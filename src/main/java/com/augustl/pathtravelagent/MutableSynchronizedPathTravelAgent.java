package com.augustl.pathtravelagent;


public class MutableSynchronizedPathTravelAgent<T_REQ extends IRequest, T_RES> {
    private final RouteTreeNode<Route<T_REQ, T_RES>, T_REQ, T_RES> routeTreeRoot;

    public MutableSynchronizedPathTravelAgent() {
        this.routeTreeRoot = new RouteTreeNode<Route<T_REQ, T_RES>, T_REQ, T_RES>();
    }

    public synchronized void addRoute(Route<T_REQ, T_RES> route) {
        this.routeTreeRoot.addRoute(route);
    }

    public synchronized T_RES match(T_REQ req) {
        return this.routeTreeRoot.match(req);
    }
}
