package textmatch;

public class Pair<T, U> {
    public final T Item1;
    public final U Item2;
    
    public Pair(T Item1, U Item2) {
        this.Item1 = Item1;
        this.Item2 = Item2;
    }
    
    @Override
    public boolean equals(Object o) {
      if (o instanceof Pair) { 
        Pair<?, ?> p1 = (Pair<?, ?>) o;
        if (p1.Item1.equals(this.Item1) && p1.Item2.equals(this.Item2)) { 
          return true;
        }
      }
      return false;
    }
}
