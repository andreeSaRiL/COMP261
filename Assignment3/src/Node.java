import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Node represents an intersection in the road graph. It stores its ID and its
 * location, as well as all the segments that it connects to. It knows how to
 * draw itself, and has an informative toString method.
 *
 * @author tony
 */
public class Node {

    public final int nodeID;
    public final Location location;
    public final Collection<Segment> segments;

    private Set<Node> neighbours;
    private Set<MSTNode> shortestSegments;
    private List<Node> children;
    private int depth;
    private int reachBack;

    private Node disjointParent;
    private int disjointDepth;

    public Node(int nodeID, double lat, double lon) {
        this.nodeID = nodeID;
        this.location = Location.newFromLatLon(lat, lon);
        this.segments = new HashSet<>();
        this.neighbours = new HashSet<>();
        this.children = new ArrayList<>();
        this.shortestSegments = new HashSet<>();
    }

    public void addSegment(Segment seg) {
        segments.add(seg);
    }

    public void draw(Graphics g, Dimension area, Location origin, double scale) {
        Point p = location.asPoint(origin, scale);

        // for efficiency, don't render nodes that are off-screen.
        if (p.x < 0 || p.x > area.width || p.y < 0 || p.y > area.height)
            return;

        int size = (int) (Mapper.NODE_GRADIENT * Math.log(scale) + Mapper.NODE_INTERCEPT);
        g.fillRect(p.x - size / 2, p.y - size / 2, size, size);
    }

    /**
     * Fills the shortest segments, taking the shortest length segment of a node pair.
     * Filters over the the segments getting the duplicates or only the segment which have the same start or end nodes.
     */
    public void fillShortest() {
        for (Node node : neighbours) {
            List<Segment> duplicates = segments.stream().filter(seg -> (seg.start == this && seg.end == node) || (seg.start == node && seg.end == this)).collect(Collectors.toList());
            duplicates.sort((Segment s1, Segment s2) -> {
                if (s1.length < s2.length) {
                    return -1;
                }
                if (s1.length > s2.length) {
                    return 1;
                }
                return 0;
            });

            Segment shortest = duplicates.get(0);
            shortestSegments.add(new MSTNode(this, node, shortest.length, shortest));
        }
    }

    public String toString() {
        Set<String> edges = new HashSet<String>();
        for (Segment s : segments) {
            if (!edges.contains(s.road.name))
                edges.add(s.road.name);
        }

        String str = "ID: " + nodeID + "  Loc: " + location + "\nRoads: ";
        for (String e : edges) {
            str += e + ", ";
        }
        return str.substring(0, str.length() - 2);
    }


    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }

    public void setReachBack(int reachBack) {
        this.reachBack = reachBack;
    }

    public int getReachBack() {
        return reachBack;
    }

    public void addNeighbour(Node neighbour) {
        neighbours.add(neighbour);
    }

    public Set<Node> getNeighbours() {
        return Collections.unmodifiableSet(neighbours);
    }

    public Node getNextChild() {
        return children.remove(0);
    }

    public List<Node> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public void setChildren(Node parent) {
        children.clear();
        children.addAll(neighbours);
        children.remove(parent);
    }

    public Set<MSTNode> getShortestSegments() {
        return Collections.unmodifiableSet(shortestSegments);
    }

    public Node getDisjointParent() {
        return disjointParent;
    }

    public void setDisjointParent(Node disjointParent) {
        this.disjointParent = disjointParent;
    }

    public int getDisjointDepth() {
        return disjointDepth;
    }

    public void setDisjointDepth(int disjointDepth) {
        this.disjointDepth = disjointDepth;
    }
}

// code for COMP261 assignments