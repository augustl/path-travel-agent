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

    public RouteTreeNode<T_REQ, T_RES> merge(RouteTreeNode<T_REQ, T_RES> other) {
        return merge(other, new ArrayList<String>());
    }

    private RouteTreeNode<T_REQ, T_RES> merge(RouteTreeNode<T_REQ, T_RES> other, List<String> context) {
        context = new ArrayList<String>(context);
        context.add(other.label);

        return new RouteTreeNode<T_REQ, T_RES>(
            other.label,
            this.getMergedHandler(other, context),
            this.getMergedPathSegmentChildNodes(other, context),
            this.getMergedParametricChild(other, context),
            this.getMergedWildcardChild(other, context));
    }

    private IRouteHandler<T_REQ, T_RES> getMergedHandler(RouteTreeNode<T_REQ, T_RES> other, List<String> context) {
        if (other.handler == null) {
            return this.handler;
        } else {
            return other.handler;
        }
    }

    private HashMap<String, RouteTreeNode<T_REQ, T_RES>> getMergedPathSegmentChildNodes(RouteTreeNode<T_REQ, T_RES> other, List<String> context) {
        HashMap<String, RouteTreeNode<T_REQ, T_RES>> res = new HashMap<String, RouteTreeNode<T_REQ, T_RES>>(this.pathSegmentChildNodes);

        for (String pathSegment : other.pathSegmentChildNodes.keySet()) {
            if (this.pathSegmentChildNodes.containsKey(pathSegment)) {
                res.put(pathSegment, this.pathSegmentChildNodes.get(pathSegment).merge(other.pathSegmentChildNodes.get(pathSegment), context));
            } else {
                res.put(pathSegment, other.pathSegmentChildNodes.get(pathSegment));
            }
        }

        return res;
    }

    private ParametricChild<T_REQ, T_RES> getMergedParametricChild(RouteTreeNode<T_REQ, T_RES> other, List<String> context) {
        if (this.parametricChild == null) {
            return other.parametricChild;
        } else {
            if (other.parametricChild == null) {
                return this.parametricChild;
            } else {
                return new ParametricChild<T_REQ, T_RES>(other.parametricChild.getParametricSegment(), this.parametricChild.getChildNode().merge(other.parametricChild.getChildNode(), context));
            }
        }
    }

    private RouteTreeNode<T_REQ, T_RES> getMergedWildcardChild(RouteTreeNode<T_REQ, T_RES> other, List<String> context) {
        if (this.wildcardChild == null) {
            return other.wildcardChild;
        } else {
            return this.wildcardChild.merge(other.wildcardChild, context);
        }
    }
}