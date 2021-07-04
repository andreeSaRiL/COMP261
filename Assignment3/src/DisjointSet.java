import java.util.Set;
public class DisjointSet {
    /**
     * Updates the Disjoint parent and depth of the given node, then returns it.
     * */
    public static Node makeSet(Node x) {
        x.setDisjointParent(x);
        x.setDisjointDepth(0);
        return x;
    }

    /**
     * A Recursive to find the disjoint parent of the node.
     * Recurse up the inverted tree set until the node is the parent of itself, and returned back down.
     * */
    public static Node find(Node x) {
        Node root;
        if (x.getDisjointParent() == x) { return x; }
        else {
            root = find(x.getDisjointParent());
            return root;
        }
    }

    /**
     * Merges two given nodes into the same disjoint set if the nodes are not already in the same tree,
     * will merge the smaller node into the larger node based off the disjoint depth.
     * */
    public static boolean union(Node x, Node y, Set<Node> forest) {
        Node rootX = find(x);
        Node rootY = find(y);

        if (rootX == rootY) { return false; }
        else if (rootX.getDisjointDepth() < rootY.getDisjointDepth()) {
            forest.remove(rootX);
            rootX.setDisjointParent(rootY);
            return true;
        }else {
            forest.remove(rootY);
            rootY.setDisjointParent(rootX);
            if (rootX.getDisjointDepth() == rootY.getDisjointDepth()) { rootX.setDisjointDepth(rootX.getDisjointDepth() + 1); }
            return true;
        }
    }
}