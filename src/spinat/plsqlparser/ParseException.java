package spinat.plsqlparser;


// this execption is thrown if parsing fails
// the exception contains the position where parsing failed
public class ParseException extends RuntimeException {

    private static String positionString(Seq s) {
        return s.head().line +"/" + s.head().col;
    }
    
    public final Seq position;
    
    public ParseException(String s,Seq position) {
        super(s + " at " + positionString(position));
        this.position = position;
    }
    
}
