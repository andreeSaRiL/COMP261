import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This represents the data structure storing all the roads, nodes, and
 * segments, as well as some information on which nodes and segments should be
 * highlighted.
 *
 * @author tony
 */
public class Graph {
    Map<Integer, Node> nodes = new HashMap<>();
    Map<Integer, Road> roads;
    Collection<Segment> segments;

    Node highlightedNode;
    Collection<Node> highlightedNodes = new HashSet<Node>();
    Collection<Segment> highlightedSegments = new HashSet<Segment>();
    Collection<Road> highlightedRoads = new HashSet<>();

    public Graph(File nodes, File roads, File segments, File polygons) {
        this.nodes = Parser.parseNodes(nodes, this);
        this.roads = Parser.parseRoads(roads, this);
        this.segments = Parser.parseSegments(segments, this);
    }


    public void draw(Graphics g, Dimension screen, Location origin, double scale) {
        // a compatibility wart on swing is that it has to give out Graphics
        // objects, but Graphics2D objects are nicer to work with. Luckily
        // they're a subclass, and swing always gives them out anyway, so we can
        // just do this.
        Graphics2D g2 = (Graphics2D) g;

        // draw all the segments.
        g2.setColor(Mapper.SEGMENT_COLOUR);
        for (Segment s : segments) {
            if (highlightedSegments.contains(s)) { g2.setColor(Color.GREEN); }
            else { g2.setColor(Mapper.SEGMENT_COLOUR); }
            s.draw(g2, origin, scale);
        }

        // draw the segments of all highlighted roads.
        g2.setColor(Mapper.HIGHLIGHT_COLOUR);
        g2.setStroke(new BasicStroke(3));
        for (Road road : highlightedRoads) {
            for (Segment seg : road.components) {
                seg.draw(g2, origin, scale);
            }
        }

        // draw all the nodes
        for (Node n : nodes.values()) {
            if (highlightedNodes.contains(n)) { g2.setColor(Color.RED); }
            else { g2.setColor(Mapper.NODE_COLOUR);  }
            n.draw(g2, screen, origin, scale);
        }

        // draw the highlighted node, if it exists.
        if (highlightedNode != null) {
            g2.setColor(Mapper.HIGHLIGHT_COLOUR);
            highlightedNode.draw(g2, screen, origin, scale);
        }
    }


    public void setHighlightedNodes(Set<Node> highlightedNodes) {
        this.highlightedNodes.addAll(highlightedNodes);
    }

    public void addHighlightedNode(Node highlightedNode) {
        this.highlightedNodes.add(highlightedNode);
    }

    public void setHighlight(Node node) {
        this.highlightedNode = node;
    }

    public void setHighlight(Collection<Road> roads) {
        this.highlightedRoads = roads;
    }

    public void setHighlightedSegments(Set<Segment> highlightedSegments) {
        this.highlightedSegments = highlightedSegments;
    }

    public void unHighlightAll() {
        highlightedNodes.clear();
        highlightedSegments.clear();
    }
}

// code for COMP261 assignments