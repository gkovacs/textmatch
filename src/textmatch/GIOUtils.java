package textmatch;
import java.io.*;
import java.util.*;


public class GIOUtils {
    public static List<String> readLines(Reader s) throws Exception {
	List<String> lines = new ArrayList<String>();
	String line = null;
	BufferedReader reader = new BufferedReader(s);
        while ((line = reader.readLine()) != null) {
            lines.add(line.trim());
        }
        return lines;
    }
}
