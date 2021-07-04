package code;

/**
 * Tuples class used for Lempel-Ziv Compression.
 */
public class Tuples {
    int offset;
    int length;
    Character nextCharacter;

    public Tuples(int offset, int length, Character nextCharacter){
        this.offset = offset;
        this.length = length;
        this.nextCharacter = nextCharacter;
    }

    @Override
    public String toString() {
        return "[" + offset + ", " + length + ", " + nextCharacter + "]";
    }

}
