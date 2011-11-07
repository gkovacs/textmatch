package textmatch;

import static textmatch.LCS.*;
import java.util.*;

import org.junit.*;

import static textmatch.GStringUtils.*;

import static textmatch.GCollectionUtils.*;

import static textmatch.POMsgSource.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class POMsgSourceTest {
    @Test
    public void testBasicPair() throws Exception {
        List<String> po = new ArrayList<String>();
        po.add("msgid \"gesture|Move left\"");
        po.add("msgstr \"Mover a la izquierda\"");
        POMsgSource msgsrc = new POMsgSource(po);
        List<String> expected = singleElemList("gesture|Move left");
        assertEquals(expected, msgsrc.getMsgStrings());
    }
    
    @Test
    public void testMultiLines() throws Exception {
        List<String> po = new ArrayList<String>();
        po.add("msgid \"\"");
        po.add("\"Indicates whether to close the shell when an upgrade or uninstall action is \"");
        po.add("\"performed.\"");
        POMsgSource msgsrc = new POMsgSource(po);
        List<String> expected = singleElemList("Indicates whether to close the shell when an upgrade or uninstall action is performed.");
        assertEquals(expected, msgsrc.getMsgStrings());
    }
    
    @Test
    public void testIgnoreComments() throws Exception {
        List<String> po = new ArrayList<String>();
        po.add("msgid \"\"");
        po.add("\"Indicates whether to close the shell when an upgrade or uninstall action is \"");
        po.add("#comment text2");
        po.add("\"performed.\"");
        POMsgSource msgsrc = new POMsgSource(po);
        List<String> expected = singleElemList("Indicates whether to close the shell when an upgrade or uninstall action is performed.");
        assertEquals(expected, msgsrc.getMsgStrings());
    }
    
    @Test
    public void testTemplateSubstitution() throws Exception {
        List<String> po = new ArrayList<String>();
        po.add("msgid \"\"");
        po.add("\"The shortcut \\\"%s\\\" cannot be used because it will become impossible to type \"");
        po.add("\"using this key.\\n\"");
        po.add("\"Please try with a key such as Control, Alt or Shift at the same time.\"");
        POMsgSource msgsrc = new POMsgSource(po);
        List<String> expected = singleElemList("The shortcut \"" + SUBCHAR +"\" cannot be used because it will become impossible to type using this key. Please try with a key such as Control, Alt or Shift at the same time.");
        assertEquals(expected, msgsrc.getMsgStrings());
    }
    
    @Test
    public void testTextFromMsgIdBlock() throws Exception {
        List<String> po = new ArrayList<String>();
        po.add("msgid \"\"");
        po.add("\"Indicates whether to close the shell when an upgrade or uninstall action is \"");
        po.add("\"performed.\"");
        String expected = "Indicates whether to close the shell when an upgrade or uninstall action is performed.";
        assertEquals(expected, textFromMsgIdBlock(join(po, "\n")));
    }
    
    @Test
    public void testAnnotationFromMsgIdBlock() throws Exception {
        List<String> po = new ArrayList<String>();
        po.add("#& foo.png(1,2,3,4)~~~分sa分foo");
        po.add("msgid \"\"");
        po.add("\"Indicates whether to close the shell when an upgrade or uninstall action is \"");
        po.add("\"performed.\"");
        MsgAnnotation annotation = annotationFromMsgIdBlock(join(po, "\n"));
        assertEquals("foo.png", annotation.filename);
        assertEquals(1, annotation.x);
        assertEquals(2, annotation.y);
        assertEquals(3, annotation.w);
        assertEquals(4, annotation.h);
        String[] expectedTemplateSubstitutions = new String[] {"sa", "foo"};
        assertArrayEquals(expectedTemplateSubstitutions, annotation.templateSubstitutions);
    }
    
    @Test
    public void testSplitIntoMsgIdBlocks() throws Exception {
        List<String> po = new ArrayList<String>();
        po.add("msgid \"- GNOME Mouse Preferences\"");
        po.add("msgid \"\"");
        po.add("\"Indicates whether to close the shell when an upgrade or uninstall action is \"");
        po.add("#comment text2");
        po.add("\"performed.\"");
        POMsgSource msgsrc = new POMsgSource(po);
        List<String> ex = new ArrayList<String>();
        ex.add("msgid \"- GNOME Mouse Preferences\"");
        ex.add("msgid \"\"" + '\n' +
               "\"Indicates whether to close the shell when an upgrade or uninstall action is \"" + '\n' +
               "#comment text2" + '\n' +
               "\"performed.\"");
        assertEquals(ex, msgsrc.splitIntoMsgIdBlocks());
    }
    
    @Test
    public void testSplitIntoMsgIdBlocks2() throws Exception {
        List<String> po = new ArrayList<String>();
        po.add("msgid \"- GNOME Mouse Preferences\"");
        po.add("#& some annotation");
        po.add("msgid \"\"");
        po.add("\"Indicates whether to close the shell when an upgrade or uninstall action is \"");
        po.add("\"performed.\"");
        POMsgSource msgsrc = new POMsgSource(po);
        List<String> ex = new ArrayList<String>();
        ex.add("msgid \"- GNOME Mouse Preferences\"");
        ex.add("#& some annotation" + '\n' +
               "msgid \"\"" + '\n' +
               "\"Indicates whether to close the shell when an upgrade or uninstall action is \"" + '\n' +
               "\"performed.\"");
        assertEquals(ex, msgsrc.splitIntoMsgIdBlocks());
    }
    
    @Test
    public void testSplitIntoMsgIdBlocks3() throws Exception {
        List<String> po = new ArrayList<String>();
        po.add("#. comment1");
        po.add("msgid \"- GNOME Mouse Preferences\"");
        po.add("#. comment2");
        po.add("#. comment3");
        po.add("msgid \"foobar\"");
        POMsgSource msgsrc = new POMsgSource(po);
        List<String> ex = new ArrayList<String>();
        ex.add("#. comment1" + '\n' +
               "msgid \"- GNOME Mouse Preferences\"");
        ex.add("#. comment2" + '\n' +
               "#. comment3" + '\n' +
               "msgid \"foobar\"");
        assertEquals(ex, msgsrc.splitIntoMsgIdBlocks());
    }
    
    @Test
    public void testAnnotateTemplate() throws Exception {
        HashMap<String, String> annotations = new HashMap<String, String>();
        List<String> po = new ArrayList<String>();
        po.add("msgid \"\"");
        po.add("\"Indicates whether to close the shell when an upgrade or uninstall action is \"");
        po.add("\"performed.\"");
        annotations.put("Indicates whether to close the shell when an upgrade or uninstall action is performed.", "some annotation");
        List<String> ex = new ArrayList<String>();
        ex.add("#& some annotation");
        ex.add("msgid \"\"");
        ex.add("\"Indicates whether to close the shell when an upgrade or uninstall action is \"");
        ex.add("\"performed.\"");
        assertEquals(join(ex, "\n"), new POMsgSource(po).makeAnnotatedMsgFile(annotations));
    }
    
    @Test
    public void testAnnotateTemplate2() throws Exception {
        HashMap<String, String> annotations = new HashMap<String, String>();
        List<String> po = new ArrayList<String>();
        po.add("msgid \"foo bar\"");
        po.add("msgid \"click clack\"");
        annotations.put("click clack", "some annotation");
        List<String> ex = new ArrayList<String>();
        ex.add("msgid \"foo bar\"");
        ex.add("#& some annotation");
        ex.add("msgid \"click clack\"");
        assertEquals(join(ex, "\n"), new POMsgSource(po).makeAnnotatedMsgFile(annotations));
    }
}
