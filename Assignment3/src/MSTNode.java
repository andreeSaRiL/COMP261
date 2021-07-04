public class MSTNode implements Comparable<MSTNode> {

    private Node N1;
    private Node N2;
    private double weight;
    private Segment connect; //Connecting segment

    public MSTNode(Node N1, Node N2, double weight, Segment connectingSegment) {
        this.N1 = N1;
        this.N2 = N2;
        this.weight = weight;
        this.connect = connectingSegment;
    }

    public Segment getConnect() { return connect; }
    public Node getNode1() { return N1; }
    public Node getNode2() {
        return N2;
    }

    @Override
    public int compareTo(MSTNode other) {
        if(this.weight < other.weight) { return -1; }
        else if(this.weight > other.weight) { return 1; }
        else { return 0; }
    }
}