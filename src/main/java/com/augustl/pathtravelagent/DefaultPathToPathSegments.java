package com.augustl.pathtravelagent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>Default implementation of path to path segments, i.e. <tt>"/projects/123?test"</tt> to <tt>["project", "123"]</tt>.</p>
 */
public class DefaultPathToPathSegments {
    private static final List<String> EMPTY_PATH_SEGMENTS = new ArrayList<String>();

    public static List<String> parse(String path) {
        String[] pathSegmentsAry = extractPath(path).split("/");
        if (pathSegmentsAry.length == 0) {
            return EMPTY_PATH_SEGMENTS;
        } else {
            return Arrays.asList(pathSegmentsAry).subList(1, pathSegmentsAry.length);
        }
    }

    public static String extractPath(String path) {
        String[] aroundQueryParams = path.split("\\?", 2);
        if (aroundQueryParams.length == 0) {
            return path;
        } else {
            return aroundQueryParams[0];
        }
    }
}
