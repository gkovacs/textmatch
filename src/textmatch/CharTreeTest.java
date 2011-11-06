package textmatch;

import java.util.*;

import org.junit.*;

import static org.junit.Assert.*;

import static textmatch.CharTree.*;
import static textmatch.GStringUtils.split;

public class CharTreeTest {
    @Test
    public void testCharTreeToString() {
        assertEquals("", charTreeToString(null));
        CharTree a = new CharTree(null, 'a');
        CharTree b = new CharTree(a, 'b');
        CharTree c = new CharTree(a, 'c');
        assertEquals("ab", charTreeToString(b));
        assertEquals("ac", charTreeToString(c));
        assertEquals("a", charTreeToString(a));
    }
}
