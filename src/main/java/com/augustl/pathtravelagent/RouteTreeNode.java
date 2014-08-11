package com.augustl.pathtravelagent;

import com.augustl.pathtravelagent.segment.IParametricSegment;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The actual routes. An immutable value. Build with RouteTreeBuilder or SingleRouteBuilder.
 *
 * All the methods on this class are used when performing matching operations. The actual instance, being immutable,
 * can be considered a raw value. Use the various builders to create instances of these.
 *
 * @param <T_REQ> A request object, implementing IRequest.
 * @param <T_RES> The return value for the handler. Can be any type you want, not used for anything by PathTravelAgent.
 * @see com.augustl.pathtravelagent.RouteTreeBuilder
 * @see com.augustl.pathtravelagent.SingleRouteBuilder
 */
public class RouteTreeNode<T_REQ extends IRequest, T_RES> {
    private final String label;
    private final IRouteHandler<T_REQ, T_RES> handler;
    private final Map<String, RouteTreeNode<T_REQ, T_RES>> pathSegmentChildNodes;
    private final ParametricChild<T_REQ, T_RES> parametricChild;
    private final RouteTreeNode<T_REQ, T_RES> wildcardChild;

    public RouteTreeNode() {
        this.label = "::ROOT::";
        this.handler = null;
        this.pathSegmentChildNodes = Collections.unmodifiableMap(new HashMap<String, RouteTreeNode<T_REQ, T_RES>>());
        this.parametricChild = null;
        this.wildcardChild = null;
    }

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

    /**
     * If the node has a named child, the matcher should most likely prioritize this named child over any parametric
     * or wildcard child. For example, even if there's a parametric handler for /projects/myproj, if there happens to be
     * a named handler for "myproj", it should take precedence over the parametric handler.
     */
    public boolean containsPathSegmentChildNodes(String pathSegment) {
        return this.pathSegmentChildNodes.containsKey(pathSegment);
    }

    public RouteTreeNode<T_REQ, T_RES> getPathSegmentChildNode(String pathSegment) {
        return this.pathSegmentChildNodes.get(pathSegment);
    }

    /**
     * If a node has a parametric child, the matcher can use this child to handle arbitrary values. For example, given
     * the path /projects/myproj, if there is no named handler for "myproj", the parametric handler can be invoked for
     * "myproj", giving us a named parameter containing that value.
     */
    public boolean hasParametricChild() {
        return this.parametricChild != null;
    }

    public IParametricSegment getParametricChildSegment() {
        return this.parametricChild.getParametricSegment();
    }

    public RouteTreeNode<T_REQ, T_RES> getParametricChildNode() {
        return this.parametricChild.getChildNode();
    }

    /**
     * When a node has a wildcard child node, it means that the rest of the path at this point will be passed to that
     * child, as a wildcard. For example, given the path /foo/bar/baz/maz and a wildcard handler at /foo/bar, any
     * segment beyond /foo/bar should be passed to the wildcard child, instead of looking for further child handlers.
     */
    public boolean hasWildcardChild() {
        return this.wildcardChild != null;
    }

    public RouteTreeNode<T_REQ, T_RES> getWildcardChildNode() {
        return this.wildcardChild;
    }

    /**
     * This method returns a handler or null. If a node has no handler, it means the tree has no handler at this
     * particular point. This is useful for deep trees where you only want a handler at the bottom. An example would
     * be handling /foo/bar/baz, but not /foo/bar. The node representing the "bar" level would have a null handler in
     * that case.
     *
     * @return The handler associated with this node.
     */
    public IRouteHandler<T_REQ, T_RES> getHandler() {
        return handler;
    }

    /**
     * <p>Deep left-to-right merges a node with another. Since this class is immutable, a new instance is returned, and
     * none of the two merged instances are changed.</p>
     *
     * <p>How two handlers are merged is up to the handler in question. When both the source and target tree has a
     * handler at a given point in the tree, the merge method is called on the source handler, getting the target
     * handler passed in. The details of how this merge takes place is up to the user, no default implementation is
     * provided.</p>
     *
     * <p>The other elements such as parametric routes and wildcard routes are automatically merged and can not be
     * configured by the user.</p>
     *
     * @param other The (immutable) node to merge with
     * @return The new (immutable) node
     * @see com.augustl.pathtravelagent.IRouteHandler#merge
     */
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
            if (this.handler == null) {
                return other.handler;
            } else {
                return this.handler.merge(other.handler);
            }
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