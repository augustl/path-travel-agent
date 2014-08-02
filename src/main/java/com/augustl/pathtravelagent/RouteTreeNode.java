package com.augustl.pathtravelagent;

import com.augustl.pathtravelagent.segment.IParametricSegment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RouteTreeNode<T_REQ extends IRequest, T_RES> {
    private final Pattern validSegmentChars = Pattern.compile("[\\w\\-\\._~]+");
    private IRouteHandler<T_REQ, T_RES> handler;
    private HashMap<String, RouteTreeNode<T_REQ, T_RES>> pathSegmentChildNodes = new HashMap<String, RouteTreeNode<T_REQ, T_RES>>();
    private ParametricChild parametricChild;
    private WildcardChild wildcardChild;

    public T_RES match(T_REQ req) {
        List<String> pathSegments = req.getPathSegments();
        RouteTreeNode<T_REQ, T_RES> targetNode = this;
        RouteMatchResult routeMatchResult = new RouteMatchResult();

        int i;
        for (i = 0; i < pathSegments.size(); i++) {
            String pathSegment = pathSegments.get(i);

            if (targetNode.pathSegmentChildNodes.containsKey(pathSegment)) {
                targetNode = targetNode.pathSegmentChildNodes.get(pathSegment);
                continue;
            }

            if (targetNode.parametricChild != null) {
                if (!routeMatchResult.addParametricSegment(targetNode.parametricChild.parametricSegment, pathSegment)) {
                    return null;
                }
                targetNode = targetNode.parametricChild.childNode;
                continue;
            }

            if (targetNode.wildcardChild != null) {
                for (; i < pathSegments.size(); i++) {
                    pathSegment = pathSegments.get(i);
                    routeMatchResult.addToWildcardMatches(pathSegment);
                }
                targetNode = targetNode.wildcardChild.childNode;
                break;
            }

            return null;
        }

        if (targetNode == null) {
            return null;
        }

        if (targetNode.handler == null) {
            return null;
        }

        return targetNode.handler.call(new RouteMatch<T_REQ>(req, routeMatchResult));
    }

    public void setHandler(IRouteHandler<T_REQ, T_RES> handler) {
        this.handler = handler;
    }

    public void addPathSegmentChild(String pathSegment, RouteTreeNode<T_REQ, T_RES> childNode) {
        ensureContainsValidSegmentChars(pathSegment);
        pathSegmentChildNodes.put(pathSegment, childNode);
    }

    public synchronized void setParametricChild(IParametricSegment parametricSegment, RouteTreeNode<T_REQ, T_RES> childNode) {
        if (parametricChild != null) {
            throw new IllegalStateException("Cannot assign parametric child, already has one");
        }
        parametricChild = new ParametricChild(parametricSegment, childNode);
    }

    public synchronized  void setWildcardChild(String paramName, RouteTreeNode<T_REQ, T_RES> childNode) {
        if (wildcardChild != null) {
            throw new IllegalStateException("Cannot assign wildcard child, already has one");
        }

        wildcardChild = new WildcardChild(paramName, childNode);
    }

    private void ensureContainsValidSegmentChars(String str) {
        Matcher matcher = validSegmentChars.matcher(str);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Param " + str + " contains invalid characters");
        }
    }

    private class ParametricChild {
        private final IParametricSegment parametricSegment;
        private final RouteTreeNode<T_REQ, T_RES> childNode;
        public ParametricChild(IParametricSegment parametricSegment, RouteTreeNode<T_REQ, T_RES> childNode) {
            this.parametricSegment = parametricSegment;
            this.childNode = childNode;
        }
    }

    private class WildcardChild {
        private final String paramName;
        private final RouteTreeNode<T_REQ, T_RES> childNode;

        public WildcardChild(String paramName, RouteTreeNode<T_REQ, T_RES> childNode) {
            this.paramName = paramName;
            this.childNode = childNode;
        }
    }
}