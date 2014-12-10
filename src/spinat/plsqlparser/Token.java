package spinat.plsqlparser;

public class Token {

    final int col; // the column in the source code 
    final int line; // the line in the source code 
    final int ipos; // the position where the ignored tokens before this token start 
    final int pos; // the absolute position in the string 
    final String str; // the raw string 
    final TokenType ttype; // : tokentype (* the token type, i.e. classification *)

    public Token(TokenType ttype,
            String str,
            int pos,
            int ipos,
            int line,
            int col) {
        this.ttype = ttype;
        this.str = str;
        this.pos = pos;
        this.ipos = ipos;
        this.line = line;
        this.col = col;
    }

    @Override
    public String toString() {
        return "<" + ttype + "/" + pos + "/" + ipos + "/" + line + "/" + col + ": " + str + ">";
    }
}
