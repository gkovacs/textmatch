package textmatch;

import java.util.Comparator;

public class MsgAnnotationRegionComparator implements Comparator<MsgAnnotation> {
    @Override
    public int compare(MsgAnnotation o1, MsgAnnotation o2) {
        if (!o1.filename.equals(o2.filename))
            return o1.filename.compareTo(o2.filename);
        if (o1.y != o2.y)
            return o1.y - o2.y;
        return o1.x - o2.x;
    }
}

