package com.augustl.pathtravelagent;

import com.augustl.pathtravelagent.segment.IParametricSegment;

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
