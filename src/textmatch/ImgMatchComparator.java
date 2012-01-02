package textmatch;

import java.util.Comparator;

public class ImgMatchComparator implements Comparator<ImgMatch> {

    @Override
    public int compare(ImgMatch o1, ImgMatch o2) {
        if (Math.abs(o1.y - o2.y) > 2)
            return o1.y - o2.y;
        if (o1.x != o2.x)
            return o1.x - o2.x;
        return 0;
    }

}
