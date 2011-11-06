package textmatch;

import static textmatch.GCollectionUtils.count;
import static textmatch.GCollectionUtils.setAll;

public class CharTree {
    // Constructs a string by going from child to root
    // (root is first character, deepest is the last character)
    
    private CharTree parent;
    private char letter;
    
    public CharTree(CharTree parent, char letter) {
        this.parent = parent;
        this.letter = letter;
    }
    
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        CharTree cur = this;
        while (cur != null) {
            b.append(cur.letter);
            cur = cur.parent;
        }
        b.reverse();
        return b.toString();
    }
    
    public static String charTreeToString(CharTree c) {
        if (c == null)
            return "";
        return c.toString();
    }
}
