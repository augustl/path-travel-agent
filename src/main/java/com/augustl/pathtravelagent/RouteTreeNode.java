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
    private RouteTreeNode<T_REQ, T_RES> wildcardChild;

    public boolean containsPathSegmentChildNodes(String pathSegment) {
        return this.pathSegmentChildNodes.containsKey(pathSegment);
    }

    public RouteTreeNode<T_REQ, T_RES> getPathSegmentChildNode(String pathSegment) {
        return this.pathSegmentChildNodes.get(pathSegment);
    }

    public boolean hasParametricChild() {
        return this.parametricChild != null;
    }

    public IParametricSegment getParametricChildSegment() {
        return this.parametricChild.parametricSegment;
    }

    public RouteTreeNode<T_REQ, T_RES> getParametricChildNode() {
        return this.parametricChild.childNode;
    }

    public boolean hasWildcardChild() {
        return this.wildcardChild != null;
    }

    public RouteTreeNode<T_REQ, T_RES> getWildcardChildNode() {
        return this.wildcardChild;
    }

    public IRouteHandler<T_REQ, T_RES> getHandler() {
        return handler;
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

    public synchronized  void setWildcardChild(RouteTreeNode<T_REQ, T_RES> childNode) {

        if (wildcardChild != null) {
            throw new IllegalStateException("Cannot assign wildcard child, already has one");
        }

        wildcardChild = childNode;
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
}