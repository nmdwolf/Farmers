public class Pair {

    public final int X,Y;

    public Pair(int x, int y) {
        X = x;
        Y = y;
    }

    @Override
    public int hashCode() {
        return X*10000 + Y;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Pair) {
            Pair p = (Pair)obj;
            return (X == p.X) && (Y == p.Y);
        }
        return false;
    }
}
