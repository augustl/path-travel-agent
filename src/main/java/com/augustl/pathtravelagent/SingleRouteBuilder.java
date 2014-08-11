package com.augustl.pathtravelagent;


import com.augustl.pathtravelagent.segment.IParametricSegment;
import com.augustl.pathtravelagent.segment.StringSegment;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a RouteTreeNode, but for a single path, as opposed to a complete
 * route tree.
 *
 * This is typically used to add a single route to an existing tree, or to
 * create a whole array of RouteTreeNodes and merge them later.
 *
 * @see com.augustl.pathtravelagent.RouteTreeBuilder
 */
public class SingleRouteBuilder<T_REQ extends IRequest, T_RES> {
    private final List<ISegment<T_REQ, T_RES>> segments = new ArrayList<ISegment<T_REQ, T_RES>>();

    public SingleRouteBuilder<T_REQ, T_RES> path(String path) {
        segments.add(new PathSegment<T_REQ, T_RES>(path));
        return this;
    }

    public SingleRouteBuilder<T_REQ, T_RES> param(String name) {
        segments.add(new ParamSegment<T_REQ, T_RES>(new StringSegment(name)));
        return this;
    }

    public RouteTreeNode<T_REQ, T_RES> build(IRouteHandler<T_REQ, T_RES> handler) {
        RouteTreeNodeBuilder<T_REQ, T_RES> bottomNodeBuilder = new RouteTreeNodeBuilder<T_REQ, T_RES>();

        if (segments.size() == 0) {
            bottomNodeBuilder.setHandler(handler);
            return bottomNodeBuilder.createNode("::ROOT::");
        }

        bottomNodeBuilder.setHandler(handler);
        RouteTreeNode<T_REQ, T_RES> res = bottomNodeBuilder.createNode("::BOTTOM::");
        for (int i = segments.size() - 1; i >= 0; i--) {
            res = segments.get(i).getNode(res);
        }

        return res;
    }

    private interface ISegment<TT_REQ extends IRequest, TT_RES> {
        public RouteTreeNode<TT_REQ, TT_RES> getNode(RouteTreeNode<TT_REQ, TT_RES> childNode);
    }

    private class PathSegment<TT_REQ extends IRequest, TT_RES> implements ISegment<TT_REQ, TT_RES> {
        private final String path;
        PathSegment(String path) {
            this.path = path;
        }

        @Override
        public RouteTreeNode<TT_REQ, TT_RES> getNode(RouteTreeNode<TT_REQ, TT_RES> childNode) {
            RouteTreeNodeBuilder<TT_REQ, TT_RES> builder = new RouteTreeNodeBuilder<TT_REQ, TT_RES>();
            builder.addPathSegmentChild(this.path, childNode);
            return builder.createNode("::PATH:" + this.path + "::");
        }
    }

    private class ParamSegment<TT_REQ extends IRequest, TT_RES> implements ISegment<TT_REQ, TT_RES> {
        private final IParametricSegment parametricSegment;
        ParamSegment(IParametricSegment parametricSegment) {
            this.parametricSegment = parametricSegment;
        }

        @Override
        public RouteTreeNode<TT_REQ, TT_RES> getNode(RouteTreeNode<TT_REQ, TT_RES> childNode) {
            RouteTreeNodeBuilder<TT_REQ, TT_RES> builder = new RouteTreeNodeBuilder<TT_REQ, TT_RES>();
            builder.setParametricChild(this.parametricSegment, childNode);
            return builder.createNode("::PARAM:" + this.parametricSegment.getParamName() + "::");
        }
    }
}
