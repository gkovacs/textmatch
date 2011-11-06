package textmatch;

import static textmatch.GCollectionUtils.slice;

import java.util.*;

public class GCollectionUtils {
    public static <T> List<T> singleElemList(T elem) {
        List<T> output = new ArrayList<T>();
        output.add(elem);
        return output;
    }
    
    public static <T, U> Pair<T, U> makePair(T x, U y) {
        return new Pair<T, U>(x, y);
    }
    
    public static int lastElem(int[] u) {
        return u[u.length - 1];
    }
    
    public static int lastElem(int[][] u) {
        return lastElem(u[u.length - 1]);
    }
    
    public static <T> T lastElem(T[] u) {
        return u[u.length - 1];
    }
    
    public static <T> T lastElem(T[][] u) {
        return lastElem(u[u.length - 1]);
    }
    
    public static int count(int[] x, int elem) {
        int count = 0;
        for (int i : x) {
            if (i == elem)
                ++count;
        }
        return count;
    }
    
    public static int count(char[] x, char elem) {
        int count = 0;
        for (char i : x) {
            if (i == elem)
                ++count;
        }
        return count;
    }
    
    public static int count(String x, char elem) {
        int count = 0;
        for (int i = 0; i < x.length(); ++i) {
            if (x.charAt(i) == elem)
                ++count;
        }
        return count;
    }
    
    public static <T> int count(T[] x, T elem) {
        int count = 0;
        for (T i : x) {
            if (i.equals(x))
                ++count;
        }
        return count;
    }
    
    public static <T> void setAll(T[] x, T value) {
        for (int i = 0; i < x.length; ++i) {
            x[i] = value;
        }
    }
    
    public static <T> List<T> slice(T[] x, int start, int end) {
        List<T> output = new ArrayList<T>();
        for (int i = start; i < end; ++i) {
            output.add(x[i]);
        }
        return output;
    }
    
    public static <T> List<T> slice(List<T> x, int start, int end) {
        List<T> output = new ArrayList<T>();
        for (int i = start; i < end; ++i) {
            output.add(x.get(i));
        }
        return output;
    }
    
    public static <T> List<T> toList(Iterable<T> l) {
        List<T> output = new ArrayList<T>();
        for (T x : l) {
            output.add(x);
        }
        return output;
    }
    
    public static <T> List<T> toList(T[] l) {
        List<T> output = new ArrayList<T>();
        for (T x : l) {
            output.add(x);
        }
        return output;
    }
    
    public static <T> Iterable<List<T>> substrings(final List<T> words) {
        return new Iterable<List<T>>() {

            @Override
            public Iterator<List<T>> iterator() {
                return new Iterator<List<T>>() {

                    private int i = 0;
                    // i: the ending entry
                    private int j = 0;
                    // j: the starting entry
                    
                    @Override
                    public boolean hasNext() {
                        return (i <= words.size());
                    }

                    @Override
                    public List<T> next() {
                        if (i > words.size())
                            throw new NoSuchElementException();
                        List<T> retv = slice(words, j, i);
                        ++j;
                        if (j >= i) {
                            j = 0;
                            ++i;
                        }
                        return retv;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                    
                };
            }
            
        };
    }
}
