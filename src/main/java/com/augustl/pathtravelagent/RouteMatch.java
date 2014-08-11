package com.augustl.pathtravelagent;

import java.util.List;

/**
 * <p>The value passed to handlers when they match a path. Contains information obtained from the URL.</p>
 *
 * <p>Parametric values in the URL can be obtained from this object. For type safety, different methods are
 * exposed for different types. Call the correct method with the correct param name depending on the type that
 * was defined when the route was created.</p>
 *
 * <p>The various route builders defaults to strings when no type has been specified.</p>
 *
 * @param <T_REQ> A request object, implementing IRequest. Used to access the raw request object used for matching.
 */
public class RouteMatch<T_REQ extends IRequest> {
    private final T_REQ req;
    private final RouteMatchResult routeMatchResult;

    public RouteMatch(T_REQ req, RouteMatchResult routeMatchResult) {
        this.req = req;
        this.routeMatchResult = routeMatchResult;
    }

    /**
     * @return The raw request object
     */
    public T_REQ getRequest() {
        return this.req;
    }

    /**
     * @param paramName The name used when defining the route
     * @return The integer value associated with the paramName
     */
    public Integer getIntegerRouteMatchResult(String paramName) {
        return this.routeMatchResult.getIntegerMatch(paramName);
    }

    /**
     * @param paramName The name used when defining the route
     * @return The string value associated with the paramName
     */
    public String getStringRouteMatchResult(String paramName) {
        return this.routeMatchResult.getStringMatch(paramName);
    }

    public RouteMatchResult getRouteMatchResult() {
        return this.routeMatchResult;
    }

    public List<String> getWildcardRouteMatchResult() {
        return this.routeMatchResult.getWildcardMatches();
    }
}
