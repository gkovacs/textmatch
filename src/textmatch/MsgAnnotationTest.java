package textmatch;

import java.util.*;

import org.junit.*;

import static org.junit.Assert.*;

import static textmatch.GCollectionUtils.*;

public class MsgAnnotationTest {
    @Test
    public void testMsgAnnotationFromString() {
        MsgAnnotation annotation = new MsgAnnotation("foo.png(1,2,3,4)~~~^sa^foo");
        assertEquals("foo.png", annotation.filename);
        assertEquals(1, annotation.x);
        assertEquals(2, annotation.y);
        assertEquals(3, annotation.w);
        assertEquals(4, annotation.h);
        String[] expectedTemplateSubstitutions = new String[] {"sa", "foo"};
        assertArrayEquals(expectedTemplateSubstitutions, annotation.templateSubstitutions);
    }
}
