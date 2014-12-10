package spinat.plsqlparser;

import java.util.ArrayList;
import java.util.List;


public final class Seq {

    
    private final ArrayList<Token> l;
    private final int start;
    private final int end;
    
    public Seq(List<Token> l) {
        this.l = new ArrayList<>();
        this.l.addAll(l);
        start = 0;
        end = this.l.size();
    }
    
    public Token head() {
       if (this.start < this.end) {
           return this.l.get(start);
       } else {
           throw new RuntimeException("past end of seq");
       }
    }
    
    Seq (Seq s,int start,int end) {
        this.l = s.l;
        this.start = start;
        this.end = end;
    }
    
    public Seq tail() {
        if (this.start < this.end) {
            return new Seq(this,this.start+1,end);
        } else {
            throw new RuntimeException("passt end of seq");
        }
    }
    
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(String.format("Seq %d/%d:",this.head().line,this.head().col));
        for(int i=0;i<10;i++){
            if (i+this.start>= this.end){
                return b.toString();
            }
            b.append(" ");
            b.append(this.l.get(i+this.start).str);
        }
        return b.toString();   
    }
}
