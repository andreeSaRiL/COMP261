public class FringeObject implements Comparable<FringeObject> {
    private Node currNode;
    private Node prevNode;
    private double costFromStart; //true cost for the path from the start to curr node
    private double estCost; //the overall estimated cost; the cost and estimated dist to the end node


    public FringeObject(Node currNode, Node prevNode, double costFromStart, double estCost) {
        this.currNode = currNode;
        this.prevNode = prevNode;
        this.costFromStart = costFromStart;
        this.estCost = estCost;
    }

    /**
     *Sorts the priority queue so the fringe objects with the lower estimated cost is at the top
     * */
    public int compareTo(FringeObject other) {
        if (this.estCost < other.estCost) {
            return -1;
        } else if (this.estCost > other.estCost) {
            return 1;
        } else {
            return 0;
        }
    }

    public Node getCurrNode() {
        return currNode;
    }

    public Node getPrevNode() {
        return prevNode;
    }

    public double getCostFromStart() {
        return costFromStart;
    }


}