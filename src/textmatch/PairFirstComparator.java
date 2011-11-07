package textmatch;

import java.util.*;

public class PairFirstComparator<T> implements Comparator<Pair<T,?>> {

    private final Comparator<T> c;
    public PairFirstComparator(Comparator<T> c) {
        this.c = c;
    }
    
    @Override
    public int compare(Pair<T, ?> o1, Pair<T, ?> o2) {
        return c.compare(o1.Item1, o2.Item1);
    }

}
