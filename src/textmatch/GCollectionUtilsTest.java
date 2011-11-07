package textmatch;

import java.util.*;

import org.junit.*;

import static org.junit.Assert.*;

import static textmatch.GCollectionUtils.*;

public class GCollectionUtilsTest {
    @Test
    public void testSingleElemList() {
	List<String> expected = new LinkedList<String>();
	expected.add("foobar");
	assertEquals(expected, singleElemList("foobar"));
	assertNotSame(expected, singleElemList("foobare"));
    }
    
    @Test
    public void testSlice() {
        List<String> expected = new ArrayList<String>();
        expected.add("1");
        expected.add("2");
        expected.add("3");
        String[] input = new String[] {"0", "1", "2", "3", "4", "5"};
        assertEquals(expected, slice(input, 1, 4));
    }
    
    @Test
    public void testSubstrings() {
        String[] words = new String[] { "foo", "bar", "en"};
        List<List<String>> exp = new ArrayList<List<String>>();
        exp.add(toList(new String[0]));
        exp.add(toList(new String[] {"foo"} ));
        exp.add(toList(new String[] {"foo", "bar"} ));
        exp.add(toList(new String[] {"bar"} ));
        exp.add(toList(new String[] {"foo", "bar", "en"} ));
        exp.add(toList(new String[] {"bar", "en"} ));
        exp.add(toList(new String[] {"en"} ));
        assertEquals(exp, toList(substrings(toList(words))));
    }
    
    @Test
    public void testSubstrings3() {
        String[] words = new String[] { "foo", "bar", "en"};
        List<List<String>> exp = new ArrayList<List<String>>();
        exp.add(toList(new String[] {"foo"} ));
        exp.add(toList(new String[] {"foo", "bar"} ));
        exp.add(toList(new String[] {"bar"} ));
        exp.add(toList(new String[] {"foo", "bar", "en"} ));
        exp.add(toList(new String[] {"bar", "en"} ));
        exp.add(toList(new String[] {"en"} ));
        assertEquals(exp, toList(substrings(toList(words), 1, 3)));
    }
    
    @Test
    public void testSubstrings4() {
        String[] words = new String[] { "foo", "bar", "en"};
        List<List<String>> exp = new ArrayList<List<String>>();
        exp.add(toList(new String[] {"foo", "bar"} ));
        exp.add(toList(new String[] {"foo", "bar", "en"} ));
        exp.add(toList(new String[] {"bar", "en"} ));
        assertEquals(exp, toList(substrings(toList(words), 2, 3)));
    }
    
    @Test
    public void testSubstrings2() {
        String[] words = new String[] { "foo", "bar", "en"};
        List<List<String>> exp = new ArrayList<List<String>>();
        exp.add(toList(new String[] {"foo"} ));
        exp.add(toList(new String[] {"foo", "bar"} ));
        exp.add(toList(new String[] {"bar"} ));
        exp.add(toList(new String[] {"bar", "en"} ));
        exp.add(toList(new String[] {"en"} ));
        assertEquals(exp, toList(substrings(toList(words), 1, 2)));
    }
}
