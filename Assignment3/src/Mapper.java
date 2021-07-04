import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

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

    //these two constants define the size of the node squares at different zoom
     //levels; the equation used is node size = NODE_INTERCEPT + NODE_GRADIENT *
     //log(scale)
    public static final int NODE_INTERCEPT = 1;
    public static final double NODE_GRADIENT = 0.8;

    // defines how much you move per button press, and is dependent on scale.
    public static final double MOVE_AMOUNT = 100;
    // defines how much you zoom in/out per button press, and the maximum and
     //minimum and maximum zoom levels.
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

        // if it's close enough, then start critical intersections search
        if (clicked.distance(closest.location) < MAX_CLICKED_DISTANCE) {
            findArticulationPoints(closest);
        }
    }

    @Override
    protected void onSearch() {
        // Does nothing
    }

    @Override
    protected void onMove(Move m) {
        if (graph == null) { return; }
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
        getTextOutputArea().setText("");
    }

    @Override
    protected void searchAllArticulationPoints() {
        if (graph != null) { findDisconnected(); }
    }

    @Override
    protected void searchMST() {
        if (graph != null) {
            getTextOutputArea().setText("");
            findMSTTree();
        }

    }

    /**
     * This method does the nasty logic of making sure we always zoom into/out
     * of the centre of the screen. It assumes that scale has just been updated
     * to be either scale * ZOOM_FACTOR (zooming in) or scale / ZOOM_FACTOR
     * (zooming out). The passed boolean should correspond to this, ie. be true
     * if the scale was just increased.
     *
     * @param zoomIn	wether the last action was a zoom in
     */
    private void scaleOrigin(boolean zoomIn) {
        Dimension area = getDrawingAreaDimension();
        double zoom = zoomIn ? 1 / ZOOM_FACTOR : ZOOM_FACTOR;

        int dx = (int) ((area.width - (area.width * zoom)) / 2);
        int dy = (int) ((area.height - (area.height * zoom)) / 2);

        origin = Location.newFromPoint(new Point(dx, dy), origin, scale);
    }

    private Node getRandomRoot() {
        List<Node> randomNodes = new ArrayList<>(graph.nodes.values());
        Random randomRoot = new Random();
        Node rootNode = randomNodes.get(randomRoot.nextInt(randomNodes.size()));
        return rootNode;
    }

    /**
     * Clears the previous highlights of the articulation points, and resets the node depths for the search.
     * */
    private void searchReset() {
        graph.unHighlightAll();
        for (Node node : graph.nodes.values()){ node.setDepth(-1); }
    }

    /**
     * findArticulationPoints method begins the search for articulation points, begin searching its neighbours and calls the iterative search method
     * to travel down the graph. Finds intersection points and different paths to nodes if there is more than one sub tree.
     * Will not be able to search the disconnected sections of the graph, only those connected to the rootNode.
     * */
    private void findArticulationPoints(Node rootNode) {
        Set<Node> articulationPoints = new HashSet<>();
        searchReset();
        rootSearch(rootNode, articulationPoints);
        graph.setHighlightedNodes(articulationPoints);
        getTextOutputArea().setText((articulationPoints.size()) + " Articulation Points Found");
    }

    /**
     * findDisconnected method begins the search for articulation points, begin searching its neighbours and calls the iterative search method
     * to travel down the graph. Finds intersection points and different paths to nodes if there is more than one sub tree.
     * Finds all articulation points, including in those in disconnected sections.
     * */
    private void findDisconnected() {
        Set<Node> articulationPoints = new HashSet<>();
        searchReset();
        Node rootNode = getRandomRoot(); //Start node is chosen randomly

        boolean complete = false; //Will only stop the search when all nodes in the graph have been visited
        while (!complete) { //Begin the search down the tree
            rootSearch(rootNode, articulationPoints);
            complete = true; //Check if there are unvisited nodes in the graph, if so update rootNode
            for (Node node : graph.nodes.values()) {
                if (node.getDepth() == -1) {
                    complete = false;
                    rootNode = node;
                }
            }
        }
        graph.setHighlightedNodes(articulationPoints);
        getTextOutputArea().setText((articulationPoints.size()) + " Total Articulation Points Found");
    }

    /**
     * Method begins the articulation search going down the neighbours and creating a tree to find articulation points,
     * and also checks if the rootNode is an articulation point. Uses an iterative search to avoid stack overflow.
     * */
    private void rootSearch(Node rootNode, Set<Node> articulationPoints) {
        int subTrees = 0;
        rootNode.setDepth(0);
        for (Node neighbour : rootNode.getNeighbours()){
            if (neighbour.getDepth() == -1) {
                articulationSearch(neighbour, rootNode, 1, articulationPoints);
                subTrees++;
            }
            if(subTrees > 1){ articulationPoints.add(rootNode);  }
        }
    }

    /**
     * A nested class which is used to create the search objects as a structure to hold the node information of the current search.
     * */
    private static class articulationObjects{
        Node currNode; //Current node in the search
        Node parent; //Parent node of the current node
        int depth; //Depth in the tree of the current node

        articulationObjects(Node currNode, Node parent, int depth){
            this.currNode = currNode;
            this.parent = parent;
            this.depth = depth;
        }
    }

    /**
     * The iterative search method for articulation points, uses a stack to avoid stack overflow.
     * Adds the children of the node onto the stack and working down. Peaks each node at the top and pops only until all children have been added.
     * Adds the child onto the stack if it hasn't been visited. Else if it's able to reachback up the tree using the child's depth then the search is running.
     * After returning to the node it has been checked and ready for the next child, until there are no more left
     * and can finally decide whether it's an articulation point.
     * */
    private void articulationSearch(Node firstNode, Node root, int firstDepth, Set<Node> articulationPoints) {
        Stack<articulationObjects> searchStack = new Stack<>();
        searchStack.push(new articulationObjects(firstNode, root, firstDepth));

        while (!searchStack.isEmpty()) { //Searches all nodes to find the articulation points
            articulationObjects currObject = searchStack.peek();
            int currDepth = currObject.depth;
            Node currSearchNode = currObject.currNode;
            Node currParent = currObject.parent;

            if (currSearchNode.getDepth() == -1){ //Setting the node objects
                currSearchNode.setDepth(currDepth);
                currSearchNode.setReachBack(currDepth);
                currSearchNode.setChildren(currParent);

            } else if (!currSearchNode.getChildren().isEmpty()){ //Adding or reachBack to child
                Node child = currSearchNode.getNextChild();
                if (child.getDepth() != -1) { currSearchNode.setReachBack(Math.min(currSearchNode.getReachBack(), child.getDepth())); }
                else { searchStack.push(new articulationObjects(child, currSearchNode, currDepth + 1)); }

            }else { //Checks if it's an articulation point or can reachBack to child
                if (currSearchNode != firstNode){ currParent.setReachBack(Math.min(currSearchNode.getReachBack(), currParent.getReachBack()));
                    if (currSearchNode.getReachBack() >= currParent.getDepth()) { articulationPoints.add(currParent); } }
                searchStack.remove(currObject);
            }
        }
    }

    /**
     * Initializes the forest with all nodes into inverted trees,
     * and adds all the shortest segments into the fringe.
     * */
    private void findMSTTree() {
        Set<Node> forest = new HashSet<>();
        PriorityQueue<MSTNode> fringe = new PriorityQueue<>();
        graph.unHighlightAll();

        //Setting all nodes as inverted trees and shortest segments of themselves and filling into forest and fringe
        for (Node node : graph.nodes.values()){
            forest.add(DisjointSet.makeSet(node));
            assert(!node.getNeighbours().isEmpty());
            assert(node.getNeighbours().size() == node.getShortestSegments().size());
            fringe.addAll(node.getShortestSegments());
        }

        Set<Segment> MSTTree = searchMST(forest, fringe);
        graph.setHighlightedSegments(MSTTree);
        for (Segment seg : MSTTree) {
            graph.addHighlightedNode(seg.start);
            graph.addHighlightedNode(seg.end);
        }
    }

    /**
     * Search method for the Minimum Spanning Tree, using Kruskal's Algorithm with Disjoint sets. Method runs until there are no more edges or nodes left to search.
     * Gets the lowest weight MSTNode from the fringe, then will check the if two nodes are in different inverted tree sets.
     * */
    private Set<Segment> searchMST(Set<Node> forest, PriorityQueue<MSTNode> fringe){
        Set<Segment> tree = new HashSet<>();

        while (forest.size() > 1 && !fringe.isEmpty()){
            MSTNode currMST = fringe.poll();
            Node N1 = currMST.getNode1();
            Node N2 = currMST.getNode2();

            if (DisjointSet.union(N1, N2, forest)){ //Merges the two trees, returns true if merge occurred, else nothing if same tree
                tree.add(currMST.getConnect()); //Segment of the current MST node used to merge
            }
        }
        return tree;
    }

    public static void main(String[] args){ new Mapper(); }
}

// code for COMP261 assignments