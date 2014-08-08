package com.augustl.pathtravelagent;

public interface IRouteHandler<T_REQ extends IRequest, T_RES> {
    public IRouteHandler<T_REQ, T_RES> merge(IRouteHandler<T_REQ, T_RES> other);
    public T_RES call(RouteMatch<T_REQ> match);
}
