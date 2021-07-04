import java.util.*;

public class TrieNode {
    private Stop stop;
    private Map<Character, TrieNode> children;


    public TrieNode() {
        children = new HashMap<Character, TrieNode>();
    }


    public void addChild(char c, TrieNode addNode) {
        children.put(c, addNode);
    }


    public void setStop(Stop s) {
        stop = s;
    }


    /**
     * The children nodes, so one is able to move down.
     * */
    public Map<Character, TrieNode> getChildren() {
        return Collections.unmodifiableMap(children);
    }


    public Stop getStop(){
        return stop;
    }

}