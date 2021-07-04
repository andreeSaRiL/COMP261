package code;

public class HuffmanNode implements Comparable<HuffmanNode> {
    HuffmanNode left;
    HuffmanNode right;
    HuffmanNode parent;
    String huffmanCode = ""; //for saving HuffmanCode
    char letter;
    int frequency;

    public HuffmanNode(char letter, int frequency) {
        super();
        this.letter = letter;
        this.frequency = frequency;
    }
    public HuffmanNode(HuffmanNode left, HuffmanNode right) {
        super();
        this.left = left;
        this.right = right;
        this.left.parent = parent; //CHANGE TO NULL?
        this.right.parent = parent;
    }

    @Override
    public int compareTo(HuffmanNode node) {
        if (this.frequency < node.getFrequency()) { return -1; }
        else if (this.frequency > node.getFrequency()) { return 1; }
        else { return 0; }
    }

    /**
     * Getters and Setters.
     * @return
     */
    public HuffmanNode getLeft() { return left; }
    public HuffmanNode getRight() { return right; }
    public HuffmanNode getParent() { return parent; }
    public char getLetter() { return letter; }
    public int getFrequency() { return frequency; }
    public String getCoding() { return this.huffmanCode; }

    public void setLeft(HuffmanNode left) { this.left = left; }
    public void setRight(HuffmanNode right) { this.right = right; }
    public void setParent(HuffmanNode parent) { this.parent = parent; }
    public void setLetter(char letter) { this.letter = letter; }
    public void setFrequency(int frequency) { this.frequency = frequency; }



}
