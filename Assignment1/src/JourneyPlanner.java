import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.*;

public class JourneyPlanner extends GUI {
    //variables for the graph
    private static final double scale = 1.5;
    private static final double move = 1.5;

    //collections for holding stop, trip connection and highlighted connections and stops
    private Set<Connection> highlightedConnections = new HashSet<Connection>();
    private Set<Connection> connections = new HashSet<Connection>();
    private Set<Stop> selectedStops = new HashSet<Stop>();
    private Map<String, Stop> stopsById = new HashMap<String, Stop>();
    private Map<String, Trip> tripsById = new HashMap<String, Trip>();
    private Trie stopsName = new Trie();

    private double size = 10;
    private double width;
    private double height;
    private Location origin;
    private Stop selectedStop;
    private boolean isLoaded = false;



    /**draws all bus stops and connections between them
     */
    @Override
    protected void redraw(Graphics g) {
        for (Map.Entry<String, Stop> stop : stopsById.entrySet()) {
            stop.getValue().drawStop(g, origin, size);
        }
        for(Connection conn : connections) {
            conn.drawConnection(g);
        }
    }


    /**
     * Finds the closest stop of the Mouse Click.
     * This is done by converting the Mouse Click from the pixel point to a location.
     * The distance is compared to find the closest stop to the mouse click.
     */
    @Override
    protected void onClick(MouseEvent e) {
        if(stopsById.isEmpty() || !isLoaded) {
            return;
        }
        unselectAll();	 //removes previous selection
        Point clickPoint = e.getPoint();
        Location clickLocation = Location.newFromPoint(clickPoint, origin, size);
        Stop closestStop = null;
        double closestDist = Double.POSITIVE_INFINITY;

        for(Map.Entry<String, Stop> stop : stopsById.entrySet()) {
            if(closestStop == null || (stop.getValue().getStopLocation().distance(clickLocation) < closestDist)) {
                closestStop = stop.getValue();
                closestDist = stop.getValue().getStopLocation().distance(clickLocation);
            }
        }
        selectStop(closestStop, false);
    }


    /**
     * Gets the text from the search box and checks if there is a stop containing the characters that has been typed
     * it is selected and highlighted for all stops that contain the certain characters.
     */
    @Override
    protected void onSearch() {
        if(isLoaded) {
            unselectAll();
            String searchText = getSearchBox().getText();
            if(searchText == null || searchText.equals("")) {
                return;
            }
            Stop foundStop = stopsName.get(searchText.toCharArray());
            if(foundStop == null) {
                List<Stop> foundStops = new ArrayList<Stop>();
                if(stopsName.getAll(searchText.toCharArray()) != null) {
                    foundStops.addAll(stopsName.getAll(searchText.toCharArray()));
                    selectStop(foundStops, false);
                }
            }else {
                selectStop(foundStop, true);
            }
        }
    }


    /**
     * Moves the graph based on what button has been pressed, moves the origin.
     * The zoom works by adjusting the size first, zoom in: the origin is moved across and down, zoom out: out and up
     */
    @Override
    protected void onMove(Move m) {
        if(isLoaded) {
            switch(m) {
                case NORTH:
                    origin = new Location(origin.x, origin.y + move);
                    break;
                case SOUTH:
                    origin = new Location(origin.x, origin.y - move);
                    break;
                case EAST:
                    origin = new Location(origin.x + move, origin.y);
                    break;
                case WEST:
                    origin = new Location(origin.x - move, origin.y);
                    break;

                case ZOOM_IN:
                    size *= scale;
                    origin = origin.moveBy((width-(width/scale))/2, -(height -(height/scale))/2);
                    width /= scale;
                    height /= scale;
                    break;
                case ZOOM_OUT:
                    size /= scale;
                    origin = origin.moveBy(-((width*scale)-width)/2, ((height*scale)-height)/2);
                    width *= scale;
                    height *= scale;
                    break;
            }
        }

    }


    /**
     *Calls the methods to get the text from the files and parse them into stop, connection and trip objects
     */
    @Override
    protected void onLoad(File stopFile, File tripFile) {
        fillStops(parseFile(stopFile));
        fillTrips(parseFile(tripFile));
        setOrigin();
        setStartWidthHeightLocation();
        if(!stopsById.isEmpty() && !connections.isEmpty()) {
            isLoaded = true;
        }
    }


    /**
     * To calculate the origin, the max and min positions of the bus stops are found
     * to set the origin variable.
     */
    private void setOrigin() {
        if (!stopsById.isEmpty()) {
            List<Stop> stops = new ArrayList<Stop>(stopsById.values());
            Collections.sort(stops, (Stop stop1, Stop stop2) -> { //Stops are sorted based on the x pos
                if (stop1.getStopLocation().x < stop2.getStopLocation().x) {
                    return -1;
                } else if (stop1.getStopLocation().x > stop2.getStopLocation().x) {
                    return 1;
                } else
                    return 0;
            });

            double minLocationX = stops.get(0).getStopLocation().x;
            Collections.sort(stops, (Stop stop1, Stop stop2) -> { //Stops are sorted based on the y pos
                if (stop1.getStopLocation().y < stop2.getStopLocation().y) {
                    return -1;
                } else if (stop1.getStopLocation().y > stop2.getStopLocation().y) {
                    return 1;
                } else
                    return 0;
            });

            double maxLocationY = stops.get(stops.size() - 1).getStopLocation().y; //adjusted to the top-left origin so all stops are displayed on the panel
            origin = new Location(minLocationX, maxLocationY);
        }
    }


    /**
     * Creates the connection object then adds it to the connections collection and the stops lists
     */
    private void connectStops(String tripId, Stop prevStop, Stop currStop) {
        Connection stopsConn = new Connection(tripId, prevStop, currStop);
        connections.add(stopsConn);
        prevStop.addOutgoingEdge(stopsConn);
        currStop.addIngoingEdge(stopsConn);
    }


    /**
     * Creates the trip objects and the connections between stops on the trip.
     * It reads over the lines of the Stringbuilder to create a trip for every line.
     */
    private void fillTrips(StringBuilder tripParsed) {
        try {
            Scanner tripScan = new Scanner(tripParsed.toString());
            tripScan.useDelimiter("\\t|\\n");   //uses tab or new line as delimiter

            if(tripScan.hasNextLine()) {
                tripScan.nextLine();    //Title
            }
            while (tripScan.hasNextLine()) {
                String tripId = tripScan.next();
                Trip tempTrip = new Trip(tripId);
                tripsById.put(tripId, tempTrip);

                String scanStops = tripScan.nextLine();   //stores the rest of the lines
                Scanner stopsScanner = new Scanner(scanStops);
                Stop currStop;
                Stop prevStop = null;
                while (stopsScanner.hasNext()) {
                    String currStopId = stopsScanner.next();
                    currStop = stopsById.get(currStopId);
                    tempTrip.addStop(currStop);

                    if (prevStop != null) {
                        connectStops(tripId, prevStop, currStop);
                    }
                    prevStop = currStop;
                }
                stopsScanner.close();
            }
            tripScan.close();
        } catch (Exception e) {
            getTextOutputArea().setText("Error creating Trips. Trips file must be in the correct format. " + e);
        }
    }


    /**
     * Parses the Stringbuilder into smaller parts to create stop objects and adds to the collection.
     * The delimtter is used to separate by tabs to scan different parts.
     */
    private void fillStops(StringBuilder stopParsed) {
        try {
            Scanner stopScan = new Scanner(stopParsed.toString());
            stopScan.useDelimiter("\\t|\\n");

            if (stopScan.hasNextLine()) {
                stopScan.nextLine();
            }
            while (stopScan.hasNextLine()) {
                String id = stopScan.next();
                String name = stopScan.next();
                float lon = stopScan.nextFloat();
                float lat = stopScan.nextFloat();
                Stop newStop = new Stop(id, name, lon, lat);
                stopsById.put(id, newStop);
                stopsName.add(name.toCharArray(), newStop);
                stopScan.nextLine();
            }
            stopScan.close();
        } catch (Exception e) {
            getTextOutputArea().setText("Error creating Stops. Stops file must be in the correct format. " + e);
        }
    }


    /**
     * Takes a file and reads it over using Filereader and Bufferedreader
     * will be added to the String builder and then returned.
     */
    private StringBuilder parseFile(File parseFile) {
        StringBuilder fileParsed = new StringBuilder();
        try {
            FileReader readFile = new FileReader(parseFile); //get the file and read its contents, then passing to the buffered reader
            BufferedReader fileBuffer = new BufferedReader(readFile);
            String line;
            while ((line = fileBuffer.readLine()) != null) {
                fileParsed.append(line).append("\n");
            }
            readFile.close();
            fileBuffer.close();
        } catch (IOException e) {
            getTextOutputArea().setText("Error reading and parsing file. Error: " + e);
            return null;
        }
        return fileParsed;
    }


    /**
     * The points are based on off of the corner points of the area
     * the screen's locations width and height used relatively to the origin location.
     */
    private void setStartWidthHeightLocation() {
        Dimension areaDim = getDrawingAreaDimension();
        Location topLeft;
        Location topRight;
        Location botRight;
        Point topLPoint = new Point();
        Point topRPoint = new Point();
        Point bottomRPoint = new Point();

        topLPoint.setLocation(0, areaDim.getHeight());
        topRPoint.setLocation(areaDim.getWidth(), areaDim.getHeight());
        bottomRPoint.setLocation(areaDim.getWidth(), 0);
        topLeft = Location.newFromPoint(topLPoint, origin, size);
        topRight = Location.newFromPoint(topRPoint, origin, size);
        botRight = Location.newFromPoint(bottomRPoint, origin, size);

        width = topRight.x - topLeft.x; //sets the initial width and height from the corner locations
        height = botRight.y - topRight.y;

    }


    /**
     * Selects the stop within the stop class which will be drawn in red.
     * A set will be used to store the trip Ids.
     * No duplicates are stored, the names of the stops and trip Ids are printed.
     * The trips are highlighted if the boolean is true.
     */
    private void selectStop(Stop selStop, boolean highlightTripsOn){
        selStop.select();
        selectedStop = selStop;
        Set<String> stopTripIds = new HashSet<String>();
        for(Connection c : selStop.getAllEdgesOfStop()) {
            stopTripIds.add(c.getTripID());
        }
        getTextOutputArea().setText("Selected Stop Name: " + selStop.getName() + "\nID's of Trips through this Stop: " + stopTripIds.toString());

        if(highlightTripsOn) {
            highlightTrips(stopTripIds);
        }
    }


    private void selectStop(List<Stop> stops, boolean highlightTripsOn){
        for(Stop stop : stops) {
            stop.select();
            selectedStops.add(stop);
        }

        Set<String> stopTripIds = new HashSet<String>();
        for(Stop s : stops) {
            for(Connection c : s.getAllEdgesOfStop()) {
                stopTripIds.add(c.getTripID());
            }
        }
        String namesMatch = "Name of Stops matching Search prefix: ";
        for(Stop stop : stops) {
            namesMatch += stop.getName() + ", ";
        }
        getTextOutputArea().setText(namesMatch + "\nID's of Trips through this Stop: " + stopTripIds.toString());

        if(highlightTripsOn) {
            highlightTrips(stopTripIds);
        }
    }


    /**
     * Unselects and clears all the selected stops and connections.
     * Clears the text printed for previous stops.
     */
    private void unselectAll() {
        getTextOutputArea().setText("");
        if(selectedStop != null) {
            selectedStop.unSelect();
            selectedStop = null;
        }
        if(!selectedStops.isEmpty()) {
            for(Stop stop : selectedStops) {
                stop.unSelect();
            }
            selectedStops.clear();
        }
        if(!highlightedConnections.isEmpty()) {
            for(Connection conn : highlightedConnections) {
                conn.unSelect();
            }
            highlightedConnections.clear();
        }
    }


    /**
     * Selects all the connections for all the trips related to stops
     */
    public void highlightTrips(Set<String> stopTrips) {
        for(Connection conn : connections) {
            if(stopTrips.contains(conn.getTripID())) {
                conn.select();
                highlightedConnections.add(conn);
            }
        }
    }


    public static void main(String[] args) {
        new JourneyPlanner();
    }

}