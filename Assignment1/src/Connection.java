import java.awt.Color;
import java.awt.Graphics;

public class Connection {

    private Color col;
    private boolean selected;
    private Stop fromStop;       //stop the connection starts at
    private Stop toStop;        //stop that the connection ends at
    private String tripId;



    public Connection(String tripId, Stop fromStop, Stop toStop){
        this.tripId = tripId;
        this.fromStop = fromStop;
        this.toStop = toStop;
        selected = false;
    }


    /**
     * Draws line from 'to stop', where the colour depends if it's selected/not
     * */
    public void drawConnection(Graphics g) {
        if(selected) {
            g.setColor(col);
        }else {
            g.setColor(Color.GRAY);
        }
        g.drawLine(fromStop.getPoint().x, fromStop.getPoint().y, toStop.getPoint().x, toStop.getPoint().y);
    }


    public String toString() {
        return "Trip ID: " + tripId + "\t From Stop: " + fromStop.getName() + "\t To Stop: " + toStop.getName();
    }


    public void select() {
        selected = true;
        col = Color.MAGENTA;
    }


    public void unSelect() {
        selected = false;
    }


    public Stop getFromStop() {
        return fromStop;
    }


    public Stop getToStop() {
        return toStop;
    }


    public String getTripID() {
        return tripId;
    }
}