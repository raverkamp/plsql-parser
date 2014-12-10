package spinat.plsqlparser;

public class T4<A,B,C,D> {
    public final A f1;
    public final B f2;
    public final C f3;
    public final D f4;
    
    
    public T4(A f1,B f2,C f3,D f4) {
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.f4 = f4;
    }

     @Override
    public String toString(){
        return "<" + this.f1 +", " + this.f2 +", " + this.f3 +">";
    }
}
