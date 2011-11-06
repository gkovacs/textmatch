package textmatch;

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

    public MsgAnnotation(final String filename, int x, int y, int w, int h, final String[] templateSubstitutions) {
        this.filename = filename;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.templateSubstitutions = templateSubstitutions;
    }
    
    public MsgAnnotation(final String filename, final Region r, final String[] templateSubstitutions) {
        this(filename, r.x, r.y, r.w, r.h, templateSubstitutions);
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
            templateSubstitutionsText = "^" + join(templateSubstitutions, "^");
        b.append(templateSubstitutionsText);
        return b.toString();
    }
}
