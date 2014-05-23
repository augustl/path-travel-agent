package com.augustl.pathtravelagent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RouteSet<T_ROUTE extends Route<T_REQ, T_RES>, T_REQ extends IRequest, T_RES> {
    private final List<ISegment> segmentTable = new ArrayList<ISegment>();
    private final HashMap<Integer, T_ROUTE> routeTable = new HashMap<Integer, T_ROUTE>();

    public void addRoute(T_ROUTE route) {
        List<ISegment> routeSegments = route.getSegments();
        for (int i = 0; i < routeSegments.size(); i++) {
            ISegment segment = routeSegments.get(i);
            if (i < segmentTable.size()) {
                ISegment storedSegment = segmentTable.get(i);
                if (!storedSegment.getSegmentName().equals(segment.getSegmentName())) {
                    throw new IllegalArgumentException("Route segment at index " + i + " did not match the segment already in route set for that index.");
                }
            } else {
                segmentTable.add(i, segment);
            }
        }

        Integer routeIdx = routeSegments.size() - 1;
        if (routeTable.containsKey(routeIdx)) {
            throw new IllegalArgumentException("Route table already contains entry at position " + routeIdx);
        }
        routeTable.put(routeIdx, route);
    }

    public T_RES match(T_REQ req, List<String> pathSegments) {
        T_ROUTE route = this.routeTable.get(pathSegments.size() - 1);
        if (route == null) {
            return null;
        }

        RouteMatchResult routeMatchResult = new RouteMatchResult();

        for (int i = 0; i < pathSegments.size(); i++) {
            String pathSegment = pathSegments.get(i);
            ISegment storedSegment = segmentTable.get(i);
            RouteMatchResult.IResult matchResult = storedSegment.matchPathSegment(pathSegment);
            boolean matched = routeMatchResult.addPossibleMatch(storedSegment.getSegmentName(), matchResult);
            if (!matched) {
                return null;
            }
        }

        return route.match(req, routeMatchResult);
    }
}
