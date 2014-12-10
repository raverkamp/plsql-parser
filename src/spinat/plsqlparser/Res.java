package spinat.plsqlparser;

// the result of parsing

public class Res<X> {
    public final X v;  // the value thta was parsed, an element of the AST
    public final Seq next; // the input which is left

    public Res (X v,Seq next) {
        this.v = v;
        this.next = next;
    }
}
