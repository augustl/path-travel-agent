package com.augustl.pathtravelagent;

import java.util.List;

/**
 * <p>The default implementation of taking a RouteTreeNode and a request and returning a response.</p>
 *
 * @param <T_REQ> A request object, implementing IRequest.
 * @param <T_RES> The return value for the handler. Can be any type you want, not used for anything by PathTravelAgent.
 */
public class DefaultRouteMatcher<T_REQ extends IRequest, T_RES> {
    public T_RES match(final RouteTreeNode<T_REQ, T_RES> rootNode, T_REQ req) {
        List<String> pathSegments = req.getPathSegments();
        RouteTreeNode<T_REQ, T_RES> targetNode = rootNode;
        RouteMatchResult routeMatchResult = new RouteMatchResult();

        int i;
        for (i = 0; i < pathSegments.size(); i++) {
            String pathSegment = pathSegments.get(i);

            if (targetNode.containsPathSegmentChildNodes(pathSegment)) {
                targetNode = targetNode.getPathSegmentChildNode(pathSegment);
                continue;
            }

            if (targetNode.hasParametricChild()) {
                if (!routeMatchResult.addParametricSegment(targetNode.getParametricChildSegment(), pathSegment)) {
                    return null;
                }
                targetNode = targetNode.getParametricChildNode();
                continue;
            }

            if (targetNode.hasWildcardChild()) {
                for (; i < pathSegments.size(); i++) {
                    pathSegment = pathSegments.get(i);
                    routeMatchResult.addToWildcardMatches(pathSegment);
                }
                targetNode = targetNode.getWildcardChildNode();
                break;
            }

            return null;
        }

        if (targetNode == null) {
            return null;
        }

        if (targetNode.getHandler() == null) {
            return null;
        }

        return targetNode.getHandler().call(new RouteMatch<T_REQ>(req, routeMatchResult));
    }
}
