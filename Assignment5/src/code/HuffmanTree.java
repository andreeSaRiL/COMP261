package code;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.PriorityQueue;

public class HuffmanTree {
    HuffmanNode rootNode;

    /**constructor*/
    public HuffmanTree() {
    }

    /**
     * HuffmanTree creating a frequency queue.
     * Once the whole tree has gone through, then get the first value of the tree(queue)
     * and sets the root node of the tree.
     *
     * @param frequencyMap
     * @return
     */
    public PriorityQueue huffTree(HashMap<Character, Integer> frequencyMap) {
        PriorityQueue<HuffmanNode> queue = new PriorityQueue<>();

        for(Entry<Character, Integer> c : frequencyMap.entrySet()) {
            HuffmanNode node = new HuffmanNode(c.getKey(), c.getValue());
            queue.add(node);
        }

        while(queue.size()>1) {
            HuffmanNode left = queue.poll();
            HuffmanNode right = queue.poll();
            HuffmanNode parent = new HuffmanNode(left, right);
            int parentFrequency = left.frequency + right.frequency;

            //Setting the frequency of the parent, and left and right's parent.
            parent.setFrequency(parentFrequency);
            left.parent = parent;
            right.parent = parent;
            queue.add(parent);
        }

        this.rootNode = queue.peek();
        return queue;
    }
}