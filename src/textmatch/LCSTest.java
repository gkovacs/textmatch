package textmatch;

import java.util.*;

import org.junit.*;

import static org.junit.Assert.*;

import static textmatch.LCS.*;

import static textmatch.CharTree.*;
import static textmatch.GCollectionUtils.lastElem;

public class LCSTest {
    @Test
    public void LCSScoreTest() {
        assertEquals(4.0/10, LCSScore("abceghiqjk".toCharArray(), "achjq".toCharArray()), 0.01);
    }
    
    @Test
    public void LCSTemplateScoreTest() {
        assertEquals(1.0, LCSTemplatedScore("the%bar%thien".toCharArray(), "theamlakbarasgnlsakdnglathien".toCharArray()), 0.001);
        assertEquals(0.0, LCSTemplatedScore("the%bar%thien".toCharArray(), "qssy".toCharArray()), 0.001);
        assertEquals(0.5454545, LCSTemplatedScore("the%bar%thien".toCharArray(), "thebar".toCharArray()), 0.001);
        assertEquals(0.5454545, LCSTemplatedScore("the%bar%thien".toCharArray(), "theqssdgknetknrbar".toCharArray()), 0.001);
    }
    
    @Test
    public void GetSubstitutedStringsTest1() {
        Pair<int[][], CharTree[][]> p = LCSMatrixTemplated("thebar".toCharArray(), "thebar".toCharArray());
        String expected = "";
        assertEquals(expected, charTreeToString(lastElem(p.Item2)));
        p = LCSMatrixTemplated("the%bar".toCharArray(), "thebar".toCharArray());
        expected = "^";
        assertEquals(expected, charTreeToString(lastElem(p.Item2)));
        p = LCSMatrixTemplated("the%bar".toCharArray(), "theqasurbar".toCharArray());
        expected = "^qasur";
        assertEquals(expected, charTreeToString(lastElem(p.Item2)));
        p = LCSMatrixTemplated("the%bar%".toCharArray(), "theqasurbar".toCharArray());
        expected = "^qasur^";
        assertEquals(expected, charTreeToString(lastElem(p.Item2)));
        p = LCSMatrixTemplated("the%bar%thien".toCharArray(), "theqasurbarthien".toCharArray());
        expected = "^qasur^";
        assertEquals(expected, charTreeToString(lastElem(p.Item2)));
        p = LCSMatrixTemplated("the%bar%thien".toCharArray(), "theqasurbarORKEthien".toCharArray());
        expected = "^qasur^ORKE";
        assertEquals(expected, charTreeToString(lastElem(p.Item2)));
        p = LCSMatrixTemplated("If you reassign the shortcut to \"%\", the \"%\" shortcut will be disabled.".toCharArray(), "If you reassign the shortcut to \"Launch media player\", the \"Activate the window menu\" shortcut will be disabled.".toCharArray());
        expected = "^Launch media player^Activate the window menu";
        assertEquals(expected, charTreeToString(lastElem(p.Item2)));
        p = LCSMatrixTemplated("If you reassign the shortcut to \"%\", the \"%\" shortcut will be disabled.".toCharArray(), "If you reassign the shortcut to \"La unchmedia player\", the \"Activate the window menu\" shortcut will be disabled.".toCharArray());
        expected = "^La unchmedia player^Activate the window menu";
        assertEquals(expected, charTreeToString(lastElem(p.Item2)));
        p = LCSMatrixTemplated("If you reassign the shortcut to \"%\", the \"%\" shortcut will be disabled.".toCharArray(), "If you reassign the shortcut to \"La unchmedia player\", the \"Activate thewindow menu\" shortcut will be disabled.".toCharArray());
        expected = "^La unchmedia player^Activate thewindow menu";
        assertEquals(expected, charTreeToString(lastElem(p.Item2)));
        p = LCSMatrixTemplated("If you reassign the shortcut to \"%\", the \"%\" shortcut will be disabled.".toCharArray(), "If you reassign the shortcut to \"La unchmedia player the \"Activate thewindow menu\" shortcut will be disabled.".toCharArray());
        expected = "^La unchmedia player^Activate thewindow menu";
        assertEquals(expected, charTreeToString(lastElem(p.Item2)));
        p = LCSMatrixTemplated("If you reassign the shortcut to \"%\", the \"%\" shortcut will be disabled.".toCharArray(), "Ifyou reassign theshortcut to \"La unchmedia player the \"Activate thewindow menu\" shortcutwill be disabled".toCharArray());
        expected = "^La unchmedia player^Activate thewindow menu";
        assertEquals(expected, charTreeToString(lastElem(p.Item2)));
    }
    
    @Test
    public void GetSubstitutedStringsTest2() {
        Pair<int[][], CharTree[][]> p = LCSMatrixTemplated("thebar".toCharArray(), "thebar".toCharArray());
        String[] expected = new String[] { };
        assertArrayEquals(expected, getSubstitutedStrings(p.Item2));
        p = LCSMatrixTemplated("the%bar".toCharArray(), "thebar".toCharArray());
        //Pair<int[][], CharTree[][]> p = LCSMatrixTemplated("the%bar".toCharArray(), "theqasurbar".toCharArray());
        //assertEquals("qasur", charTreeToString(lastElem(p.Item2)));
        expected = new String[] { "" };
        assertArrayEquals(expected, getSubstitutedStrings(p.Item2));
        p = LCSMatrixTemplated("the%bar".toCharArray(), "theqasurbar".toCharArray());
        expected = new String[] { "qasur" };
        assertArrayEquals(expected, getSubstitutedStrings(p.Item2));
    }
    
    @Test
    public void GetSubstitutedStringsTest3() {
        Pair<int[][], CharTree[][]> p = LCSMatrixTemplated("If you reassign the shortcut to \"%\", the \"%\" shortcut will be disabled.".toCharArray(), "If you reassign the shortcut to \"Launch media player\", the \"Activate the window menu\" shortcut will be disabled.".toCharArray());
        String[] expected = new String[] { "Launch media player", "Activate the window menu" };
        assertArrayEquals(expected, getSubstitutedStrings(p.Item2));
    }
    
    @Test
    public void testSubsituteIntoTemplate() {
        String templatedString = "foo%bar%ke";
        String[] substitutions = new String[] {"salvod", "ypq"};
        assertEquals("foosalvodbarypqke", substituteIntoTemplate(templatedString, substitutions));
    }
    
    @Test
    public void testSubsituteIntoTemplateMarked() {
        String templatedString = "foo%bar%ke";
        String[] substitutions = new String[] {"salvod", "ypq"};
        assertEquals("foo#{salvod}bar#{ypq}ke", substituteIntoTemplateMarked(templatedString, substitutions));
    }
}
