package com.augustl.pathtravelagent;

import java.util.List;

/**
 * <p>Represents a request.</p>
 *
 * <p>The unit of work is a {@code List<String>} of path segments. So a request does not contain /projects/123 directly, it
 * is expected to return a list of <tt>["projects", "123"]</tt>.</p>
 *
 * @see com.augustl.pathtravelagent.DefaultPathToPathSegments#parse(String)
 */
public interface IRequest {
    public List<String> getPathSegments();
}
