package code;

/**
 * A new KMP instance is created for every substring search performed. Both the
 * pattern and the text are passed to the constructor and the search method. You
 * could, for example, use the constructor to create the match table and the
 * search method to perform the search itself.
 */
public class KMP {
	int[] table;

	public KMP(String pattern, String text) {
		// TODO maybe fill this in.

		table = new int[pattern.length()];
		table[0] = -1;
		int k = 0 ;
		int pos = 1;

		while(pos < pattern.length() - 1) {
			if(pattern.charAt(pos) == pattern.charAt(k)) {
				table[pos] = k+1;
				pos++;
				k++;
			}
			else if(k > 0){ k = table[k]; }
			else {
				table[pos] = 0;
				pos++;
			}
		}
	}

	/**
	 * Perform KMP substring search on the given text with the given pattern.
	 * 
	 * This should return the starting index of the first substring match if it
	 * exists, or -1 if it doesn't.
	 */
	public int search(String pattern, String text) {
		// TODO fill this in.

		int k = 0;
		int i = 0;
		long startTime = System.currentTimeMillis();

		while(k+i < text.length()) {
			if(pattern.charAt(i) == text.charAt(k+i)){
				i++;

				if(i == pattern.length()) {
					long endTime = System.currentTimeMillis();
					System.out.println("Time taken using KMP SEARCH: " + Math.abs(endTime - startTime));
					return k;
				}
			}else if(table[i] == -1) {
				i = 0;
				k = k + i + 1;
			}else {
				k = k + i - table[i];
				i = table[i];
			}
		}
		return -1;
	}

	/**
	 * Brute Force Search algorithm, where searches the substring on the given text with the given pattern.
	 *
	 * Returns the start of the index of the first substring if it exists.
	 * If either the pattern does not exist or the text length is shorter than the pattern,
	 * then returns -1.
	 */
	public int bruteSearch(String pattern, String text) {
		long startTime = System.currentTimeMillis();
		int patLength = pattern.length();
		int texLength = text.length();
		boolean found;

		if(patLength < 1) return -1; //Does not exist

		for(int i = 0; i < texLength - patLength+1; i++){
			found = true;
			for(int j = 0; j < patLength; j++){
				if(pattern.charAt(j) != text.charAt(i+j)){
					found = false;
					break;
				}
			}

			if(found == true) {
				long endTime = System.currentTimeMillis();
				System.out.println("Time taken using BRUTE SEARCH: " + Math.abs(endTime - startTime));
				return i;
			}
		}
		return -1;
	}
}
