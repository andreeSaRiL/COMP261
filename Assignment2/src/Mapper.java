import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;

/**
 * This is the main class for the mapping program. It extends the GUI abstract
 * class and implements all the methods necessary, as well as having a main
 * function.
 *
 * @author tony
 */
public class Mapper extends GUI {
	public static final Color NODE_COLOUR = new Color(77, 113, 255);
	public static final Color SEGMENT_COLOUR = new Color(130, 130, 130);
	public static final Color HIGHLIGHT_COLOUR = new Color(255, 219, 77);

	// these two constants define the size of the node squares at different zoom
	// levels; the equation used is node size = NODE_INTERCEPT + NODE_GRADIENT *
	// log(scale)
	public static final int NODE_INTERCEPT = 1;
	public static final double NODE_GRADIENT = 0.8;

	// defines how much you move per button press, and is dependent on scale.
	public static final double MOVE_AMOUNT = 100;
	// defines how much you zoom in/out per button press, and the maximum and
	// minimum zoom levels.
	public static final double ZOOM_FACTOR = 1.3;
	public static final double MIN_ZOOM = 1, MAX_ZOOM = 200;

	// how far away from a node you can click before it isn't counted.
	public static final double MAX_CLICKED_DISTANCE = 0.15;

	// these two define the 'view' of the program, ie. where you're looking and
	// how zoomed in you are.
	private Location origin;
	private double scale;

	// our data structures.
	private Graph graph;

	//nodes for the shortest path search
	private Node startNode, endNode;

	@Override
	protected void redraw(Graphics g) {
		if (graph != null)
			graph.draw(g, getDrawingAreaDimension(), origin, scale);
	}

	@Override
	protected void onClick(MouseEvent e) {
		Location clicked = Location.newFromPoint(e.getPoint(), origin, scale);
		// find the closest node.
		double bestDist = Double.MAX_VALUE;
		Node closest = null;

		for (Node node : graph.nodes.values()) {
			double distance = clicked.distance(node.location);
			if (distance < bestDist) {
				bestDist = distance;
				closest = node;
			}
		}
		// if it's close enough, highlight it and show some information.
		if (clicked.distance(closest.location) < MAX_CLICKED_DISTANCE) {
			setNodes(closest);
		}
	}

	@Override
	protected void onSearch() {
		// Does nothing
	}

	@Override
	protected void onMove(Move m) {
		if (m == GUI.Move.NORTH) {
			origin = origin.moveBy(0, MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.SOUTH) {
			origin = origin.moveBy(0, -MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.EAST) {
			origin = origin.moveBy(MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.WEST) {
			origin = origin.moveBy(-MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.ZOOM_IN) {
			if (scale < MAX_ZOOM) {
				// yes, this does allow you to go slightly over/under the
				// max/min scale, but it means that we always zoom exactly to
				// the centre.
				scaleOrigin(true);
				scale *= ZOOM_FACTOR;
			}
		} else if (m == GUI.Move.ZOOM_OUT) {
			if (scale > MIN_ZOOM) {
				scaleOrigin(false);
				scale /= ZOOM_FACTOR;
			}
		}
	}

	@Override
	protected void onLoad(File nodes, File roads, File segments, File polygons) {
		graph = new Graph(nodes, roads, segments, polygons);
		origin = new Location(-250, 250); // close enough
		scale = 1;
	}


	/**
	 * Sets the start and end node, calls the search for the shortest path.
	 * All segments are not highlighted for the next search, however the first mouse click's start pos
	 * is highlighted and the data info is printed. The end node (second click) is highlighted
	 * and the search is called.
	 * If not, the end node will be swapped to the start node, the nearest position will become the end
	 * node and search is called again.
	 */
	private void setNodes(Node near) {
		for(Segment seg : graph.getHighlightedSegments()) {
			seg.setHighlight(false);
		}
		graph.getHighlightedSegments().clear();

		//The first two sets the start node, the third is the swap from end to start nodes
		if(startNode == null) {
			graph.setHighlightedStartNode(near);
			startNode = near;
			getTextOutputArea().setText(near.toString());
		}else if(endNode == null) {
			graph.setHighlightedEndNode(near);
			endNode = near;
		}else {
			startNode = endNode;
			endNode = near;
			graph.setHighlightedStartNode(startNode);
			graph.setHighlightedEndNode(endNode);
		}

		if(startNode != null && endNode != null) {
			findShortestPath(startNode, endNode);
		}
	}


	/**
	 * This method does the nasty logic of making sure we always zoom into/out
	 * of the centre of the screen. It assumes that scale has just been updated
	 * to be either scale * ZOOM_FACTOR (zooming in) or scale / ZOOM_FACTOR
	 * (zooming out). The passed boolean should correspond to this, ie. be true
	 * if the scale was just increased.
	 *
	 * @param zoomIn --- boolean to tell if scale was just increased
	 */
	private void scaleOrigin(boolean zoomIn) {
		Dimension area = getDrawingAreaDimension();
		double zoom = zoomIn ? 1 / ZOOM_FACTOR : ZOOM_FACTOR;

		int dx = (int) ((area.width - (area.width * zoom)) / 2);
		int dy = (int) ((area.height - (area.height * zoom)) / 2);

		origin = Location.newFromPoint(new Point(dx, dy), origin, scale);

	}


	/**
	 * Performs A* search for the shortest path between the start and end nodes. This works by using
	 * a priority queue with fringe objects that contain information needed to search. The priority queue is
	 * sorted based on the overall estimated cost so that the shortest path is shown first. The search begins with
	 * the start node in the fringe then going through until it's empty or the end node is expanded. Meaning that the shortest path has been found.
	 * Every time a node is expanded it'll be visited and the previous node is set so are able to trace the path.
	 * All unvisited neighbours from connected segments are added with their current dist from the start and
	 * the overall est cost to continue the search.
	 * The path is retrieved and printed, and all previous nodes and visited are cleared before search again.
	 */
	private void findShortestPath(Node startNode, Node endNode) {
		if(startNode == endNode) {
			getTextOutputArea().setText(startNode.toString());
			return;
		}
		Queue<FringeObject> fringe = new PriorityQueue<FringeObject>(FringeObject::compareTo);
		for(Node node : graph.nodes.values()) {
			node.setVisited(false);
			node.setPrevNode(null);
		}
		fringe.offer(new FringeObject(startNode, null, 0, startNode.location.distance(endNode.location)));
		Node lastVisit = null;
		while(!fringe.isEmpty()) {
			FringeObject currFringe = fringe.poll();
			Node currNode = currFringe.getCurrNode();
			if(!currNode.isVisited()) {
				currNode.setVisited(true);
				lastVisit = currNode;
				currNode.setPrevNode(currFringe.getPrevNode());
				if(currNode == endNode) {
					break;
				}
				for(Segment seg : currNode.segments) {
					Node neighbour = seg.end; //neighbour node
					if(neighbour == currNode) {
						neighbour = seg.start;
					}
					if(!neighbour.isVisited()) {
						double costFromStart = currFringe.getCostFromStart() + seg.length;
						double estCost = costFromStart + neighbour.location.distance(endNode.location); //overall estimated cost
						fringe.offer(new FringeObject(neighbour, currNode, costFromStart, estCost));
					}
				}
			}
		}
		printShortestPath(pathFromPrev(lastVisit, endNode));
	}

	/**
	 * Prints each segment in the path, incl the length in order of the first segment from start to end.
	 * The length of each segment is added to the path distance to calculate the total distance.
	 * @param pathSeg stack contains all segments for the path to be printed
	 */
	private void printShortestPath(Stack<Segment> pathSeg) {
		String pathName = "";
		double totalDist = 0;

		if(pathSeg == null || pathSeg.isEmpty()) { //If path is empty
			pathName = "Path Not Found. \n";
		}else {
			for(Segment seg : pathSeg) {
				pathName += seg.road.name + ": " + seg.length + "km\n";
				totalDist += seg.length;
				seg.setHighlight(true);
				graph.addHighlightedSegment(seg);
			}
		}
		getTextOutputArea().setText(pathName + "Total Distance: " + totalDist + "km");
	}


	/**
	 * Goes through the previous nodes from the final node to the start node, when there are no more previous nodes for each node.
	 * For each node, the segment to the previous node is taken and stored on the stack so it'll be printed to the screen which is then return.
	 * @param endNode target node for the search
	 * @param finalNode absolute end node of the search
	 */
	private Stack<Segment> pathFromPrev(Node endNode, Node finalNode){
		Stack<Segment> pathSeg = new Stack<Segment>(); //path segments
		Node currNode = finalNode;

		if(currNode != endNode) { //returns empty stack if the finalNode is not the end node
			return pathSeg;
		}
		while(currNode.getPrevNode() != null) {
			for(Segment seg : currNode.segments) {
				if(seg.start == currNode.getPrevNode() || seg.end == currNode.getPrevNode()) { //adds the segments which connect the two nodes
					pathSeg.push(seg);
				}
			}
			currNode = currNode.getPrevNode();
		}
		return pathSeg;
	}


	public static void main(String[] args) {
		new Mapper();
	}
}

// code for COMP261 assignments