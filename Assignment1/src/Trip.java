import java.util.*;

public class Trip {

    private String tripId;
    private List<Stop> stops = new ArrayList<Stop>();


    public Trip(String tripId) {
        this.tripId = tripId;
    }


    public void addStop(Stop s){
        stops.add(s);
    }


    /**
     * The collection contains all the stops on the trip.
     * */
    public List<Stop> getStops(){
        return Collections.unmodifiableList(stops);
    }


    public String getTripId() {
        return tripId;
    }
}