package com.augustl.pathtravelagent;

import com.augustl.pathtravelagent.segment.IParametricSegment;
import com.augustl.pathtravelagent.segment.StringSegment;

/**
 * The main entry-point for building complete route trees. It uses a builder pattern
 * that organizes the Java source code in a natural tree structure.
 *
 * <pre>
 * {@code
 *  class MyHandler implements IRouteHandler<MyReq, MyRes> {
 *      // ...
 *  }
 *
 *  RouteTreeNode<MyReq, MyRes> r = new RouteTreeBuilder<MyReq, MyRes>()
 *      .handler(new MyHandler("Hello, /"))
 *      .path("/foo", new RouteTreeBuilder<MyReq, MyRes>()
 *          .handler(new MyHandler("Hello, /foo!"))
 *          .path("/bar", new RouteTreeBuilder<MyReq, MyRes>()
 *              .handler(new MyHandler("Hello, /foo/bar"))))
 *      .build();
 * }
 * </pre>
 *
 * @param <T_REQ> A request object, implementing IRequest.
 * @param <T_RES> The return type if your IRouteHandler implementation. Can be any type you want, not used for anything
 *               by PathTravelAgent.
 * @see com.augustl.pathtravelagent.IRouteHandler
 * @see com.augustl.pathtravelagent.SingleRouteBuilder
 */
public class RouteTreeBuilder<T_REQ extends IRequest, T_RES> {
    private final String pathPrefix = "/";
    private final String paramNamePrefix = "/:";
    private RouteTreeNodeBuilder<T_REQ, T_RES> nodeBuilder = new RouteTreeNodeBuilder<T_REQ, T_RES>();

    public RouteTreeBuilder<T_REQ, T_RES> handler(IRouteHandler<T_REQ, T_RES> handler) {
        nodeBuilder.setHandler(handler);
        return this;
    }

    public RouteTreeBuilder<T_REQ, T_RES> path(final String path, RouteTreeBuilder<T_REQ, T_RES> childBuilder) {
        String normalizedPath = path.startsWith(pathPrefix) ? path.substring(pathPrefix.length()) : path;
        nodeBuilder.addPathSegmentChild(normalizedPath, childBuilder.build(normalizedPath));
        return this;
    }

    public RouteTreeBuilder<T_REQ, T_RES> param(final String paramName, final RouteTreeBuilder<T_REQ, T_RES> childBuilder) {
        String normalizedParam = paramName.startsWith(paramNamePrefix) ? paramName.substring(paramNamePrefix.length()) : paramName;
        nodeBuilder.setParametricChild(new StringSegment(normalizedParam), childBuilder.build("::PARAM:" + normalizedParam + "::"));
        return this;
    }

    public RouteTreeBuilder<T_REQ, T_RES> param(final IParametricSegment segment, final RouteTreeBuilder<T_REQ, T_RES> childBuilder) {
        nodeBuilder.setParametricChild(segment, childBuilder.build("::PARAM:" + segment.getParamName() + "::"));
        return this;
    }

    public RouteTreeBuilder<T_REQ, T_RES> wildcard(final RouteTreeBuilder<T_REQ, T_RES> childBuilder) {
        nodeBuilder.setWildcardChild(childBuilder.build("::WILDCARD::"));
        return this;
    }

    public RouteTreeNode<T_REQ, T_RES> build() {
        return nodeBuilder.createNode("::ROOT::");
    }

    public RouteTreeNode<T_REQ, T_RES> build(String label) {
        return nodeBuilder.createNode(label);
    }


}
