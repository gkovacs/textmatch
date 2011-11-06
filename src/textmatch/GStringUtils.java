package textmatch;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.sikuli.script.Region;

import static textmatch.GCollectionUtils.*;

public class GStringUtils {
    public static <T> String join(Iterable<T> list, String delim) {
        StringBuilder builder = new StringBuilder();
        for (T elem : list) {
            builder.append(elem);
            builder.append(delim);
        }
        if (builder.length() <= delim.length())
            return "";
        return builder.substring(0, builder.length() - delim.length());
    }
    
    public static <T> String join(T[] list, String delim) {
        StringBuilder builder = new StringBuilder();
        for (T elem : list) {
            builder.append(elem);
            builder.append(delim);
        }
        if (builder.length() <= delim.length())
            return "";
        return builder.substring(0, builder.length() - delim.length());
    }
    
    public static String stripPrefix(String s, String prefix) {
        if (s.startsWith(prefix)) {
            return s.substring(prefix.length());
        }
        return s;
    }
    
    public static String stripSuffix(String s, String suffix) {
        if (s.endsWith(suffix)) {
            return s.substring(0, s.length() - suffix.length());
        }
        return s;
    }
    
    public static String[] split(String str, char delim) {
        final char[] s = str.toCharArray();
        final int numDelims = count(s, delim);
        final String[] output = new String[numDelims + 1];
        setAll(output, "");
        int curidx = 0;
        final StringBuilder b = new StringBuilder();
        for (char c : s) {
            if (c == delim) {
                output[curidx] = b.toString();
                b.setLength(0);
                ++curidx;
            } else {
                b.append(c);
            }
        }
        if (b.length() > 0)
            output[curidx] = b.toString();
        return output;
    }
    
    
    public static <T extends Region> String tostr(T r) {
        return r.x + "," + r.y + "," + r.w + "," + r.h;
    }
    
}
