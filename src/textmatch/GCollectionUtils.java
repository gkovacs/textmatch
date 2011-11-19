package textmatch;

import static textmatch.GCollectionUtils.slice;

import java.util.*;

import org.sikuli.script.Region;

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
    
    public static double lastElem(double[] u) {
        return u[u.length - 1];
    }
    
    public static double lastElem(double[][] u) {
        return lastElem(u[u.length - 1]);
    }
    
    public static <T> T lastElem(T[] u) {
        return u[u.length - 1];
    }
    
    public static <T> T lastElem(T[][] u) {
        return lastElem(u[u.length - 1]);
    }
    
    public static <T> T lastElem(List<T> u) {
        return u.get(u.size() - 1);
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
    
    public static <T> void incrementMap(HashMap<T, Integer> map, T elem) {
        if (!map.containsKey(elem))
            map.put(elem, 1);
        else
            map.put(elem, map.get(elem) + 1);
    }
    
    public static boolean arraysEqual(int[] a1, int[] a2) {
        if (a1.length != a2.length)
            return false;
        for (int i = 0; i < a1.length; ++i) {
            if (a1[i] != a2[i])
                return false;
        }
        return true;
    }
    
    public static Region spannedRegion(List<? extends Region> regions) {
        int minx = Integer.MAX_VALUE;
        int maxx = Integer.MIN_VALUE;
        int miny = Integer.MAX_VALUE;
        int maxy = Integer.MIN_VALUE;
        for (Region r : regions) {
            if (r.x < minx)
                minx = r.x;
            if (r.y < miny)
                miny = r.y;
            if (r.x + r.w > maxx)
                maxx = r.x + r.w;
            if (r.y + r.h > maxy)
                maxy = r.y + r.h;
        }
        int w = maxx - minx;
        int h = maxy - miny;
        return new Region(minx, miny, w, h);
    }
    
    public static int spanningArea(List<? extends Region> regions) {
        int minx = Integer.MAX_VALUE;
        int maxx = Integer.MIN_VALUE;
        int miny = Integer.MAX_VALUE;
        int maxy = Integer.MIN_VALUE;
        for (Region r : regions) {
            if (r.x < minx)
                minx = r.x;
            if (r.y < miny)
                miny = r.y;
            if (r.x + r.w > maxx)
                maxx = r.x + r.w;
            if (r.y + r.h > maxy)
                maxy = r.y + r.h;
        }
        int w = maxx - minx;
        int h = maxy - miny;
        return w * h;
    }
    
    public static int totalArea(List<? extends Region> regions) {
        int total = 0;
        for (Region x : regions) {
            total += x.w * x.h;
        }
        return total;
    }
    
    public static <T> Iterable<List<T>> substrings(final List<T> words) {
        return substrings(words, new Acceptor<List<T>>() {

            @Override
            public boolean isAccepted(List<T> x) {
                return true;
            }
            
        });
        /*
        final int wordsSize = words.size();
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
                        return (j < wordsSize);
                    }

                    @Override
                    public List<T> next() {
                        if (j >= wordsSize)
                            throw new NoSuchElementException();
                        List<T> retv = slice(words, j, i);
                        ++i;
                        if (i > wordsSize) {
                            ++j;
                            i = j + 1;
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
        */
    }
    
    public static <T> Iterable<List<T>> substrings(final List<T> words, final Acceptor<List<T>> acceptor) {
        // if acceptor rejects something, all subsequent substrings at that start point are also rejected
        final int wordsSize = words.size();
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
                        return (j < wordsSize);
                    }

                    @Override
                    public List<T> next() {
                        if (j >= wordsSize)
                            throw new NoSuchElementException();
                        List<T> retv = slice(words, j, i);
                        ++i;
                        while (j < wordsSize && (i > wordsSize || !acceptor.isAccepted(slice(words, j, i)))) {
                            ++j;
                            i = j + 1;
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
    /*
    public static <T> Iterable<List<T>> substrings(final List<T> words, final int minlength, final int maxlength) {
        return new Iterable<List<T>>() {

            @Override
            public Iterator<List<T>> iterator() {
                return new Iterator<List<T>>() {

                    private int i = minlength;
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
                        if (i < j + minlength) {
                            // length = i-j
                            // so i-j >= minlength => i >= j + minlength
                            // and i-j <= maxlength => i <= j + maxlength
                            ++i;
                            j = Math.max(i - maxlength, 0);
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
    */
}
