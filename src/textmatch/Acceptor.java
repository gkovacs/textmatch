package textmatch;

public interface Acceptor<T> {
    boolean isAccepted(T x);
}
