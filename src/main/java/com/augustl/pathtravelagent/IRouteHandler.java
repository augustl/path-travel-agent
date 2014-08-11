package com.augustl.pathtravelagent;

/**
 * The actual request handler, responsible for taking requests, T_REQ, and returning responses, T_RES. Also responsible
 * for merging itself with other handlers.
 *
 * @param <T_REQ> A request object, implementing IRequest.
 * @param <T_RES> The return value for the handler. Can be any type you want, not used for anything by PathTravelAgent.
 */
public interface IRouteHandler<T_REQ extends IRequest, T_RES> {
    /**
     * Note that since a handler is immutable, care must be taken to not modify either this handler or the handler that
     * it's being merged with when a merge is performed. A brand new handler must be created.
     *
     * @param other The other handler to merge with this handler
     * @return The new handler that was created from merging this handler with another handler.
     * @see RouteTreeNode#merge
     */
    public IRouteHandler<T_REQ, T_RES> merge(IRouteHandler<T_REQ, T_RES> other);
    public T_RES call(RouteMatch<T_REQ> match);
}
