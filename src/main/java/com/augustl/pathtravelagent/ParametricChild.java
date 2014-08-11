package com.augustl.pathtravelagent;

import com.augustl.pathtravelagent.segment.IParametricSegment;

/**
 * <p>Used internally to represent an item in the routing tree that is parametric, i.e. it takes any (valid) value,
 * instead of a static path segment.</p>
 *
 * @param <T_REQ> A request object, implementing IRequest.
 * @param <T_RES> The return value for the handler. Can be any type you want, not used for anything by PathTravelAgent.
 */
public class ParametricChild<T_REQ extends IRequest, T_RES> {
    private final IParametricSegment parametricSegment;
    private final RouteTreeNode<T_REQ, T_RES> childNode;

    public ParametricChild(IParametricSegment parametricSegment, RouteTreeNode<T_REQ, T_RES> childNode) {
        if ((parametricSegment != null && childNode == null)
            || (parametricSegment == null && childNode != null)) {
            throw new IllegalArgumentException("Both parametricSegment and parametricChildNode must be either non-null or null");
        }


        this.parametricSegment = parametricSegment;
        this.childNode = childNode;
    }

    public IParametricSegment getParametricSegment() {
        return parametricSegment;
    }

    public RouteTreeNode<T_REQ, T_RES> getChildNode() {
        return childNode;
    }
}
