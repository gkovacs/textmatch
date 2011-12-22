package textmatch;

import java.awt.Rectangle;
import java.util.*;

import org.sikuli.script.Region;

import static textmatch.GStringUtils.*;
import static textmatch.LCS.*;

public class MsgAnnotation {

    
    public final String filename;
    public final int x;
    public final int y;
    public final int w;
    public final int h;
    public final String[] templateSubstitutions;
    public final String matchedText;

    public MsgAnnotation(final String filename, int x, int y, int w, int h, final String[] templateSubstitutions, final String matchedText) {
        this.filename = filename;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.templateSubstitutions = templateSubstitutions;
        this.matchedText = matchedText;
    }
    
    public MsgAnnotation(final String filename, final Region r, final String[] templateSubstitutions, String matchedText) {
        this(filename, r.x, r.y, r.w, r.h, templateSubstitutions, matchedText);
    }
    
    public MsgAnnotation(final String filename, final Rectangle r, final String[] templateSubstitutions, String matchedText) {
        this(filename, r.x, r.y, r.width, r.height, templateSubstitutions, matchedText);
    }
    
    public MsgAnnotation(String annotationText) {
        // filename(x,y,w,h)~~~templateSubstitutions
        String[] annotationParts = annotationText.split("~~~");
        String fnCoords = annotationParts[0];
        filename = fnCoords.substring(0, fnCoords.indexOf('('));
        String coords = fnCoords.substring(fnCoords.indexOf('('));
        coords = stripPrefix(coords, "(");
        coords = stripSuffix(coords, ")");
        String[] coordl = coords.split(",");
        x = Integer.parseInt(coordl[0]);
        y = Integer.parseInt(coordl[1]);
        w = Integer.parseInt(coordl[2]);
        h = Integer.parseInt(coordl[3]);
        if (annotationParts.length < 2) {
            templateSubstitutions = new String[0];
        } else {
            templateSubstitutions = getSubstitutedStrings(annotationParts[1]);
        } if (annotationParts.length < 3) {
        	matchedText = "";
        } else {
        	matchedText = annotationParts[2];
        }
    }
    
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(filename);
        b.append("(" + x + "," + y + "," + w + "," + h + ")");
        b.append("~~~");
        String templateSubstitutionsText = "";
        if (templateSubstitutions.length > 0)
            templateSubstitutionsText = SPLITCHAR + join(templateSubstitutions, SPLITCHAR);
        b.append(templateSubstitutionsText);
        b.append("~~~");
        b.append(matchedText);
        return b.toString();
    }
}
