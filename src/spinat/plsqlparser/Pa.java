package spinat.plsqlparser;

public abstract class Pa<X> {
    
    // the parser object

   
    // if the value in Res is an object which is of type
    // Ranged then stuff the range infromation into it
    
    public static <X> Res<X> extendRes(Res<X> r,Seq start) {
        if (r!=null&& (r.v instanceof Ranged)) {
            Ranged ra = (Ranged) r.v;
            if (ra.getStart()<0) {
                ra.setRange(start.head().pos,r.next.head().ipos);
            }
        }
        return r;
    }
    
    // implement this method to parse
    // if it is not possible to parse 
    // return null
    // if parsing is successfull, then return a Res with the result of the parse and 
    // the rest of the sequence, i.e. where should parsing continue
    // you may throw an exception, if you detremine this parser must succeed 
    // but it is not possible.
    protected abstract Res<X> par(Seq s) ;
    
    
    // call this method to parse, if possible the parsed object
    // is extended with range information, i.e. where it is in the source
    public final Res<X> pa(Seq s) {
        return extendRes(par(s),s);
    } 
    
}
