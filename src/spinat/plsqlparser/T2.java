package spinat.plsqlparser;


public class T2<X,Y> {
    public final X f1;
    public final Y f2;
    
    public T2(X f1,Y f2) {
        this.f1 = f1;
        this.f2 = f2;
    }
    
    @Override
    public String toString(){
        return "<" + this.f1 +", " + this.f2 +">";
    }
}
