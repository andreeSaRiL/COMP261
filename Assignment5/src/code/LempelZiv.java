package code;

import java.util.ArrayList;

/**
 * A new instance of LempelZiv is created for every run.
 */
public class LempelZiv {
	ArrayList<Tuples> tuples = new ArrayList<>();
	private int WINDOW_SIZE = 250;

	/**
	 * Take uncompressed input as a text string, compress it, and return it as a
	 * text string.
	 */
	public String compress(String input) {
		// TODO fill this in.

		System.out.println("Window Size:" + WINDOW_SIZE);
		int cursor = 0;
		StringBuilder output = new StringBuilder();

		while(cursor < input.length()) {
			int length = 0;
			int prev = -1;

			while(true){
				int start;
				start = (cursor < WINDOW_SIZE)? 0 : cursor - WINDOW_SIZE;
				String pattern = input.substring(cursor, cursor + length);
				String str = input.substring(start, cursor);
				int match = str.indexOf(pattern);

				if(cursor + length >= input.length()){ match = -1; }
				if(match > -1){	//If it is a correct match
					prev = match;
					length++;
				}else { //Must be a new tuple
					int offset;
					offset = (prev > -1)? str.length() - prev : 0;
					char nextCharacter = input.charAt(cursor+ length - 1);

					Tuples tuple = new Tuples(offset,length - 1, nextCharacter);
					tuples.add(tuple);
					output.append(tuple.toString());
					cursor = cursor + length;
					break;
				}
			}
		}
		return output.toString();
	}

	/**
	 * Take compressed input as a text string, decompress it, and return it as a
	 * text string.
	 */
	public String decompress(String compressed) {
		// TODO fill this in.

		StringBuilder output = new StringBuilder();
		int cursor = 0;

		for(Tuples tuple : tuples){
			if(tuple.length == 0 && tuple.offset == 0){
				cursor++;
				output.append(tuple.nextCharacter);
			}else{
				output.append(output.substring(cursor - tuple.offset, cursor - tuple.offset + tuple.length));
				cursor = cursor + tuple.length;
				if (tuple.nextCharacter != null) { output.append(tuple.nextCharacter); }
				cursor++;
			}
		}
		return output.toString();
	}

	/**
	 * The getInformation method is here for your convenience, you don't need to
	 * fill it in if you don't want to. It is called on every run and its return
	 * value is displayed on-screen. You can use this to print out any relevant
	 * information from your compression.
	 */
	public String getInformation() {
		StringBuilder stringB = new StringBuilder();
		for (Tuples tupe : tuples) {
			stringB.append(tupe).append("\n");
		}
		return stringB.toString();
	}
}
