import java.util.*;

public class Trie {

    private TrieNode root;
    private TrieNode curNode;

    public Trie() {
        root = new TrieNode();
    }


    /**
     * Through each node character it traverses down till no more characters in the word
     * then the stop is set to that node.
     * If it doesn't exist, a node will be created.
     */
    public void add(char[] word, Stop stop) {
        curNode = root;
        for(char ch : word) {
            if(!curNode.getChildren().containsKey(ch)) {
                curNode.addChild(ch, new TrieNode());
            }
            curNode = curNode.getChildren().get(ch);
        }
        curNode.setStop(stop);
    }


    /**
     * Searches down the trie from the root. Moves to each child node related to each character.
     * If a child isn't found, null is returned.
     */
    public Stop get(char[] word){
        curNode = root;
        for(char ch : word) {
            if(!curNode.getChildren().containsKey(ch)) {
                return null;
            }
            curNode = curNode.getChildren().get(ch);
        }
        return curNode.getStop();
    }


    /**
     * Helper method for calling 'getAllFrom'. Traverses down till it reaches the end of the prefix char
     * and returns null.
     * If it reaches the prefix char, 'getAllFrom' is called on the node with the array.
     */
    public List<Stop> getAll(char[] prefix){
        List<Stop> results = new ArrayList<Stop>();
        curNode = root;
        for(char c : prefix) {
            if(!curNode.getChildren().containsKey(c)) {
                return null;
            }
            curNode = curNode.getChildren().get(c);
        }
        allFrom(curNode, results);
        return results;
    }


    /**
     * Adds the node's stop to results if it does not return null, then recurses down all the children of the node.
     */
    public void allFrom(TrieNode node, List<Stop> results) {
        if(node.getStop() != null) {
            results.add(node.getStop());
        }
        for(TrieNode n : node.getChildren().values()) {
            allFrom(n, results);
        }
    }

}