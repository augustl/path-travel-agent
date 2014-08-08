package com.augustl.pathtravelagent;

import com.augustl.pathtravelagent.segment.IParametricSegment;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RouteTreeNode<T_REQ extends IRequest, T_RES> {
    private final String label;
    private final IRouteHandler<T_REQ, T_RES> handler;
    private final Map<String, RouteTreeNode<T_REQ, T_RES>> pathSegmentChildNodes;
    private final ParametricChild<T_REQ, T_RES> parametricChild;
    private final RouteTreeNode<T_REQ, T_RES> wildcardChild;

    public RouteTreeNode(
        String label,
        IRouteHandler<T_REQ, T_RES> handler,
        HashMap<String, RouteTreeNode<T_REQ, T_RES>> pathSegmentChildNodes,
        ParametricChild<T_REQ, T_RES> parametricChild,
        RouteTreeNode<T_REQ, T_RES> wildcardChild) {
        this.label = label;
        this.handler = handler;
        this.pathSegmentChildNodes = Collections.unmodifiableMap(pathSegmentChildNodes);
        this.parametricChild = parametricChild;
        this.wildcardChild = wildcardChild;
    }

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
        return this.parametricChild.getParametricSegment();
    }

    public RouteTreeNode<T_REQ, T_RES> getParametricChildNode() {
        return this.parametricChild.getChildNode();
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
}