import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.*;

public class Stop{

    private String stopId;
    private String stopName;
    private Location stopLocation;
    private Point stopPoint;
    private Color stopColor;

    private int stopSize;
    private boolean selected;

    private Set<Connection> outgoingEdges = new HashSet<Connection>();
    private Set<Connection> ingoingEdges = new HashSet<Connection>();



    public Stop(String stopId, String stopName, float stopLat, float stopLon) {
        this.stopId = stopId;
        this.stopName = stopName;
        setStopLocation(stopLat, stopLon);
        stopSize = 5;
        selected = false;
    }


    /**
     * Draws the stop as a rectangle in either black (normally) or the selected colour.
     * Converts to a point initially to draw on the screen.
     * */
    public void drawStop(Graphics g, Location origin, double size) { //size is the amount to zoom in/out
        if(selected) {
            g.setColor(stopColor);
        }else {
            g.setColor(Color.BLUE);
        }
        stopPoint = stopLocation.asPoint(origin, size);
        g.fillRect(stopPoint.x - (stopSize/2), stopPoint.y - (stopSize/2), stopSize, stopSize);
    }


    public void addOutgoingEdge(Connection conn){
        outgoingEdges.add(conn);
    }


    public void addIngoingEdge(Connection conn){
        ingoingEdges.add(conn);
    }


    public void unSelect() {
        selected = false;
    }


    public void select() {
        selected = true;
        stopColor = Color.RED;
    }


    public String getName() {
        return stopName;
    }


    /**
     * Point location of the stop
     * */
    public Point getPoint() {
        return stopPoint;
    }


    public int getStopSize() {
        return stopSize;
    }


    public void setStopSize(int stopS) {
        stopSize = stopS;
    }


    /**
     * Contains all the outgoing edges of the stop
     * */
    public Set<Connection> getOutgoingEdges(){
        return Collections.unmodifiableSet(outgoingEdges);
    }


    /**
     * Contains all the ingoing edges of the stop
     * */
    public Set<Connection> getIngoingEdges(){
        return Collections.unmodifiableSet(ingoingEdges);
    }


    /**
     * Returns all the ingoing and outgoing edges of the stop
     * */
    public Set<Connection> getAllEdgesOfStop(){
        Set<Connection> allStopEdges = new HashSet<Connection>(ingoingEdges);
        allStopEdges.addAll(outgoingEdges);
        return Collections.unmodifiableSet(allStopEdges);
    }


    public Location getStopLocation() {
        return stopLocation;
    }


    public String getStopId() {
        return stopId;
    }


    /**
     *  Creates the location object of the stop based on its parameters.
     * */
    private void setStopLocation(float lati, float longi) {
        stopLocation = Location.newFromLatLon(lati, longi);
    }
}