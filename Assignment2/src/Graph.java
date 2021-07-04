import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * This represents the data structure storing all the roads, nodes, and
 * segments, as well as some information on which nodes and segments should be
 * highlighted.
 *
 * @author tony
 */
public class Graph {
	// map node IDs to Nodes.
	Map<Integer, Node> nodes = new HashMap<>();
	// map road IDs to Roads.
	Map<Integer, Road> roads;
	// just some collection of Segments.
	Collection<Segment> segments;

	//fields for storing the highlighted parts of the graph
	private Node highlightedStartNode;
	private Node highlightedEndNode;
	private Collection<Road> highlightedRoads = new HashSet<>();
	private Collection<Segment> highlightedSegments = new HashSet<Segment>();


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

		// draw all the segments. drawing highlighted color if is a highlighted segment
		g2.setColor(Mapper.SEGMENT_COLOUR);
		for (Segment s : segments) {
			if(s.getHighlight()) {
				g2.setColor(Mapper.HIGHLIGHT_COLOUR);
			}else {
				g2.setColor(Mapper.SEGMENT_COLOUR);
			}
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

		// draw all the nodes.
		g2.setColor(Mapper.NODE_COLOUR);
		for (Node n : nodes.values())
			n.draw(g2, screen, origin, scale);

		// draw the highlighted start node, if it exists.
		if (highlightedStartNode != null) {
			g2.setColor(Color.GREEN);
			highlightedStartNode.draw(g2, screen, origin, scale);
		}

		// draw the highlighted end node, if it exists.
		if (highlightedEndNode != null) {
			g2.setColor(Color.RED);
			highlightedEndNode.draw(g2, screen, origin, scale);
		}
	}


	public void setHighlightedStartNode(Node node) {
		this.highlightedStartNode = node;
	}

	public void setHighlightedEndNode(Node node) {
		this.highlightedEndNode = node;
	}

	public void setHighlight(Collection<Road> roads) {
		this.highlightedRoads = roads;
	}

	public Collection<Segment> getHighlightedSegments() {
		return highlightedSegments;
	}

	/**
	 *Adds the highlighted segmented without passing the entire collection
	 * */
	public void addHighlightedSegment(Segment s) {
		highlightedSegments.add(s);
	}
}

// code for COMP261 assignments