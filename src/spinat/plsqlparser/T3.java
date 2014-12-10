package spinat.plsqlparser;

public class T3<X,Y,Z> {
    public final X f1;
    public final Y f2;
    public final Z f3;
    
    public T3(X f1,Y f2,Z f3) {
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
    }

     @Override
    public String toString(){
        return "<" + this.f1 +", " + this.f2 +", " + this.f3 +">";
    }
}
