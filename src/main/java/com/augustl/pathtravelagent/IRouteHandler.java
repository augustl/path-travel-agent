package com.augustl.pathtravelagent;

public interface IRouteHandler<T_REQ extends IRequest, T_RES> {
    public T_RES call(RouteMatch<T_REQ> match);
}
