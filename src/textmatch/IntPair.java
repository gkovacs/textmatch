package textmatch;

public class IntPair {
    public final int Item1;
    public final int Item2;
    
    public IntPair(int Item1, int Item2) {
        this.Item1 = Item1;
        this.Item2 = Item2;
    }
    
    @Override
    public boolean equals(Object o) {
      if (o instanceof IntPair) { 
        IntPair p1 = (IntPair) o;
        if (p1.Item1 == this.Item1 && p1.Item2 == this.Item2) { 
          return true;
        }
      }
      return false;
    }
}
