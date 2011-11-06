package textmatch;

import java.util.*;

import org.junit.*;

import static org.junit.Assert.*;

import static textmatch.GStringUtils.*;

import static textmatch.GCollectionUtils.*;

public class GStringUtilsTest {
    @Test
    public void testJoin() {
        List<String> list = new ArrayList<String>();
        list.add("abc");
        list.add("def");
        assertEquals("abc def", join(list, " "));
    }
    
    @Test
    public void testStripPrefix() {
        assertEquals("cdefg", stripPrefix("abcdefg", "ab"));
        assertEquals("abcdefg", stripPrefix("abcdefg", "bc"));
    }
    
    @Test
    public void testStripSuffix() {
        assertEquals("abcde", stripSuffix("abcdefg", "fg"));
        assertEquals("abcdefg", stripSuffix("abcdefg", "ab"));
    }
    
    @Test
    public void testSplit() {
        String[] expected = new String[] {"abc", "efgh"};
        assertArrayEquals(expected, split("abc^efgh", '^'));
        expected = new String[] {"abc", "efgh", "i"};
        assertArrayEquals(expected, split("abc^efgh^i", '^'));
        expected = new String[] {"", "abc", "efgh", ""};
        assertArrayEquals(expected, split("^abc^efgh^", '^'));
        expected = new String[] {"", "abc", "", "efgh", "", ""};
        assertArrayEquals(expected, split("^abc^^efgh^^", '^'));
    }
    

}
