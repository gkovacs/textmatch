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
        assertEquals(1.0, LCSTemplatedScore("the換bar換thien".toCharArray(), "theamlakbarasgnlsakdnglathien".toCharArray()), 0.001);
        //assertEquals(0.0, LCSTemplatedScore("the換bar換thien".toCharArray(), "qssy".toCharArray()), 0.001);
        //assertEquals(0.5454545, LCSTemplatedScore("the換bar換thien".toCharArray(), "thebar".toCharArray()), 0.001);
        //assertEquals(0.5454545, LCSTemplatedScore("the換bar換thien".toCharArray(), "theqssdgknetknrbar".toCharArray()), 0.001);
    }
    
    @Test
    public void GetSubstitutedStringsTest1() {
        Pair<double[][], CharTree[][]> p = LCSMatrixTemplated("thebar".toCharArray(), "thebar".toCharArray());
        String expected = "";
        assertEquals(expected, charTreeToString(lastElem(p.Item2)));
        p = LCSMatrixTemplated("the換bar".toCharArray(), "thebar".toCharArray());
        expected = "分";
        assertEquals(expected, charTreeToString(lastElem(p.Item2)));
        p = LCSMatrixTemplated("the換bar".toCharArray(), "theqasurbar".toCharArray());
        expected = "分qasur";
        assertEquals(expected, charTreeToString(lastElem(p.Item2)));
        p = LCSMatrixTemplated("the換bar換".toCharArray(), "theqasurbar".toCharArray());
        expected = "分qasur分";
        assertEquals(expected, charTreeToString(lastElem(p.Item2)));
        p = LCSMatrixTemplated("the換bar換thien".toCharArray(), "theqasurbarthien".toCharArray());
        expected = "分qasur分";
        assertEquals(expected, charTreeToString(lastElem(p.Item2)));
        p = LCSMatrixTemplated("the換bar換thien".toCharArray(), "theqasurbarORKEthien".toCharArray());
        expected = "分qasur分ORKE";
        assertEquals(expected, charTreeToString(lastElem(p.Item2)));
        p = LCSMatrixTemplated("If you reassign the shortcut to \"換\", the \"換\" shortcut will be disabled.".toCharArray(), "If you reassign the shortcut to \"Launch media player\", the \"Activate the window menu\" shortcut will be disabled.".toCharArray());
        expected = "分Launch media player分Activate the window menu";
        assertEquals(expected, charTreeToString(lastElem(p.Item2)));
        p = LCSMatrixTemplated("If you reassign the shortcut to \"換\", the \"換\" shortcut will be disabled.".toCharArray(), "If you reassign the shortcut to \"La unchmedia player\", the \"Activate the window menu\" shortcut will be disabled.".toCharArray());
        expected = "分La unchmedia player分Activate the window menu";
        assertEquals(expected, charTreeToString(lastElem(p.Item2)));
        p = LCSMatrixTemplated("If you reassign the shortcut to \"換\", the \"換\" shortcut will be disabled.".toCharArray(), "If you reassign the shortcut to \"La unchmedia player\", the \"Activate thewindow menu\" shortcut will be disabled.".toCharArray());
        expected = "分La unchmedia player分Activate thewindow menu";
        assertEquals(expected, charTreeToString(lastElem(p.Item2)));
        p = LCSMatrixTemplated("If you reassign the shortcut to \"換\", the \"換\" shortcut will be disabled.".toCharArray(), "If you reassign the shortcut to \"La unchmedia player the \"Activate thewindow menu\" shortcut will be disabled.".toCharArray());
        expected = "分La unchmedia player分Activate thewindow menu";
        assertEquals(expected, charTreeToString(lastElem(p.Item2)));
        p = LCSMatrixTemplated("If you reassign the shortcut to \"換\", the \"換\" shortcut will be disabled.".toCharArray(), "Ifyou reassign theshortcut to \"La unchmedia player the \"Activate thewindow menu\" shortcutwill be disabled".toCharArray());
        expected = "分La unchmedia player分Activate thewindow menu";
        assertEquals(expected, charTreeToString(lastElem(p.Item2)));
    }
    
    @Test
    public void GetSubstitutedStringsTest2() {
        Pair<double[][], CharTree[][]> p = LCSMatrixTemplated("thebar".toCharArray(), "thebar".toCharArray());
        String[] expected = new String[] { };
        assertArrayEquals(expected, getSubstitutedStrings(p.Item2));
        p = LCSMatrixTemplated("the換bar".toCharArray(), "thebar".toCharArray());
        //Pair<int[][], CharTree[][]> p = LCSMatrixTemplated("the換bar".toCharArray(), "theqasurbar".toCharArray());
        //assertEquals("qasur", charTreeToString(lastElem(p.Item2)));
        expected = new String[] { "" };
        assertArrayEquals(expected, getSubstitutedStrings(p.Item2));
        p = LCSMatrixTemplated("the換bar".toCharArray(), "theqasurbar".toCharArray());
        expected = new String[] { "qasur" };
        assertArrayEquals(expected, getSubstitutedStrings(p.Item2));
    }
    
    @Test
    public void GetSubstitutedStringsTest3() {
        Pair<double[][], CharTree[][]> p = LCSMatrixTemplated("If you reassign the shortcut to \"換\", the \"換\" shortcut will be disabled.".toCharArray(), "If you reassign the shortcut to \"Launch media player\", the \"Activate the window menu\" shortcut will be disabled.".toCharArray());
        String[] expected = new String[] { "Launch media player", "Activate the window menu" };
        assertArrayEquals(expected, getSubstitutedStrings(p.Item2));
    }
    
    @Test
    public void testSubsituteIntoTemplate() {
        String templatedString = "foo換bar換ke";
        String[] substitutions = new String[] {"salvod", "ypq"};
        assertEquals("foosalvodbarypqke", substituteIntoTemplate(templatedString, substitutions));
    }
    
    @Test
    public void testSubsituteIntoTemplateMarked() {
        String templatedString = "foo換bar換ke";
        String[] substitutions = new String[] {"salvod", "ypq"};
        assertEquals("foo#{salvod}bar#{ypq}ke", substituteIntoTemplateMarked(templatedString, substitutions));
    }
}
