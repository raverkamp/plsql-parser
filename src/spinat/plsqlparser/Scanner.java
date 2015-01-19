package spinat.plsqlparser;

import java.util.ArrayList;

public class Scanner {

    final String source;
    final int len;

    // our state
    int start = 0;
    int istart = 0;
    int line = 1;
    int col = 0;

    public Scanner(String source) {
        this.source = source;
        this.len = source.length();
    }

    char get(int x) {
        if (x > len) {
            throw new RuntimeException("unexpcted end of source");
        } else {
            return source.charAt(x);
        }
    }

    int scanString(int pos) {
        if (get(pos) != '\'') {
            throw new RuntimeException("BUG");
        }

        int x = pos + 1;
        while (true) {
            if (x >= len) {
                throw new ScanException("Unexpected end of string");
            }
            char c = get(x);
            if (c == '\'') {
                if ((x + 1) < len && get(x + 1) == '\'') {
                    x = x + 2;
                } else {
                    return x + 1;
                }
            } else {
                x++;
            }
        }
    }

    int scanQIdent(int pos) {
        if (get(pos) != '"') {
            throw new RuntimeException("BUG");
        }
        int x = pos + 1;
        while (true) {
            if (x >= len) {
                throw new ScanException("unexpected end of quoted identifier");
            }
            if (get(x) == '"') {
                return x + 1;
            } else {
                x++;
            }
        }
    }

    int scanMLComment(int pos) {
        if (!(get(pos) == '/' && get(pos + 1) == '*')) {
            throw new RuntimeException("BUG");
        }
        int x = pos + 2;
        while (true) {
            if (!(x < len - 1)) {
                throw new ScanException("unexpected end of comment");
            }
            if (get(x) == '*' && get(x + 1) == '/') {
                return x + 2;
            } else {
                x++;
            }
        }
    }

    int scanEoLComment(int pos) {
        if (!((pos < len - 1) && get(pos) == '-' && get(pos + 1) == '-')) {
            throw new RuntimeException("BUG");
        }
        int x = pos + 2;
        while (true) {
            if (x < len) {
                if (get(x) == '\n') {
                    return x + 1;
                } else {
                    x++;
                }
            } else {
                return x;
            }
        }
    }

    boolean isIdentMember(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                || c == '#' || c == '$' || c == '_';
    }

    int scanIdent(int pos) {
        int x = pos;
        while (x < len && isIdentMember(get(x))) {
            x++;
        }
        return x;
    }

    int ScanInt(int pos) {
        int x = pos;
        while (x < len && get(x) >= '0' && get(x) <= '9') {
            x++;
        }
        return x;
    }

    int ScabWS(int pos) {
        int x = pos;
        while (x < len && Character.isWhitespace(get(x))) {
            x++;
        }
        return x;
    }

    void advPos(int[] lc, int from, int to) {
        int x = from;
        while (true) {
            if (x >= to) {
                return;
            }
            if (get(x) == '\n') {
                lc[0]++;
                lc[1] = 0;
            } else {
                lc[1]++;
            }
            x++;
        }
    }

    int qString(int pos) {
        // pos = q 
        // pos +1 = '
        // pos +2 = ende
        final char ende;
        switch (get(pos + 2)) {
            case '[':
                ende = ']';
                break;
            case '(':
                ende = ')';
                break;
            case '{':
                ende = '}';
                break;
            case '<':
                ende = '>';
                break;
            case '\'':
                throw new ScanException("' an not be ende of q string");
            default:
                ende = get(pos + 2);
        }
        int x = pos + 3;
        while (true) {
            if (x + 1 < len) {
                if ((get(x) == ende && get(x + 1) == '\'')) {
                    return x + 2;
                } else {
                    x++;
                }
            }
        }
    }

    int scanDollarDollarIdent(int pos) {
        int p = scanIdent(pos + 2);
        return p;
    }

    static boolean isArabicDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // the e has been found
    int scanExpo(int pos) {
        int x;
        if (pos + 1 < len && (get(pos + 1) == '-' || get(pos + 1) == '+')) {
            x = pos + 2;
        } else {
            x = pos + 1;
        }
        if (!isArabicDigit(get(x))) {
            throw new ScanException("can not parse number");
        }
        while (x < len && isArabicDigit(get(x))) {
            x++;
        }
        return x;
    }

    int scanFloat(int pos) {
        if (get(pos) != '.') {
            throw new RuntimeException("BUG");
        }
        int p = ScanInt(pos + 1);
        if (p < len && (get(p) == 'e' || get(p) == 'E')) {
            return scanExpo(p);
        } else {
            return p;
        }
    }

    Token scanNumber(int pos) {
        int x = ScanInt(pos);
        // the problem : for i in 1..10 loop 
        // we only have to check for the second "." *)
        if (x >= len || (get(x) == '.' && x + 1 < len && get(x + 1) == '.')) {
            return tokx(TokenType.Int, x);
        } else {
            final int p;
            if (get(x) == '.') {
                p = ScanInt(x + 1);
            } else {
                p = x;
            }
            if (p < len && (get(p) == 'E' || get(p) == 'e')) {
                return tokx(TokenType.Float, scanExpo(p));
            } else {
                if (p == x) {/* no . */

                    return tokx(TokenType.Int, x);
                } else {
                    return tokx(TokenType.Float, p);
                }

            }
        }
    }

    Token tokx(TokenType tt, int next) {
        Token res = new Token(tt, this.source.substring(start, next), this.start, this.istart, this.line, this.col);

        int[] f = new int[]{this.line, this.col};
        advPos(f, this.start, next);
        this.line = f[0];
        this.col = f[1];
        this.start = next;
        return res;
    }

    Token tok2(TokenType what) {
        return tokx(what, this.start + 2);
    }

    Token tok1(TokenType what) {
        return tokx(what, this.start + 1);
    }


    /*fun tokx what next =
     let val (linen,coln)=advPos(line,col,start,next)
     in
     ({ttype= what, pos=start,ipos=istart, 
     str= substring(str,start,next-start),
     line=line,col=col},next,linen,coln)
     end
 
     */
    Token check1() {
        char c = get(start);
        switch (c) {
            case '\'':
                return tokx(TokenType.String, scanString(start));
            case '(':
                return tok1(TokenType.LParen);
            case ')':
                return tok1(TokenType.RParen);
            case '[':
                return tok1(TokenType.LBracket);
            case ']':
                return tok1(TokenType.RBracket);
            case '>':
                return tok1(TokenType.Greater);
            case '<':
                return tok1(TokenType.Less);
            case '-':
                return tok1(TokenType.Minus);
            case '.':
                return tok1(TokenType.Dot);
            case '+':
                return tok1(TokenType.Plus);
            case '/':
                return tok1(TokenType.Div);
            case '*':
                return tok1(TokenType.Mul);
            case ',':
                return tok1(TokenType.Comma);
            case ';':
                return tok1(TokenType.Semi);
            case '=':
                return tok1(TokenType.Equal);
            case '%':
                return tok1(TokenType.Percent);
            case ':':
                return tok1(TokenType.Colon);
            case '!':
                return tok1(TokenType.Exclamation);
            case '"':
                return tokx(TokenType.QIdent, scanQIdent(this.start));
            default:
                if (isArabicDigit(c)) {
                    return scanNumber(this.start);
                } else if (Character.isWhitespace(c)) {
                    return tokx(TokenType.WhiteSpace, ScabWS(this.start));
                } else if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
                    return tokx(TokenType.Ident, scanIdent(this.start));
                } else {
                    throw new ScanException("unhandled char: " + c + " at line" + this.line);
                }
        }
    }

    Token scan1(int istart) {
        this.istart = istart;
        if (this.start < len - 2) {
            String a = source.substring(this.start, this.start + 2);
            switch (a) {
                case "=>":
                    return tok2(TokenType.Arrow);
                case "<=":
                    return tok2(TokenType.LEqual);
                case ">=":
                    return tok2(TokenType.GEqual);
                case ":=":
                    return tok2(TokenType.Assign);
                case "<>":
                    return tok2(TokenType.NEqual);
                case "!=":
                    return tok2(TokenType.NEqual);
                case "||":
                    return tok2(TokenType.StringAdd);
                case "--":
                    return tokx(TokenType.EOLineComment, scanEoLComment(this.start));
                case "/*":
                    return tokx(TokenType.MultiLineComment, scanMLComment(this.start));
                case "**":
                    return tok2(TokenType.Power);
                case "..":
                    return tok2(TokenType.DotDot);
                case "<<":
                    return tok2(TokenType.LabelStart);
                case ">>":
                    return tok2(TokenType.LabelEnd);
                case "q'":
                    return tokx(TokenType.QString, qString(this.start));
                case "$$":
                    return tokx(TokenType.DollarDollarIdent, scanDollarDollarIdent(this.start));
                default:
                    if (get(start) == '.' && isArabicDigit(get(start + 1))) {
                        return tokx(TokenType.Float, scanFloat(this.start));
                    } else {
                        return check1();
                    }

            }
        } else {
            return check1();
        }
    }

    public static boolean isRelevant(Token t) {
        switch (t.ttype) {
            case EOLineComment:
                return false;
            case MultiLineComment:
                return false;
            case WhiteSpace:
                return false;
            default:
                return true;
        }
    }

    public static ArrayList<Token> scanAll(String s) {
        ArrayList<Token> res = new ArrayList<>();
        int len = s.length();
        Scanner sc = new Scanner(s);
        int istart = 0;
        while (true) {
            if (sc.start < len) {
                Token t = sc.scan1(istart);
                if (isRelevant(t)) {
                    res.add(t);
                    istart = sc.start;
                } else {
                    res.add(t);
                }
            } else {
                res.add(new Token(TokenType.TheEnd, "", sc.start, istart, sc.line, sc.col));
                return res;
            }
        }
    }

}
