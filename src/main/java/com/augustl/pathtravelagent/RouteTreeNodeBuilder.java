package com.augustl.pathtravelagent;

import com.augustl.pathtravelagent.segment.IParametricSegment;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RouteTreeNodeBuilder<T_REQ extends IRequest, T_RES> {
    private final Pattern validSegmentChars = Pattern.compile("[\\w\\-\\._~]+");

    private IRouteHandler<T_REQ, T_RES> handler;
    private HashMap<String, RouteTreeNode<T_REQ, T_RES>> pathSegmentChildNodes = new HashMap<String, RouteTreeNode<T_REQ, T_RES>>();
    private ParametricChild<T_REQ, T_RES> parametricChild;
    private RouteTreeNode<T_REQ, T_RES> wildcardChild;

    public void setHandler(IRouteHandler<T_REQ, T_RES> handler) {
        this.handler = handler;
    }

    public void addPathSegmentChild(String pathSegment, RouteTreeNode<T_REQ, T_RES> childNode) {
        ensureContainsValidSegmentChars(pathSegment);
        this.pathSegmentChildNodes.put(pathSegment, childNode);
    }

    public synchronized void setParametricChild(IParametricSegment parametricSegment, RouteTreeNode<T_REQ, T_RES> childNode) {
        if (parametricChild != null) {
            throw new IllegalStateException("Cannot assign parametric child, already has one");
        }

        parametricChild = new ParametricChild<T_REQ, T_RES>(parametricSegment, childNode);
    }

    public synchronized  void setWildcardChild(RouteTreeNode<T_REQ, T_RES> childNode) {
        if (wildcardChild != null) {
            throw new IllegalStateException("Cannot assign wildcard child, already has one");
        }

        wildcardChild = childNode;
    }

    public RouteTreeNode<T_REQ, T_RES> createNode(String label) {
        return new RouteTreeNode<T_REQ, T_RES>(
            label,
            this.handler,
            this.pathSegmentChildNodes,
            this.parametricChild,
            this.wildcardChild);
    }

    private void ensureContainsValidSegmentChars(String str) {
        Matcher matcher = validSegmentChars.matcher(str);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Param " + str + " contains invalid characters");
        }
    }
}
