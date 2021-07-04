package code;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

/**
 * A new instance of HuffmanCoding is created for every run. The constructor is
 * passed the full text to be encoded or decoded, so this is a good place to
 * construct the tree. You should store this tree in a field and then use it in
 * the encode and decode methods.
 */
public class HuffmanCoding {
	Queue<HuffmanNode> treeQueue = new PriorityQueue<>();
	HashMap<Character, Integer> frequencyMap;
	HashMap<Character, String> codeMap;
	HuffmanNode root;

	/**
	 * This would be a good place to compute and store the tree.
	 * HuffmanCoding, creates a hashmap which stores every char's frequency and then checks if it contains the char given with frequency.
	 * Creates the Huffman tree and constructs the Huffman code. While loop goes through and removes the node that has been transferred.
	 * Checks the left and right child nodes, if they are empty then them to the stack.
	 * Once it has reached the lowest level in the tree, it has finished therefore add it to the map.
	 */
	public HuffmanCoding(String text) {
		HashMap<Character, Integer> freq = new HashMap<>();

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if(!freq.containsKey(c)) { freq.put(c, 1); }
			else { freq.put(c, freq.get(c) + 1); }
		}

		this.frequencyMap= freq;
		HuffmanTree tree = new HuffmanTree();
		this.treeQueue = tree.huffTree(frequencyMap);
		this.root = tree.rootNode;

		HashMap<Character, String> huffmanMap = new HashMap<>();
		Stack<HuffmanNode> stack = new Stack<>();
		stack.push(this.root);

		while (!stack.isEmpty()) {
			HuffmanNode node = stack.pop();

			if (node.left != null) {
				node.left.huffmanCode = (node.huffmanCode + '0');
				stack.push(node.left);
			}
			if (node.right != null) {
				node.right.huffmanCode = (node.huffmanCode + '1');
				stack.push(node.right);
			}
			else { huffmanMap.put(node.letter, node.getCoding()); }
		}
		this.codeMap = huffmanMap;
	}

	/**
	 * Take an input string, text, and encode it with the stored tree. Should
	 * return the encoded text as a binary string, that is, a string containing
	 * only 1 and 0.
	 */
	public String encode(String text) {
		StringBuilder encode = new StringBuilder();
		for(int index = 0; index< text.length(); index++){
			char c = text.charAt(index);
			encode.append(codeMap.get(c)); //Append the value of the char to encode.
		}
		return encode.toString();
	}

	/**
	 * Take encoded input as a binary string, decode it using the stored tree,
	 * and return the decoded text as a text string.
	 */
	public String decode(String encoded) {
		HuffmanNode rootNode = this.root;
		HuffmanNode node = this.root; //Initializing with the root node.
		StringBuilder decode = new StringBuilder();
		char[] charArray = encoded.toCharArray();

		int i = 0;
		while(i < charArray.length){
			char c = charArray[i];
			if(c == '0') { node = node.left; }
			else node = node.right;
			if(node.left == null|| node.right == null) {
				decode.append(node.letter);
				node = rootNode;
			}
			i++;
		}
		return decode.toString();
	}

	/**
	 * The getInformation method is here for your convenience, you don't need to
	 * fill it in if you don't wan to. It is called on every run and its return
	 * value is displayed on-screen. You could use this, for example, to print
	 * out the encoding tree.
	 */
	public String getInformation() {
		String getInfo = "";
		for (Map.Entry<Character, String> character : codeMap.entrySet()) {
			getInfo = getInfo + character.getKey() + ":" + character.getValue() + "\n";
		}
		return getInfo.toString();
	}
}