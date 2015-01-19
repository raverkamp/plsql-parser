package spinat.plsqlparser;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import spinat.plsqlparser.Ast.Expression;
import spinat.plsqlparser.Ast.Statement;

public class Parser {

    static final List<String> badwords = Arrays.asList(new String[]{"insert", "update", "select", "declare", "loop", "end", "while",
        "begin", "null", "in", "out", "exception", "constant", "cursor",
        "pragma", "procedure", "function", "if", "for", "exception", "when", "elsif", "raise", "return", "else", "like",
        "case", "table", "cast"});

    static final Combinator c = new Combinator();

    void must(Res r, Seq s, String msg) {
        if (r == null) {
            throw new ParseException("can not parse: " + msg, s);
        }
    }

    Pa<String> pkw_or = c.forkw("or");
    Pa<String> pkw_and = c.forkw("and");
    Pa<String> pkw_not = c.forkw("not");
    Pa<String> pkw_like = c.forkw("like");
    Pa<String> pkw_between = c.forkw("between");
    Pa<String> pkw_type = c.forkw("type");
    Pa<String> pkw_subtype = c.forkw("subtype");
    Pa<String> pkw_procedure = c.forkw("procedure");
    Pa<String> pkw_function = c.forkw("function");
    Pa<String> pkw_exception = c.forkw("excpetion");
    Pa<String> pkw_is = c.forkw("is");
    Pa<String> pkw_index = c.forkw("index");
    Pa<String> pkw_by = c.forkw("by");
    Pa<String> pkw_not_null = c.forkw2("not", "null");
    Pa<String> pkw_varray = c.forkw("varray");
    Pa<String> pkw_varying_array = c.forkw2("varying", "array");
    Pa<String> pkw_of = c.forkw("of");
    Pa<String> pkw_return = c.forkw("return");
    Pa<String> pkw_pragma = c.forkw("pragma");
    Pa<String> pkw_language = c.forkw("language");
    Pa<String> pkw_begin = c.forkw("begin");
    Pa<String> pkw_end = c.forkw("end");
    Pa<String> pkw_declare = c.forkw("declare");
    Pa<String> pkw_when = c.forkw("when");
    Pa<String> pkw_others = c.forkw("others");
    Pa<String> pkw_then = c.forkw("then");
    Pa<String> pkw_if = c.forkw("if");
    Pa<String> pkw_else = c.forkw("else");
    Pa<String> pkw_elsif = c.forkw("elsif");
    Pa<String> pkw_loop = c.forkw("loop");
    Pa<String> pkw_end_loop = c.forkw2("end", "loop");
    Pa<String> pkw_while = c.forkw("while");
    Pa<String> pkw_for = c.forkw("for");
    Pa<String> pkw_select = c.forkw("select");
    Pa<String> pkw_reverse = c.forkw("reverse");
    Pa<String> pkw_case = c.forkw("case");
    Pa<String> pkw_end_case = c.forkw2("end", "case");
    Pa<String> pkw_raise = c.forkw("raise");
    Pa<String> pkw_open = c.forkw("open");
    Pa<String> pkw_using = c.forkw("using");
    Pa<String> pkw_close = c.forkw("close");
    Pa<String> pkw_end_if = c.forkw2("end", "if");
    Pa<String> pkw_fetch = c.forkw("fetch");
    Pa<String> pkw_into = c.forkw("into");
    Pa<String> pkw_exit = c.forkw("exit");
    Pa<String> pkw_continue = c.forkw("continue");
    Pa<String> pkw_pipe_row = c.forkw2("pipe", "row");
    Pa<String> pkw_execute_immediate = c.forkw2("execute", "immediate");
    Pa<String> pkw_bulk_collect = c.forkw2("bulk", "collect");
    Pa<String> pkw_returning = c.forkw("returning");
    Pa<String> pkw_package_body = c.forkw2("package", "body");
    Pa<String> pkw_cursor = c.forkw("cursor");
    Pa<String> pkw_forall = c.forkw("forall");
    Pa<String> pkw_goto = c.forkw("goto");
    Pa<String> pkw_restrict_references = c.forkw("restrict_references");
    Pa<String> pkw2_pragma_restrict_references = c.forkw2("pragma", "restrict_references");
    Pa<String> pkw_default = c.forkw("default");
    Pa<String> pkw_with = c.forkw("with");
    Pa<String> pkw_limit = c.forkw("limit");
    Pa<String> pkw_range = c.forkw("range");
    Pa<String> pkw_in = c.forkw("in");
    Pa<String> pkw_out = c.forkw("out");
    Pa<String> pkw_in_out = c.forkw2("in", "out");
    Pa<String> pkw_nocopy = c.forkw("nocopy");
    Pa<String> pkw_from = c.forkw("from");
    // do not care about the string, what operators are there else?
    Pa pkw_multiset_union_all = c.seq2(c.forkw2("multiset", "union"), c.forkw("all"));

    Pa<Integer> pNatural = new Pa<Integer>() {

        final Pa<String> p = c.token(TokenType.Int);

        @Override
        public Res<Integer> par(Seq s) {
            Res<String> r = p.pa(s);
            if (r != null) {
                return new Res<>(new Integer(r.v), r.next);
            } else {
                return null;
            }
        }

        @Override
        public String toString() {
            return "pNatural";
        }
    };

    Pa<Integer> pInteger = new Pa<Integer>() {
        final Pa<String> p = c.token(TokenType.Int);

        @Override
        public Res<Integer> par(Seq s) {
            Res rm = c.token(TokenType.Minus).pa(s);
            int sign = 1;
            if (rm != null) {
                s = rm.next;
                sign = -1;
            } else {
                Res rp = c.token(TokenType.Plus).pa(s);
                if (rp != null) {
                    s = rp.next;
                }
            }
            Res<String> r = p.pa(s);
            if (r != null) {
                return new Res<>(new Integer(r.v) * sign, r.next);
            } else {
                return null;
            }
        }

        @Override
        public String toString() {
            return "pNatural";
        }
    };

    final Pa<Ast.Ident> pIdent = new Pa<Ast.Ident>() {

        @Override
        public Res<Ast.Ident> par(Seq s) {
            Token t = s.head();
            if (t.ttype == TokenType.Ident) {
                if (badwords.contains(t.str.toLowerCase())) {
                    return null;
                } else {
                    return new Res<>(new Ast.Ident(t.str.toUpperCase()), s.tail());
                }
            } else if (t.ttype == TokenType.QIdent) {
                return new Res<>(new Ast.Ident(t.str.substring(1, t.str.length() - 1)), s.tail());
            } else {
                return null;
            }
        }

        @Override
        public String toString() {
            return "pIdent";
        }
    };

    final Pa<String> justkw = new Pa<String>() {

        @Override
        public Res<String> par(Seq s) {
            if (s.head().ttype == TokenType.Ident) {
                return new Res<>(s.head().str.toLowerCase(), s.tail());
            } else {
                return null;
            }
        }
    };

    final Pa<Ast.CmpOperator> pCmpOp = new Pa<Ast.CmpOperator>() {

        @Override
        public Res<Ast.CmpOperator> par(Seq s) {
            final Ast.CmpOperator o;
            switch (s.head().ttype) {
                case Less:
                    o = Ast.CmpOperator.LTH;
                    break;
                case LEqual:
                    o = Ast.CmpOperator.LEQ;
                    break;
                case Greater:
                    o = Ast.CmpOperator.GTH;
                    break;
                case GEqual:
                    o = Ast.CmpOperator.GEQ;
                    break;
                case Equal:
                    o = Ast.CmpOperator.EQ;
                    break;
                case NEqual:
                    o = Ast.CmpOperator.NOT_EQ;
                    break;
                default:
                    return null;
            }
            return new Res<>(o, s.tail());
        }
    };

    final Pa<BigInteger> pPlainInt = new Pa<BigInteger>() {

        @Override
        public Res<BigInteger> par(Seq s) {
            if (s.head().ttype == TokenType.Int) {
                return new Res<>(new BigInteger(s.head().str), s.tail());
            } else {
                return null;
            }
        }
    };

    Res<Ast.Expression> paExpr(Seq s) {
        return paOrExpr(s);
    }

    final Pa<Expression> pExpr = new Pa<Expression>() {

        @Override
        public Res<Expression> par(Seq s) {
            return paExpr(s);
        }

        @Override
        public String toString() {
            return "pExpr";
        }
    };

    public Res<Ast.Expression> paOrExpr(Seq s) {
        Res<List<Ast.Expression>> r = c.sep1(pAndExpr, pkw_or).pa(s);
        if (r == null) {
            return null;
        } else {
            return new Res<Ast.Expression>(new Ast.OrExpr(r.v), r.next);
        }
    }

    final Pa<Ast.Expression> pOrExpr = new Pa<Expression>() {
        @Override
        public Res<Expression> par(Seq s) {
            return paOrExpr(s);
        }
    };

    Res<Ast.Expression> paAndExpr(Seq s) {
        Res<List<Ast.Expression>> r = c.sep1(pNotExpression, pkw_and).pa(s);
        if (r == null) {
            return null;
        } else {
            return new Res<Ast.Expression>(new Ast.AndExpr(r.v), r.next);
        }
    }

    final Pa<Ast.Expression> pAndExpr = new Pa<Expression>() {
        @Override
        public Res<Expression> par(Seq s) {
            return paAndExpr(s);
        }
    };

    final Pa<Expression> pNotExpression = new Pa<Expression>() {

        @Override
        public Res<Expression> par(Seq s) {
            Res<String> r = c.opt(pkw_not).pa(s);
            Res<Expression> r2 = paCompExpr(r.next);
            if (r.v == null) {
                return r2;
            } else {
                return new Res<Ast.Expression>(new Ast.NotExpr(r2.v), r2.next);
            }
        }

    };

    final Res<Expression> paCompExpr(Seq s) {
        Res<Ast.Expression> r = paIsNullExpr(s);
        if (r == null) {
            return null;
        }
        Res<Ast.CmpOperator> r2 = pCmpOp.pa(r.next);
        if (r2 == null) {
            return r;
        } else {
            Res<Ast.Expression> r3 = paIsNullExpr(r2.next);
            return new Res<Expression>(new Ast.CompareExpr(r2.v, r.v, r3.v), r3.next);
        }
    }

    Res<Expression> paIsNullExpr(Seq s) {
        Res<Expression> r = paLikeExpr(s);
        if (r == null) {
            return null;
        }
        Res<String> r2 = pkw_is.pa(r.next);
        if (r2 == null) {
            return r;
        }
        Res<String> r3 = c.opt(pkw_not).pa(r2.next);
        Res<Expression> r4 = paLikeExpr(r3.next);
        must(r4, r3.next, "expression");
        return new Res<Expression>(new Ast.IsNullExpr(r4.v, r2.v != null), r4.next);

    }

    Res<Expression> paLikeExpr(Seq s) {
        Res<Expression> r = paBetweenExpr(s);
        if (r == null) {
            return null;
        }
        Res<Boolean> rnot = c.bopt(pkw_not).pa(r.next);
        Res<String> r2 = pkw_like.pa(rnot.next);
        if (r2 == null) {
            return r;
        }
        Res<Expression> r3 = paBetweenExpr(r2.next);
        must(r3, r2.next, "expression");
        Res<String> r4 = c.forkw("escape").pa(r3.next);
        if (r4 == null) {
            return new Res<Expression>(new Ast.LikeExpression(r.v, r3.v, null, rnot.v), r3.next);
        } else {
            Res<Expression> r5 = paBetweenExpr(r4.next);
            must(r5, r4.next, "expression");
            return new Res<Expression>(new Ast.LikeExpression(r.v, r3.v, r5.v, rnot.v), r5.next);
        }
    }

    Res<Expression> paBetweenExpr(Seq s) {
        Res<Expression> r = paInExpression(s);
        if (r == null) {
            return null;
        }
        Res<String> r2 = c.opt(pkw_not).pa(r.next);
        Res<String> r3 = pkw_between.pa(r2.next);
        if (r3 == null) {
            return r;
        }
        Res<Expression> re1 = paInExpression(r3.next);
        Res<String> rand = pkw_and.pa(re1.next);
        Res<Expression> re2 = paInExpression(rand.next);
        if (r2.v == null) {
            return new Res<Expression>(new Ast.BetweenExpression(r.v, re1.v, re2.v), re2.next);
        } else {
            // fixme:  x not between a and b versus not (x between a and b)
            return new Res<Expression>(new Ast.NotExpr(new Ast.BetweenExpression(r.v, re1.v, re2.v)), re2.next);
        }
    }

    Res<Expression> paInExpression(Seq s) {
        // multiset !
        Res<Expression> r = paMultisetExpression(s);
        if (r == null) {
            return null;
        }
        Res<String> r2 = c.opt(c.forkw("not")).pa(r.next);
        Res<String> r3 = c.forkw("in").pa(r2.next);
        if (r3 == null) {
            return r;
        }
        Res<List<Expression>> r4 = c.withParensCommit(c.sep1(pExpr, c.pComma), r3.next);
        Ast.Expression e = new Ast.InExpression(r.v, r4.v);
        if (r2.v == null) {
            return new Res<>(e, r4.next);
        } else {
            return new Res<Expression>(new Ast.NotExpr(e), r4.next);
        }

    }

    Res<Expression> paMultisetExpression(Seq s) {
        // fixme
        Res<Expression> r = paAddExpression(s);
        if (r == null) {
            return null;
        }
        Expression e = r.v;
        Seq ss = r.next;
        while (true) {
            Res rmu = pkw_multiset_union_all.pa(ss);
            if (rmu == null) {
                return new Res<Expression>(e, ss);
            }
            Res<Expression> r2 = paAddExpression(rmu.next);
            must(r2, rmu.next, "expecting an expression");
            e = new Ast.MultisetExpr("multi", e, r2.v);
            ss = r2.next;
        }
    }

    Res<Expression> paAddExpression(Seq s) {
        Res<Expression> r = paMulExpression(s);
        if (r == null) {
            return null;
        }
        Expression e = r.v;
        Seq ss = r.next;
        while (true) {
            final Ast.Binop binop;
            switch (ss.head().ttype) {
                case Plus:
                    binop = Ast.Binop.ADD;
                    break;
                case Minus:
                    binop = Ast.Binop.MINUS;
                    break;
                case StringAdd:
                    binop = Ast.Binop.CONCAT;
                    break;
                default:
                    return new Res<>(e, ss);
            }
            Res<Expression> rx = paMulExpression(ss.tail());
            must(rx, ss.tail(), "expecting an expression");
            e = new Ast.BinopExpression(binop, e, rx.v);
            ss = rx.next;
        }
    }

    Res<Expression> paMulExpression(Seq s) {
        Res<Expression> r = paUnarySignExpr(s);
        if (r == null) {
            return null;
        }
        Expression e = r.v;
        Seq ss = r.next;
        while (true) {
            final Ast.Binop binop;
            switch (ss.head().ttype) {
                case Mul:
                    binop = Ast.Binop.ADD;
                    break;
                case Div:
                    binop = Ast.Binop.MINUS;
                    break;
                case Ident:
                    if (ss.head().str.equalsIgnoreCase("mod")) {
                        binop = Ast.Binop.MOD;
                        break;
                    } else {
                        return new Res<>(e, ss);
                    }
                default:
                    return new Res<>(e, ss);
            }
            Res<Expression> rx = paUnarySignExpr(ss.tail());
            must(rx, ss.tail(), "expecting an expression");
            e = new Ast.BinopExpression(binop, e, rx.v);
            ss = rx.next;
        }
    }

    Res<Expression> paUnarySignExpr(Seq s) {
        switch (s.head().ttype) {
            case Plus:
                Res<Expression> r1 = pExponentExpr(s.tail());
                must(r1, s.tail(), "exptecting expression");
                return new Res<Expression>(new Ast.UnaryPlusExpression(r1.v), r1.next);
            case Minus:
                Res<Expression> r2 = pExponentExpr(s.tail());
                must(r2, s.tail(), "exptecting expression");
                return new Res<Expression>(new Ast.UnaryPlusExpression(r2.v), r2.next);
            default:
                return pExponentExpr(s);
        }

    }

    Res<Expression> pExponentExpr(Seq s) {
        Res<Expression> r = paAtomExpr(s);
        if (r == null) {
            return null;
        }
        Res<String> rp = c.pPower.pa(r.next);
        if (rp == null) {
            return r;
        }
        Res<Expression> re = paAtomExpr(rp.next);
        must(re, rp.next, "Expression expected");
        return new Res<Expression>(new Ast.BinopExpression(Ast.Binop.POWER, r.v, re.v), re.next);
    }
    /* 
     and pAtom s = orn [
     pNumber,
     pString,
     (* true and false are just identifiers in pl/sql ... *)
     pFalse,
     pTrue,
     pParenExpr,
     pSQLAttribute,
     pNew, (* fixme : commit after new *)
     pVariableOrFunctionCall,
     pNull,
     pCaseBoolExpr,
     pCaseMatchExpr
     ] s
     */

    public Res<Expression> paAtomExpr(Seq s) {
        TokenType tt = s.head().ttype;
        if (tt == TokenType.Int) {
            return new Res<Expression>(new Ast.CNumber(new BigDecimal(s.head().str)), s.tail());
        }
        if (tt == TokenType.String) {
            String s1 = s.head().str;
            String s2 = s1.substring(1, s1.length() - 1);
            return new Res<Expression>(new Ast.CString(s2.replace("''", "'")), s.tail());
        }
        if (tt == TokenType.QString) {
            String s1 = s.head().str;
            String s2 = s1.substring(3, s1.length() - 2);
            return new Res<Expression>(new Ast.CString(s2), s.tail());
        }

        if (tt == TokenType.LParen) {
            Res<Expression> r = c.withParensCommit(pExpr, s);
            return new Res<>(r.v, r.next);
        }
        if (tt == TokenType.DollarDollarIdent) {
            String str = s.head().str.substring(2);
            return new Res<Expression>(new Ast.DollarDollar(str), s.tail());
        }

        if (tt == TokenType.Ident) {
            String s2 = s.head().str;
            if (s2.equalsIgnoreCase("true")) {
                return new Res<Expression>(new Ast.CBool(true), s.tail());
            }
            if (s2.equalsIgnoreCase("false")) {
                return new Res<Expression>(new Ast.CBool(false), s.tail());
            }
            if (s2.equalsIgnoreCase("null")) {
                return new Res<Expression>(new Ast.CNull(), s.tail());
            }
            if (s2.equalsIgnoreCase("case")) {
                return paCaseExpr(s);
            }
            if (s2.equalsIgnoreCase("sql")) {
                return paSQLAttribute(s);
            }
            // date '2001-1-1' is expression of type date
            if (s2.equalsIgnoreCase("date") && s.tail().head().ttype == TokenType.String) {
                String sc = s.tail().head().str;
                String sclean = sc.substring(1, sc.length() - 1);
                return new Res<Expression>(new Ast.CString(sclean.replace("''", "'")), s.tail().tail());
            }

            if (s2.equalsIgnoreCase("new")) {
                Res<List<Ast.CallPart>> r = paCallParts(s.tail());
                must(r, s.tail(), "expecting a callpart");
                return new Res<Expression>(new Ast.NewExpression(r.v), r.next);
            }

        }
        return paVariableOrFunctionCall(s);
    }

    Res<Expression> paCaseExpr(Seq s) {
        Res<String> r0 = c.forkw("case").pa(s);
        if (r0 == null) {
            return null;
        }
        Seq ss;
        Res<String> r1 = c.forkw("when").pa(r0.next);
        Expression em;
        if (r1 == null) {
            Res<Expression> rm = paExpr(r0.next);
            em = rm.v;
            ss = rm.next;
        } else {
            ss = r0.next;
            em = null;
        }
        List<Ast.CaseExpressionPart> l = new ArrayList<>();
        while (true) {
            Res<String> r2 = c.forkw("when").pa(ss);
            if (r2 == null) {
                break;
            }
            Res<Expression> re = paExpr(r2.next);
            Res<String> rt = c.forkw("then").pa(re.next);
            Res<Expression> re2 = paExpr(rt.next);
            l.add(new Ast.CaseExpressionPart(re.v, re2.v));
            ss = re2.next;
        }
        Res<String> r3 = c.forkw("else").pa(ss);
        Expression defaultt;
        if (r3 == null) {
            defaultt = null;
        } else {
            Res<Expression> rd = paExpr(r3.next);
            defaultt = rd.v;
            ss = rd.next;
        }
        Res<String> r4 = c.forkw("end").pa(ss);
        must(r4, ss, "expecting and END");
        if (em == null) {
            return new Res<Expression>(new Ast.CaseBoolExpression(l, defaultt), r4.next);
        } else {
            return new Res<Expression>(new Ast.CaseMatchExpression(em, l, defaultt), r4.next);
        }
    }

    Res<Expression> paSQLAttribute(Seq s) {
        Res<String> r1 = c.forkw("sql").pa(s);
        Res<String> r2 = c.pPercent.pa(r1.next);
        // fixme : quoted allowed
        Res<List<Ast.CallPart>> rcp = paCallParts(r2.next);
        return new Res<Expression>(new Ast.SqlAttribute(rcp.v), rcp.next);
    }

    Pa<Ast.LValue> pLValue = new Pa<Ast.LValue>() {
        @Override
        protected Res<Ast.LValue> par(Seq s) {
            Res<List<Ast.CallPart>> r = paCallParts(s);
            if (r == null) {
                return null;
            }
            return new Res<>(new Ast.LValue(r.v), r.next);
        }
    };

    Res<List<Ast.CallPart>> paCallParts(Seq s) {
        Res<Ast.Ident> rident = pIdent.pa(s);
        if (rident == null) {
            return null;
        }
        ArrayList<Ast.CallPart> l = new ArrayList<>();
        l.add(new Ast.Component(rident.v));
        Seq next = rident.next;
        while (true) {
            Res rdot = c.pDot.pa(next);
            if (rdot != null) {
                Res<Ast.Ident> rident2 = c.mustp(pIdent, "ident").pa(rdot.next);
                l.add(new Ast.Component(rident2.v));
                next = rident2.next;
                continue;
            }
            Res<List<Ast.ActualParam>> rca = paCallArgs(next);
            if (rca == null) {
                break;
            }
            l.add(new Ast.CallOrIndexOp(rca.v));
            next = rca.next;
        }
        return new Res<List<Ast.CallPart>>(l, next);
    }

    Res<Expression> paExtractFunction(Seq s) {
        Res<Ast.Ident> r = pIdent.pa(s);
        if (r == null || !r.v.val.equals("EXTRACT")) {
            return null;
        }
        Res r2 = c.pPOpen.pa(r.next);
        if (r2 == null) {
            return null;
        }
        Res<String> r3 = justkw.pa(r2.next);
        if (r3 == null) {
            return null;
        }
        if (!(r3.v.equalsIgnoreCase("year")
                || r3.v.equalsIgnoreCase("month")
                || r3.v.equalsIgnoreCase("day"))) {
            return null;
        }
        Res r4 = pkw_from.pa(r3.next);
        if (r4 == null) {
            return null;
        }
        // from here comitted
        Res<Ast.Expression> r5 = c.mustp(pExpr, "expression").pa(r4.next);
        Res r6 = c.mustp(c.pPClose, "close paren").pa(r5.next);
        return new Res<Expression>(new Ast.ExtractDatePart(r3.v.toUpperCase(), r5.v), r6.next);
    }

    Res<Expression> paVariableOrFunctionCall(Seq s) {
        Res<Expression> r_extract = paExtractFunction(s);
        if (r_extract != null) {
            return r_extract;
        }

        Res<List<Ast.CallPart>> r = paCallParts(s);
        if (r == null) {
            return null;
        }
        Res<String> rp = c.pPercent.pa(r.next);
        if (rp == null) {
            return new Res<Expression>(new Ast.VarOrCallExpression(r.v), r.next);
        } else {
            Res<Ast.Ident> ra = pIdent.pa(rp.next);
            must(ra, rp.next, "expectinga cursor attribute");
            // fixme maybe change to Ast
            Res<T2<String, Ast.Ident>> noch_ein_dot = c.opt(c.seq2(c.pDot, pIdent)).pa(ra.next);
            String attr_val;
            if (noch_ein_dot.v != null) {
                attr_val = ra.v.val + "." + noch_ein_dot.v.f2.val;
            } else {
                attr_val = ra.v.val;
            }
            return new Res<Expression>(new Ast.CursorAttribute(r.v, attr_val), noch_ein_dot.next);
        }
    }

    public Res<List<Ast.ActualParam>> paCallArgs(Seq s) {
        if (s.head().ttype != TokenType.LParen) {
            return null;
        } else {
            Res<List<Ast.ActualParam>> r = c.withParensCommit(c.sep(paParam, c.pComma), s);
            return r;
        }
    }

    public final Pa<Ast.ActualParam> paParam = new Pa<Ast.ActualParam>() {

        @Override
        public Res<Ast.ActualParam> par(Seq s) {
            Res<T2<T2<Ast.Ident, String>, Expression>> r1
                    = c.seq2(c.opt(c.seq2(pIdent, c.pArrow)), pExpr).pa(s);
            if (r1 == null) {
                return null;
            }
            if ((r1.v.f1 == null)) {
                return new Res<>(new Ast.ActualParam(r1.v.f2, null), r1.next);
            } else {
                return new Res<>(new Ast.ActualParam(r1.v.f2, r1.v.f1.f1.val), r1.next);
            }

        }

    };

    /*
     public final Pa<Ast.CallPart> pCallPart
     = new Pa<Ast.CallPart>() {
     @Override
     public Res<Ast.CallPart> pa(Seq s) {
     Res<Ast.Ident> r1 = pIdent.pa(s);
     if (r1 == null) {
     return null;
     }
     Res<List<Ast.ActualParam>> r2 = paCallArgs(r1.next);
     if (r2 == null) {
     return new Res<>(new Ast.Component(r1.v), r1.next);
     } else {
     return new Res<>(new Ast.Call(r1.v, r2.v), r2.next);
     }
     }

     @Override
     public String toString() {
     return "pCall";
     }
     };
     */
    //   Declarations
    //sep1(pIdent,tDot) s
    public final Pa<List<Ast.Ident>> pIdents = c.sep1(pIdent, c.pDot);

    public Res<Ast.DataType> paDataType(Seq s) {
        Res r1 = c.forkw2("timestamp", "with").pa(s);
        if (r1 != null) {
            Res rloc = c.bopt(c.forkw("local")).pa(r1.next);
            Res rtz = c.forkw2("time", "zone").pa(rloc.next);
            must(rtz, r1.next, "expecteing with 'time zone'");
            return new Res<Ast.DataType>(new Ast.TimestampWithTimezone(), rtz.next);
        }
        Res<String> r2 = c.forkw2("long", "raw").pa(s);
        if (r2 != null) {
            return new Res<Ast.DataType>(new Ast.LongRaw(), r2.next);
        }
        Res r2b = c.seq2(c.forkw2("interval", "year"), c.forkw2("to", "month")).pa(s);
        if (r2b != null) {
            return new Res<Ast.DataType>(new Ast.IntervalYearToMonth(), r2b.next);
        }

        Res r2c = c.seq2(c.forkw2("interval", "day"), c.forkw2("to", "second")).pa(s);
        if (r2c != null) {
            return new Res<Ast.DataType>(new Ast.IntervalDayToSecond(), r2c.next);
        }

        // parametrisierter typ varchar2, varchar ,raw, number
        Res<T2<Ast.Ident, String>> r3 = c.seq2(pIdent, c.pPOpen).pa(s);
        if (r3 != null) {
            String tyname1 = r3.v.f1.val;
            if (tyname1.equalsIgnoreCase("varchar2") || tyname1.equalsIgnoreCase("varchar")) {
                Res<T2<Integer, String>> r99 = c.seq2(pNatural, c.opt(c.or2(c.forkw("char"), c.forkw("byte")))).pa(r3.next);
                Res r100 = c.mustp(c.pPClose, "expecting ')'").pa(r99.next);
                return new Res<Ast.DataType>(new Ast.ParameterizedType(r3.v.f1, r99.v.f1, null), r100.next);
            } else {
                Res<T2<Integer, T2<String, Integer>>> r99 = c.seq2(pNatural, c.opt(c.seq2(c.pComma, pNatural))).pa(r3.next);
                Res r100 = c.mustp(c.pPClose, "expecting ')'").pa(r99.next);
                Integer z;
                if (r99.v.f2 != null) {
                    z = r99.v.f2.f2;
                } else {
                    z = null;
                }
                return new Res<Ast.DataType>(new Ast.ParameterizedType(r3.v.f1, r99.v.f1, z), r100.next);
            }
        }
        // ident.ident... 
        //  ident.ident... %type
        //  ident.ident... %rowtype

        Res<List<Ast.Ident>> r4 = c.sep1(pIdent, c.pDot).pa(s);
        if (r4 == null) {
            return null;
        }
        Res<String> r5 = c.pPercent.pa(r4.next);
        if (r5 == null) {
            return new Res<Ast.DataType>(new Ast.NamedType(r4.v), r4.next);
        }
        Res<String> r6 = c.forkw("type").pa(r5.next);
        if (r6 != null) {
            return new Res<Ast.DataType>(new Ast.VarType(r4.v), r6.next);
        }
        Res<String> r7 = c.forkw("rowtype").pa(r5.next);
        if (r7 != null) {
            return new Res<Ast.DataType>(new Ast.RowType(r4.v), r7.next);
        }
        throw new ParseException("expecting row or type", r5.next);
    }

    public Pa<Ast.DataType> pDataType = new Pa<Ast.DataType>() {

        @Override
        public Res<Ast.DataType> par(Seq s) {
            return paDataType(s);
        }
    };
    /*    
     fun pObjectName s: object_name result  =
     tr(seq2(pIdent,opt(commit(tDot,pIdent))),
     fn (x,NONE) => (NONE,x)
     | (x,SOME (_,y)) => (SOME x,y)) s
     */

    public Res<Ast.ObjectName> paObjectName(Seq s) {
        Res<Ast.Ident> r = pIdent.pa(s);
        if (r == null) {
            return null;
        }
        Res<String> r2 = c.pDot.pa(r.next);
        if (r2 == null) {
            return new Res<>(new Ast.ObjectName(null, r.v), r.next);
        }
        Res<Ast.Ident> r3 = pIdent.pa(r2.next);
        must(r3, r2.next, "expecting ident");
        return new Res<>(new Ast.ObjectName(r.v, r3.v), r3.next);
    }

    public Res<Boolean> paCreateOrReplace(Seq s) {
        Res<String> r = c.forkw("create").pa(s);
        if (r == null) {
            return null;
        }
        Res<String> r2 = c.opt(c.forkw2("or", "replace")).pa(r.next);
        return new Res<>(r2.v == null, r2.next);
    }

    /*
     fun pInvokerClause s = 
     tr(commit(kw "authid",orn[kw "current_user",kw "definer"]),
     fn (_,x) => x) s
     */
    public Pa<String> pInvokerClause = new Pa<String>() {

        @Override
        public Res<String> par(Seq s) {
            Res<String> r = c.forkw("authid").pa(s);
            if (r == null) {
                return null;
            }
            // fixme check for current_user or definer
            return c.mustp(justkw, "some word").pa(r.next);
        }
    };

    public Pa<String> pIsOrAs = new Pa<String>() {
        @Override
        public Res<String> par(Seq s) {
            Res<String> r = justkw.pa(s);
            if (r != null && (r.v.equals("is") || r.v.equals("as"))) {
                return r;
            } else {
                return null;
            }
        }
    };

    public Pa<String> pAssignOrDefault = c.or2(c.forkw("default"), c.pAssign);

    public Pa<String> pNotNull = c.forkw2("not", "null");

    //fun pVariableDeclaration s =
    // seq3(pDataType, bopt(pNotNull), opt(commit(pAssignOrDefault,pExpr))) s
    public Pa<T3<Ast.DataType, Boolean, T2<String, Ast.Expression>>> pVariableDeclaration
            = c.seq3(pDataType, c.bopt(pNotNull), c.opt(c.seq2(pAssignOrDefault, pExpr)));

    // fun pConstantDeclaration s =
    //  seq5(kw "constant",pDataType, bopt(pNotNull), pAssignOrDefault,pExpr) s
    public Pa<T3<Ast.DataType, Boolean, Ast.Expression>> pConstantDeclaration
            = new Pa<T3<Ast.DataType, Boolean, Ast.Expression>>() {

                @Override
                public Res<T3<Ast.DataType, Boolean, Expression>> par(Seq s) {
                    Res<String> r = c.forkw("constant").pa(s);
                    if (r == null) {
                        return null;
                    }
                    Res<T4<Ast.DataType, Boolean, String, Expression>> r2
                    = c.seq4(pDataType, c.bopt(pNotNull), pAssignOrDefault, pExpr).pa(r.next);
                    must(r2, r.next, "expecting constant declaration");
                    return new Res<>(new T3<>(r2.v.f1, r2.v.f2, r2.v.f4), r2.next);
                }

            };

    //fun pExceptionDeclaration s =
    // kw "exception" s 
    public Pa<Ast.Declaration> pItemDeclaration = new Pa<Ast.Declaration>() {

        @Override
        public Res<Ast.Declaration> par(Seq s) {
            Res<Ast.Ident> r = pIdent.pa(s);
            if (r == null) {
                return null;
            }
            Res<T3<Ast.DataType, Boolean, T2<String, Ast.Expression>>> r1
                    = pVariableDeclaration.pa(r.next);
            if (r1 != null) {
                Ast.Expression de = r1.v.f3 == null ? null : r1.v.f3.f2;
                return new Res<Ast.Declaration>(new Ast.VariableDeclaration(r.v, r1.v.f1, r1.v.f2, false, de), r1.next);
            }
            Res<T3<Ast.DataType, Boolean, Ast.Expression>> r2
                    = pConstantDeclaration.pa(r.next);
            if (r2 != null) {
                return new Res<Ast.Declaration>(new Ast.VariableDeclaration(r.v, r2.v.f1, r2.v.f2, true, r2.v.f3), r2.next);
            }

            Res<String> r3 = c.forkw("exception").pa(r.next);
            if (r3 != null) {
                return new Res<Ast.Declaration>(new Ast.ExceptionDeclaration(r.v), r3.next);
            }
            return null;
        }

    };

    //  tr(seq3(pIdent,pDataType,opt(seq3(bopt(pNotNull),pAssignOrDefault,pExpr))),
    public Pa<Ast.RecordField> pRecordField = new Pa<Ast.RecordField>() {

        @Override
        public Res<Ast.RecordField> par(Seq s) {
            Res<T2<Ast.Ident, Ast.DataType>> r = c.seq2(pIdent, pDataType).pa(s);
            if (r == null) {
                return null;
            }
            Res<T3<Boolean, String, Ast.Expression>> r2 = c.seq3(c.bopt(pNotNull), pAssignOrDefault, pExpr).pa(r.next);
            if (r2 == null) {
                return new Res<>(new Ast.RecordField(r.v.f1, r.v.f2, false, null), r.next);
            } else {
                return new Res<>(new Ast.RecordField(r.v.f1, r.v.f2, r2.v.f1, r2.v.f3), r2.next);
            }
        }

    };

    public Pa<Ast.TypeDefinition> pRecordTypeDefinition
            = new Pa<Ast.TypeDefinition>() {

                @Override
                public Res<Ast.TypeDefinition> par(Seq s) {
                    Res<String> r = c.forkw("record").pa(s);
                    if (r == null) {
                        return null;
                    }
                    Res<List<Ast.RecordField>> r2 = c.withParensCommit(c.sep1(pRecordField, c.pComma), r.next);
                    must(r2, r.next, "expecting record fields");
                    return new Res<Ast.TypeDefinition>(new Ast.RecordType(r2.v), r2.next);
                }
            };

    //tr(commit(seq2(kw "ref" ,kw "cursor"),opt(commit(kw "return",pDataType))),
    public Pa<Ast.TypeDefinition> pRefCursorTypeDefinition
            = new Pa<Ast.TypeDefinition>() {

                @Override
                public Res<Ast.TypeDefinition> par(Seq s) {
                    Res<String> r = c.forkw2("ref", "cursor").pa(s);
                    if (r == null) {
                        return null;
                    }
                    Res<String> r2 = c.forkw("return").pa(r.next);
                    if (r2 == null) {
                        return new Res<Ast.TypeDefinition>(new Ast.RefCursorType(null), r.next);
                    } else {
                        Res<Ast.DataType> r3 = c.mustp(pDataType, "datatype").pa(r2.next);
                        return new Res<Ast.TypeDefinition>(new Ast.RefCursorType(r3.v), r3.next);
                    }
                }
            };

    /*"table",seq4( 
     kw "of",
     pDataType,
     bopt(pNotNull),
     opt(commit(kw "index",seq2(kw "by",pDataType))))),*/
    public Pa<Ast.TypeDefinition> pNestetTableTypeDefinition = new Pa<Ast.TypeDefinition>() {
        @Override
        public Res<Ast.TypeDefinition> par(Seq s) {
            Res<String> r = c.forkw2("table", "of").pa(s);
            if (r == null) {
                return null;
            }
            Res<T2<Ast.DataType, Boolean>> r2 = c.seq2(pDataType, c.bopt(pNotNull)).pa(r.next);
            must(r2, r.next, "datatype");
            Res<T3<String, String, Ast.DataType>> r3 = c.seq3(pkw_index, pkw_by, pDataType).pa(r2.next);
            if (r3 == null) {
                return new Res<Ast.TypeDefinition>(new Ast.TableSimple(r2.v.f1, r2.v.f2), r2.next);
            } else {
                return new Res<Ast.TypeDefinition>(new Ast.TableIndexed(r2.v.f1, r2.v.f2, r3.v.f3), r3.next);
            }
        }
    };

    //seq6(tLParen,pPlainInt,tRParen, kw "of",pDataType,bopt(pNotNull))),
    public Pa<Ast.TypeDefinition> pVarrayTypeDefinition = new Pa<Ast.TypeDefinition>() {

        @Override
        public Res<Ast.TypeDefinition> par(Seq s) {
            Res<String> r = c.or2(pkw_varray, pkw_varying_array).pa(s);
            if (r == null) {
                return null;
            }
            Res<Integer> r2 = c.withParensCommit(pNatural, r.next);
            Res<T3<String, Ast.DataType, Boolean>> r3 = c.seq3(pkw_of, pDataType, c.bopt(pNotNull)).pa(r2.next);
            return new Res<Ast.TypeDefinition>(new Ast.Varray(r3.v.f2, r2.v, r3.v.f3), r3.next);
        }

    };

    public Res<Ast.Declaration> paTypeDefinition(Seq s) {
        Res<String> r = pkw_type.pa(s);
        if (r == null) {
            return null;
        }
        Res<T2<Ast.Ident, String>> ris = c.mustp(c.seq2(pIdent, pkw_is), "expecting 'bla is'").pa(r.next);
        Seq next = ris.next;
        Ast.Ident name = ris.v.f1;
        Res<Ast.TypeDefinition> r1 = pRecordTypeDefinition.pa(next);
        if (r1 != null) {
            return new Res<Ast.Declaration>(new Ast.TypeDeclaration(name, r1.v), r1.next);
        }
        Res<Ast.TypeDefinition> r2 = pRefCursorTypeDefinition.pa(next);
        if (r2 != null) {
            return new Res<Ast.Declaration>(new Ast.TypeDeclaration(name, r2.v), r2.next);
        }
        Res<Ast.TypeDefinition> r3 = pNestetTableTypeDefinition.pa(next);
        if (r3 != null) {
            return new Res<Ast.Declaration>(new Ast.TypeDeclaration(name, r3.v), r3.next);
        }
        Res<Ast.TypeDefinition> r4 = pVarrayTypeDefinition.pa(next);
        if (r4 != null) {
            return new Res<Ast.Declaration>(new Ast.TypeDeclaration(name, r4.v), r4.next);
        }

        return null;
    }

    public Pa<T2<Integer, Integer>> pRangeOption = new Pa<T2<Integer, Integer>>() {

        @Override
        protected Res<T2<Integer, Integer>> par(Seq s) {
            Res<String> r = pkw_range.pa(s);
            if (r == null) {
                return new Res<>(null, s);
            }
            Res<T3<Integer, String, Integer>> r2 = c.seq3(pInteger, c.pDotDot, pInteger).pa(r.next);
            must(r2, r.next, "expectint int .. int ");
            return new Res<>(new T2<Integer, Integer>(r2.v.f1, r2.v.f3), r2.next);
        }
    };

//    fun pSubTypeDefinition s  =
//    tr(commit(kw "subtype", seq4(pIdent,kw "is",pDataType,bopt(pNotNull))),
//       fn (_,(name,_,dt,nn)) =>   TypeDefinition (name,SubType (dt,nn))) s
    public Res<Ast.Declaration> paSubTypeDeclaration(Seq s) {
        Res<String> r = pkw_subtype.pa(s);
        if (r == null) {
            return null;
        }
        Res<T2<Ast.Ident, String>> ris = c.mustp(c.seq2(pIdent, pkw_is), "expecting '<subtypename> is'").pa(r.next);

        Ast.Ident name = ris.v.f1;

        Res<T3<Ast.DataType, T2<Integer, Integer>, Boolean>> r2 = c.seq3(pDataType, pRangeOption, c.bopt(pNotNull)).pa(ris.next);
        return new Res<Ast.Declaration>(new Ast.TypeDeclaration(name, new Ast.SubType(r2.v.f1, r2.v.f2, r2.v.f3)), r2.next);
    }

    public Res<Ast.ParamMode> paParamModeOption(Seq s) {
        Res<String> r = pkw_in_out.pa(s);
        if (r != null) {
            Res<Boolean> r2 = c.bopt(c.forkw("nocopy")).pa(r.next);
            return new Res<>(new Ast.ParamMode(Ast.ParamModeType.INOUT, r2.v), r2.next);
        }
        Res<String> r3 = pkw_in.pa(s);
        if (r3 != null) {
            return new Res<>(new Ast.ParamMode(Ast.ParamModeType.IN, false), r3.next);
        }
        Res<String> r4 = pkw_out.pa(s);
        if (r4 != null) {
            Res<Boolean> r5 = c.bopt(pkw_nocopy).pa(r4.next);
            return new Res<>(new Ast.ParamMode(Ast.ParamModeType.OUT, r5.v), r5.next);
        }
        return new Res<>(null, s);
    }

//    fun pCharacterSet s =
//    cseq3(kw "CHARACTER", kw "SET", kw "ANY_CS") s
    Pa<String> pkw_character_set = c.forkw2("character", "set");

    public Res<Boolean> paCSOption(Seq s) {
        Res<String> r = pkw_character_set.pa(s);
        if (r == null) {
            return new Res<>(false, s);
        }
        Res<String> r2 = c.mustp(c.forkw("any_cs"), "expecting 'any_cs'").pa(r.next);
        return new Res<>(true, r2.next);
    }

    //    seq5(pIdent,opt(pParamMode),pDataType,opt pCharacterSet,opt(commit(pAssignOrDefault,pExpr))),
    public Pa<Ast.Parameter> pParameter = new Pa<Ast.Parameter>() {

        @Override
        public Res<Ast.Parameter> par(Seq s) {
            Res<Ast.Ident> r = pIdent.pa(s);
            if (r == null) {
                return null;
            }
            Res<Ast.ParamMode> r2 = paParamModeOption(r.next);
            Res<Ast.DataType> r3 = c.mustp(pDataType, "expecting datatype").pa(r2.next);
            Res<Boolean> r4 = paCSOption(r3.next);
            Res<Ast.Expression> r5 = c.opt(c.commit(pAssignOrDefault, pExpr)).pa(r4.next);
            return new Res<>(new Ast.Parameter(r.v, r3.v, r2.v, r5.v), r5.next);
        }
    };

    public Res<List<String>> paFunctionAttributes(Seq s) {
        //kw "deterministic",kw "pipelined",kw "parallel_enable",kw "result_cache"]),
        List<String> l = new ArrayList<>();
        Seq next = s;
        while (true) {
            Res<String> r = c.token(TokenType.Ident).pa(next);
            if (r != null && (r.v.equalsIgnoreCase("deterministic")
                    || r.v.equalsIgnoreCase("pipelined")
                    || r.v.equalsIgnoreCase("parallel_enable")
                    || r.v.equalsIgnoreCase("result_cache"))) {
                l.add(r.v.toLowerCase());
                next = r.next;
            } else {
                break;
            }
        }
        return new Res<>(l, next);
    }
    /*
     r(commit(kw "function",
     seq5(pProcOrFunNameInDecl,
     pParameterDeclarations, (*opt(seq3(tLParen,sep1(pParameterDeclaration,tComma),tRParen)),*)
     kw "return",
     pDataType,
     pFunctionAttribute
     )),
     fn (_,(name,l,_,rdt,attrs)) => FunctionHeading (name,l,rdt,attrs)) s*/

    public Res<Ast.FunctionHeading> paFunctionHeading(Seq s) {
        Res<String> r = pkw_function.pa(s);
        if (r == null) {
            return null;
        }
        Res<Ast.Ident> r2 = pIdent.pa(r.next);
        must(r2, r.next, "ident");
        Res<List<Ast.Parameter>> r3 = c.withParensCommit(c.sep1(pParameter, c.pComma), r2.next);
        List<Ast.Parameter> params;
        Seq next;
        if (r3 == null) {
            params = new ArrayList<>();
            next = r2.next;
        } else {
            params = r3.v;
            next = r3.next;
        }
        Res<String> r4 = pkw_return.pa(next);
        Res<Ast.DataType> r5 = pDataType.pa(r4.next);
        Res<List<String>> r6 = paFunctionAttributes(r5.next);
        return new Res<>(new Ast.FunctionHeading(r2.v, params, r5.v, r6.v), r6.next);
    }

    /*
     fun pProcedureHeading s =
     tr(commit(kw "procedure", seq2(pProcOrFunNameInDecl,pParameterDeclarations)),
     fn (_,(name,l)) => ProcedureHeading (name,l)) s
     */
    public Res<Ast.ProcedureHeading> paProcedureHeading(Seq s) {
        Res<String> r = pkw_procedure.pa(s);
        if (r == null) {
            return null;
        }
        Res<Ast.Ident> r2 = pIdent.pa(r.next);
        Res<List<Ast.Parameter>> r3 = c.withParensCommit(c.sep1(pParameter, c.pComma), r2.next);

        if (r3 == null) {
            return new Res<>(new Ast.ProcedureHeading(r2.v, new ArrayList<Ast.Parameter>()), r2.next);
        } else {
            return new Res<>(new Ast.ProcedureHeading(r2.v, r3.v), r3.next);
        }
    }

    Pa<Token> pNoSemi = new Pa<Token>() {

        @Override
        public Res<Token> par(Seq s) {
            if (s.head().ttype == TokenType.Semi || s.head().ttype == TokenType.TheEnd) {
                return null;
            } else {
                return new Res<>(s.head(), s.tail());
            }
        }
    };

    /*
     pPragma s =
     tr(commit(kw "pragma",seq2(pIdent,many(pNoSemi))),
     fn(_,(name,l)) => Pragma (name,l)) s
     */
    public Res<Ast.Declaration> paPragma(Seq s) {
        Res<String> r = pkw_pragma.pa(s);
        if (r == null) {
            return null;
        }
        if (pkw_restrict_references.pa(r.next) != null) {
            return paRestrictReferencesCommited(s);
        }
        Res<Ast.Ident> r1 = pIdent.pa(r.next);
        Res<List<Token>> r2 = c.many(pNoSemi).pa(r1.next);
        return new Res<Ast.Declaration>(new Ast.SimplePragma(r1.v, r2.v), r2.next);
    }

    final Pa<String> pRestrictReferencesMode = new Pa<String>() {
        @Override
        protected Res<String> par(Seq s) {
            if (s.head().ttype == TokenType.String) {
                String s1 = s.head().str;
                String s2 = s1.substring(1, s1.length() - 1);
                return new Res<>(s2.replace("''", "'"), s.tail());
            }
            Res<String> r = justkw.pa(s);
            if (r == null) {
                return null;
            }
            return r;
        }
    };

    public Res<Ast.Declaration> paRestrictReferencesCommited(Seq s) {
        Res r = c.mustp(pkw2_pragma_restrict_references,
                "expecting pragma restrict references").pa(s);
        Res r2 = c.pPOpen.pa(r.next);
        must(r2, r.next, "expecting paren open");

        final Seq next;
        final Ast.Ident ident;
        Res r3 = pkw_default.pa(r2.next);
        if (r3 == null) {
            Res<Ast.Ident> r4 = pIdent.pa(r2.next);
            must(r4, r2.next, "expecting ident");
            ident = r4.v;
            next = r4.next;
        } else {
            ident = null;
            next = r3.next;
        }
        Res r5 = c.pComma.pa(next);
        must(r5, next, "expecting ,");
        Res<List<String>> r6 = c.sep1(pRestrictReferencesMode, c.pComma).pa(r5.next);
        must(r6, r5.next, "at least one mode");
        Res r7 = c.pPClose.pa(r6.next);
        must(r7, r6.next, "expecting paren close");
        return new Res<Ast.Declaration>(new Ast.PragmaRestrictReferences(ident, ident == null, r6.v), r7.next);
    }

    public Res<List<Token>> paBalancedParenAndNoSemi(Seq s) {
        int level = 0;
        List<Token> acc = new ArrayList<>();
        Seq next = s;
        while (true) {
            switch (next.head().ttype) {
                case Semi:
                case TheEnd:
                    if (level > 0) {
                        throw new ParseException("parens not balanced", next);
                    } else {
                        return new Res<>(acc, next);
                    }
                case LParen:
                    level++;
                    acc.add(next.head());
                    next = next.tail();
                    break;
                case RParen:
                    if (level == 0) {
                        return new Res<>(acc, next);
                    } else {
                        level--;
                        acc.add(next.head());
                        next = next.tail();
                    }
                    break;
                default:
                    acc.add(next.head());
                    next = next.tail();
            }
        }
    }
    /*
     fun pSQLStatement s =
     case get s of 
     t as {ttype=T.Ident,str=str, ...} => 
     if ciequal(str,"INSERT") 
     orelse ciequal(str,"UPDATE")
     orelse ciequal(str,"SELECT")
     orelse ciequal(str,"MERGE")
     orelse ciequal(str,"delete") then
     tr(pBalancedParenAndNoSemi,SQLStatement) s
     else NONE
     | _ => NONE
     */

    public Res<Ast.Statement> paSQLStatement(Seq s) {
        if (s.head().ttype != TokenType.Ident) {
            return null;
        }
        String a = s.head().str;
        if (a.equalsIgnoreCase("insert")
                || a.equalsIgnoreCase("update")
                || a.equalsIgnoreCase("delete")
                || a.equalsIgnoreCase("select")
                || a.equalsIgnoreCase("merge")
                || a.equalsIgnoreCase("with")) {
            Res<List<Token>> r = paBalancedParenAndNoSemi(s);
            return new Res<Ast.Statement>(new Ast.SqlStatement(r.v), r.next);
        } else {
            return null;
        }
    }

    //fun pLang s = tr(cseq4(kw "language",kw "java",kw "name",pString),
    //             fn (_,_,_,CString str) => ("JAVA",str)) s
    public Pa<T2<String, String>> pLang = new Pa<T2<String, String>>() {

        @Override
        public Res<T2<String, String>> par(Seq s) {
            Res r = pkw_language.pa(s);
            if (r == null) {
                return null;
            }
            // fixme: java is fixed? 
            Res<T3<String, String, String>> r2 = c.seq3(c.forkw("java"), c.forkw("name"), c.token(TokenType.String)).pa(r.next);
            must(r2, r.next, "expecting java name '...' ");
            return new Res<>(new T2<String, String>("java", r2.v.f3), r2.next);
        }
    };

    /*
     fun pFunDecl s =
     bind(pFunctionHeading,
     fn h => orn[tr(cseq2(pIsOrAs,pLang),fn(_,(name,str)) => LFunctionDefinition (h,name,str)),
     tr(nix,fn () => FunctionDeclaration h)]) s
     */
    public Res<Ast.Declaration> paFunDecl(Seq s) {
        Res<Ast.FunctionHeading> r = paFunctionHeading(s);
        if (r == null) {
            return null;
        }
        Res<String> r2 = pIsOrAs.pa(r.next);
        if (r2 == null) {
            return new Res<Ast.Declaration>(new Ast.FunctionDeclaration(r.v), r.next);
        } else {
            Res<T2<String, String>> r3 = pLang.pa(r2.next);
            must(r3, r2.next, "java proc");
            return new Res<Ast.Declaration>(new Ast.ExtFunctionDefinition(r.v, r3.v.f1, r3.v.f2), r3.next);
        }
    }

    /*
     fun pProcDecl s =
     bind(pProcedureHeading,
     fn h => orn[tr(seq2(pIsOrAs,
     orn[tr(pLang,fn(name,str) => LProcedureDefinition (h,name,str))]),#2),
     tr(nix,fn () =>  ProcedureDeclaration h)]) s
     */
    public Res<Ast.Declaration> paProcDecl(Seq s) {
        Res<Ast.ProcedureHeading> r = paProcedureHeading(s);
        if (r == null) {
            return null;
        }
        Res<String> r2 = pIsOrAs.pa(r.next);
        if (r2 == null) {
            return new Res<Ast.Declaration>(new Ast.ProcedureDeclaration(r.v), r.next);
        } else {
            Res<T2<String, String>> r3 = pLang.pa(r2.next);
            must(r3, r2.next, "java proc");
            return new Res<Ast.Declaration>(new Ast.ExtProcedureDefinition(r.v, r3.v.f1, r3.v.f2), r3.next);
        }
    }

    Res<Ast.Declaration> paCursorDefinition(Seq s) {
        Res r = pkw_cursor.pa(s);
        if (r == null) {
            return null;
        }
        Res<Ast.Ident> rn = pIdent.pa(r.next);
        Res<List<Ast.Parameter>> r3 = c.withParensCommit(c.sep1(pParameter, c.pComma), rn.next);
        Res r_is = pkw_is.pa(r3 == null ? rn.next : r3.next);
        Res<List<Token>> rsql = paBalancedParenAndNoSemi(r_is.next);
        return new Res<Ast.Declaration>(new Ast.CursorDefinition(rn.v, r3 == null ? null : r3.v, rsql.v), rsql.next);
    }

    /*
     fun pDeclaration s = 
     orn[pTypeDefinition,
     pSubTypeDefinition,
     pProcDecl,
     pFunDecl,
     pItemDeclaration,
     pCursorDefinition,
     pPragma] s */
    public final Pa<Ast.Declaration> pDeclaration = new Pa<Ast.Declaration>() {

        @Override
        public String toString() {
            return "pDeclaration";
        }

        public Res<Ast.Declaration> par(Seq s) {
            Res<String> r = justkw.pa(s);
            if (r != null) {
                String word = r.v.toLowerCase();
                switch (word) {
                    case "begin":
                    case "end":
                        return null;
                    case "type":
                        return paTypeDefinition(s);
                    case "subtype":
                        return paSubTypeDeclaration(s);
                    case "procedure":
                        return paProcedureDefinitionOrDeclaration(s);
                    case "function":
                        return paFunctionDefinitionOrDeclaration(s);
                    case "pragma":
                        return paPragma(s);
                    case "cursor":
                        return paCursorDefinition(s);
                    default: ;
                }
            }
            // variable or exception declaration
            Res<Ast.Declaration> ritem = pItemDeclaration.pa(s);
            if (ritem != null) {
                return ritem;
            }
            return null;
        }
    };

    /*  
     (*
     create_package :
     CREATE ( OR kREPLACE )? PACKAGE ( schema_name=ID DOT )? package_name=ID
     ( invoker_rights_clause )?
     ( IS | AS ) ( declare_section )? END ( ID )? SEMI
     *)
     */
    public Res<List<Ast.Declaration>> paDeclarations(Seq s) {
        List<Ast.Declaration> res = new ArrayList<>();
        Seq seq = s;
        while (true) {
            Res<Ast.Declaration> r = pDeclaration.pa(seq);
            if (r == null) {
                return new Res<>(res, seq);
            }
            Res rs = c.mustp(c.pSemi, "expecting a ;").pa(r.next);
            res.add(r.v);
            seq = rs.next;
        }
    }

    /*
     fun pPackage s : package_spec result =
     tr(cseq6(kw "package",
     pObjectName,
     opt(pInvokerClause),
     pIsOrAs,
     pDeclareSection,
     seq3(kw "end",opt(pIdent),tSemi)),
     */
    public Res<Ast.PackageSpec> paPackageSpec(Seq s) {
        Res<String> r = c.forkw("package").pa(s);
        if (r == null) {
            return null;
        }
        Res<Ast.ObjectName> ro = paObjectName(r.next);
        if (ro == null) {
            return null;
        }
        Res<String> ric = c.opt(pInvokerClause).pa(ro.next);
        Res<String> risas = c.mustp(pIsOrAs, "is or as").pa(ric.next);
        Res<List<Ast.Declaration>> rde = paDeclarations(risas.next);
        Res<T3<String, Ast.Ident, String>> rend = c.seq3(c.forkw("end"), c.opt(pIdent), c.pSemi).pa(rde.next);
        must(rend, rde.next, "end [name] ;");
        return new Res<>(new Ast.PackageSpec(ro.v, rde.v, ric.v), rend.next);
    }

    public Pa<Ast.PackageSpec> pCRPackage = new Pa<Ast.PackageSpec>() {
        @Override
        public Res<Ast.PackageSpec> par(Seq s) {
            Res r = paCreateOrReplace(s);
            if (r == null) {
                return null;
            }
            return paPackageSpec(r.next);
        }
    };

    public Res<Ast.Statement> paStatement(Seq s) {
        Res<String> rlab = paLabel(s);
        Seq next;
        if (rlab == null) {
            next = s;
        } else {
            next = rlab.next;
        }
        Res<Ast.Statement> r = parseStatement(next);
        if (r == null || r.v.getStart() >= 0) {
            return r;
        } else {
            r.v.setRange(s.head().pos, r.next.head().ipos);
            return r;
        }
    }

    public final Pa<Ast.Statement> pStatement = new Pa<Ast.Statement>() {

        @Override
        protected Res<Statement> par(Seq s) {
            return paStatement(s);
        }
    };

    public Res<String> paLabel(Seq s) {
        Res r = c.pLabelStart.pa(s);
        if (r == null) {
            return null;
        }
        Res<String> rname = justkw.pa(r.next);
        Res r2 = c.pLabelEnd.pa(rname.next);
        return new Res<>(rname.v, r2.next);
    }

    public Res<Ast.Statement> parseStatement(Seq s) {
        Res<String> r = justkw.pa(s);
        if (r != null) {
            switch (r.v) {
                case "end":
                case "exception": // exception block begins
                case "when": // next exception list
                case "else": // next exception list
                case "elsif": // next exception list
                    return null;
                case "null":
                    return new Res<Ast.Statement>(new Ast.NullStatement(), r.next);
                case "savepoint":
                    return paSavePoint(s);
                case "rollback":
                    return paRollback(s);
                case "begin":
                    return paBlock_committed(s);
                case "declare":
                    return paBlock_committed(s);
                case "for":
                    return paForLoop(s);
                case "loop":
                    return paSimpleLoop_comitted(s);
                case "while":
                    return paWhileLoopStatement(s);
                case "case":
                    return paCaseStatement(s);
                case "raise":
                    return paRaiseStatement(s);
                case "return":
                    return paReturnStatement(s);
                case "open":
                    return paOpenStatement(s);
                case "close":
                    return paCloseStatement(s);
                case "if":
                    return paIfStatement(s);
                case "fetch":
                    return paFetchStatement(s);
                case "exit":
                    return paExitStatement(s);
                case "continue":
                    return paContinueStatement(s);
                case "pipe": /*row */

                    return paPipeRowStatement(s);
                case "execute": /* execute immediate */

                    return paExecuteImmediate(s);

                case "goto":
                    return paGotoStatement(s);
                case "forall":
                    return paForAllStatement(s);
                case "insert":
                case "update":
                case "delete":
                case "merge":
                case "select":
                // with q as (select * from dual) select dummy fromdual into bla from q:
                // is a valid select sql statement, to be exact we should check 
                // that with is not a procedure or variable name 
                case "with": // with q as (select * from dual) select dummy fromdual into bla from q:
                    return paSQLStatement(s);
            }
        }
        return paAssignOrCallStatement(s);
    }

    public Res<Ast.Statement> paSavePoint(Seq s) {
        Res<T2<String, Ast.Ident>> r = c.seq2(c.forkw("savepoint"), pIdent).pa(s);
        return new Res<Ast.Statement>(new Ast.Savepoint(r.v.f2), r.next);
    }

    public Res<Ast.Statement> paRollback(Seq s) {
        Res<T2<String, T2<String, Ast.Ident>>> r = c.seq2(c.forkw("rollback"), c.opt(c.seq2(c.forkw("to"), pIdent))).pa(s);
        if (r.v.f2 == null) {
            return new Res<Ast.Statement>(new Ast.Rollback(null), r.next);
        } else {
            return new Res<Ast.Statement>(new Ast.Rollback(r.v.f2.f2), r.next);
        }
    }

    Res<Ast.Statement> paGotoStatement(Seq s) {
        Res r = pkw_goto.pa(s);
        if (r == null) {
            return null;
        }
        Res<String> rs = c.mustp(justkw, "just a word").pa(r.next);
        return new Res<Ast.Statement>(new Ast.GotoStatement(rs.v), rs.next);
    }

    Res<Ast.Statement> paAssignOrCallStatement(Seq s) {
        Res<List<Ast.CallPart>> r = paCallParts(s);
        if (r == null) {
            return null;
        }
        Res r2 = c.pAssign.pa(r.next);
        if (r2 == null) {
            return new Res<Ast.Statement>(new Ast.ProcedureCall(r.v), r.next);
        } else {
            Res<Ast.Expression> re = c.mustp(pExpr, "expecting expression").pa(r2.next);
            return new Res<Ast.Statement>(new Ast.Assignment(new Ast.LValue(r.v), re.v), re.next);
        }
    }

    Res<List<Ast.Statement>> paStatementList(Seq s) {
        Res<Ast.Statement> r = paStatement(s);
        if (r == null) {
            return null;
        }
        List<Ast.Statement> l = new ArrayList<>();
        while (true) {
            Res<String> r2 = c.pSemi.pa(r.next);
            must(r2, r.next, "expecting semi colon");
            l.add(r.v);
            r = paStatement(r2.next);
            if (r == null) {
                return new Res<>(l, r2.next);
            }
        }
    }

    Pa<Ast.QualId> pQualId = new Pa<Ast.QualId>() {

        Pa<List<Ast.Ident>> pli = c.sep1(pIdent, c.pDot);

        @Override
        protected Res<Ast.QualId> par(Seq s) {
            Res<List<Ast.Ident>> r = pli.pa(s);
            if (r == null) {
                return null;
            }
            return new Res<>(new Ast.QualId(r.v), r.next);
        }
    };

    Res<Ast.ExceptionHandler> paOneException(Seq s) {
        Res<String> r = pkw_when.pa(s);
        if (r == null) {
            return null;
        }
        List<Ast.QualId> el;
        Res<String> r2 = pkw_others.pa(r.next);
        Seq next;
        if (r2 != null) {
            el = null;
            next = r2.next;
        } else {
            Res<List<Ast.QualId>> r3 = c.sep1(pQualId, pkw_or).pa(r.next);
            must(r3, r.next, "exception list with or");
            el = r3.v;
            next = r3.next;
        }
        Res<String> r4 = pkw_then.pa(next);
        must(r4, next, "expecting then");
        Res<List<Ast.Statement>> rs = paStatementList(r4.next);
        return new Res<>(new Ast.ExceptionHandler(el, rs.v), rs.next);
    }

    Res<Ast.ExceptionBlock> paExceptionBlockOption(Seq s) {
        Res<String> r = c.forkw("exception").pa(s);
        if (r == null) {
            return new Res<>(null, s);
        }
        List<Ast.ExceptionHandler> l = new ArrayList<>();
        s = r.next;
        while (true) {
            Res<Ast.ExceptionHandler> re = paOneException(s);
            if (re == null) {
                break;
            } else {
                l.add(re.v);
                s = re.next;
            }
        }
        if (l.isEmpty()) {
            throw new ParseException("expecting at least one exception  handler", s);
        }
        List<Ast.ExceptionHandler> l2 = new ArrayList<>();
        for (int i = 0; i < l.size() - 1; i++) {
            if (l.get(i).exceptions == null) {
                throw new ParseException("others must be last exception block", s);
            } else {
                l2.add(l.get(i));
            }
        }
        List<Ast.Statement> others;
        if (l.get(l.size() - 1).exceptions == null) {
            others = l.get(l.size() - 1).statements;
        } else {
            l2.add(l.get(l.size() - 1));
            others = null;
        }
        return new Res<>(new Ast.ExceptionBlock(l2, others), s);
    }

    Res<T2<List<Ast.Statement>, Ast.ExceptionBlock>> paBody(Seq s) {
        Res r = pkw_begin.pa(s);
        if (r == null) {
            return null;
        }
        Res<List<Ast.Statement>> rs = paStatementList(r.next);
        must(rs, r.next, "statement list");
        Res<Ast.ExceptionBlock> reb = paExceptionBlockOption(rs.next);
        Res rend = c.mustp(c.seq2(pkw_end, c.opt(pIdent)), "expect end").pa(reb.next);
        return new Res<>(new T2<>(rs.v, reb.v), rend.next);
    }

    Res<List<Ast.Declaration>> paDeclareSectionOption(Seq s) {
        Res<String> r = pkw_declare.pa(s);
        if (r == null) {
            return new Res<>(null, s);
        }
        return paDeclarations(r.next);
    }

    // this committed, since we already saw a declare or a begin
    Res<Ast.Statement> paBlock_committed(Seq s) {
        Res<List<Ast.Declaration>> rd = paDeclareSectionOption(s);
        Res<T2<List<Ast.Statement>, Ast.ExceptionBlock>> rse
                = paBody(rd.next);
        must(rse, rd.next, "expecting a begin");
//        Res rend = c.mustp(c.seq2(pkw_end, c.opt(pIdent)), "expect end").pa(rse.next);
        Ast.Block block = new Ast.Block(rd.v, rse.v.f1, rse.v.f2);
        return new Res<Ast.Statement>(new Ast.BlockStatement(block), rse.next);
    }

    Res<List<Ast.Statement>> paLoopBody_comitted(Seq s) {
        Res r1 = c.mustp(pkw_loop, "expecting loop").pa(s);
        Res<List<Ast.Statement>> r2 = paStatementList(r1.next);
        Res re = c.mustp(pkw_end_loop, "expecting end loop").pa(r2.next);
        Res re2 = c.opt(pIdent).pa(re.next);
        return new Res<>(r2.v, re2.next);
    }

    Res<Ast.Statement> paSimpleLoop_comitted(Seq s) {
        Res<List<Ast.Statement>> r2 = paLoopBody_comitted(s);
        return new Res<Ast.Statement>(new Ast.BasicLoopStatement(r2.v), r2.next);
    }

    Res<Ast.Statement> paWhileLoopStatement(Seq s) {
        Res r = pkw_while.pa(s);
        must(r, s, "expecting while");
        Res<Ast.Expression> re = pExpr.pa(r.next);
        must(re, r.next, "expecting an expression");
        Res<List<Ast.Statement>> rl = paLoopBody_comitted(re.next);
        return new Res<Ast.Statement>(new Ast.WhileLoopStatement(re.v, rl.v), rl.next);
    }

    Res<Ast.Statement> paForAllStatement(Seq s) {
        Res r = pkw_forall.pa(s);
        if (r == null) {
            return null;
        }
        Res<Ast.Ident> rident = pIdent.pa(r.next);
        Res rin = pkw_in.pa(rident.next);
        Res<T3<Ast.Expression, String, Ast.Expression>> rbounds = c.seq3(pExpr, c.pDotDot, pExpr).pa(rin.next);
        Res<List<Token>> rsql = paBalancedParenAndNoSemi(rbounds.next);
        return new Res<Ast.Statement>(new Ast.ForAllStatement(rident.v, new Ast.FromToBounds(rbounds.v.f1, rbounds.v.f3), rsql.v), rsql.next);

    }

    Res<Ast.Statement> paForLoop(Seq s) {
        Res r = pkw_for.pa(s);
        Res<Ast.Ident> rident = pIdent.pa(r.next);
        Res rin = pkw_in.pa(rident.next);
        {
            Res resel = c.seq2(c.pPOpen, pkw_select).pa(rin.next);
            if (resel == null) {
                resel = c.seq2(c.pPOpen, pkw_with).pa(rin.next);
            }
            if (resel != null) {
                // skip the starting paren !
                Res<List<Token>> rsql = paBalancedParenAndNoSemi(rin.next.tail());
                must(rsql, rin.next, "(select .... )");
                // eat the end paren
                Res pc = c.pPClose.pa(rsql.next);
                Res<List<Ast.Statement>> rl = paLoopBody_comitted(pc.next);
                return new Res<Ast.Statement>(new Ast.SelectLoopStatement(rident.v, rsql.v, rl.v), rl.next);
            }
        }
        {
            Res<Ast.Expression> rcp = pExpr.pa(rin.next);
            if (rcp != null) {
                if (pkw_loop.pa(rcp.next) != null) {
                    Res<List<Ast.Statement>> rl = paLoopBody_comitted(rcp.next);
                    return new Res<Ast.Statement>(new Ast.CursorLoopStatement(rident.v, rcp.v, rl.v), rl.next);
                }
            }
        }
        {
            Res<Boolean> rev = c.bopt(pkw_reverse).pa(rin.next);
            Res<T3<Ast.Expression, String, Ast.Expression>> rft
                    = c.seq3(pExpr, c.pDotDot, pExpr).pa(rev.next);
            if (rft != null) {
                Res<List<Ast.Statement>> rl = paLoopBody_comitted(rft.next);
                return new Res<Ast.Statement>(new Ast.FromToLoopStatement(rident.v, rev.v, rft.v.f1, rft.v.f3, rl.v), rl.next);
            }
        }
        throw new ParseException("can not parse for loop", s);
    }

    public Res<Ast.Statement> paCaseStatement(Seq s) {
        Res r = pkw_case.pa(s);
        if (r == null) {
            return null;
        }
        Res r2 = pkw_when.pa(r.next);
        Expression m;
        Seq next;
        if (r2 == null) {
            Res<Expression> re = pExpr.pa(r.next);
            m = re.v;
            next = re.next;
        } else {
            next = r.next;
            m = null;
        }

        List<Ast.ExprAndStatements> l = new ArrayList<>();
        //next is the loop variable
        while (true) {
            Res<String> rw = pkw_when.pa(next);
            if (rw == null) {
                break;
            }
            Res<Expression> rex = pExpr.pa(rw.next);
            Res<String> rt = pkw_then.pa(rex.next);
            Res<List<Ast.Statement>> rsl = paStatementList(rt.next);
            l.add(new Ast.ExprAndStatements(rex.v, rsl.v));
            next = rsl.next;
        }
        List<Ast.Statement> elsestmts;
        if (pkw_else.pa(next) != null) {
            Res relse = pkw_else.pa(next);
            Res<List<Ast.Statement>> rsl2 = paStatementList(relse.next);
            elsestmts = rsl2.v;
            next = rsl2.next;
        } else {
            elsestmts = null;
        }
        Res rend_case = pkw_end_case.pa(next);
        Res rend_case2 = c.opt(pIdent).pa(rend_case.next);
        if (m == null) {
            return new Res<Ast.Statement>(new Ast.CaseCondStatement(l, elsestmts), rend_case2.next);
        } else {
            return new Res<Ast.Statement>(new Ast.CaseMatchStatement(m, l, elsestmts), rend_case2.next);
        }
    }

    public Res<Ast.Statement> paRaiseStatement(Seq s) {
        Res r = pkw_raise.pa(s);
        if (r == null) {
            return null;
        }
        Res<Ast.QualId> ri = c.opt(pQualId).pa(r.next);
        return new Res<Ast.Statement>(new Ast.RaiseStatement(ri.v), ri.next);
    }

    public Res<Ast.Statement> paReturnStatement(Seq s) {
        Res r = pkw_return.pa(s);
        if (r == null) {
            return null;
        }
        Res<Ast.Expression> ri = c.opt(pExpr).pa(r.next);
        return new Res<Ast.Statement>(new Ast.ReturnStatement(ri.v), ri.next);
    }

    public Res<Ast.Statement> paOpenStatement(Seq s) {
        Res r = pkw_open.pa(s);
        if (r == null) {
            return null;
        }
        Res<Ast.QualId> rq = pQualId.pa(r.next);
        Ast.QualId qualid = rq.v;
        must(rq, r.next, "expecting qualid");
        Res rf = pkw_for.pa(rq.next);
        if (rf == null) {
            Res<List<Ast.ActualParam>> ra = paCallArgs(rq.next);
            if (ra == null) {
                return new Res<Ast.Statement>(new Ast.OpenFixedCursorStatement(rq.v, null), rq.next);
            } else {
                return new Res<Ast.Statement>(new Ast.OpenFixedCursorStatement(rq.v, ra.v), ra.next);
            }
        } else {
            // ref cursor 
            Res rselect = c.or2(pkw_select, pkw_with).pa(rf.next);
            if (rselect == null) {
                Res<Expression> rsql = pExpr.pa(rf.next);
                Res<T2<String, List<Ast.Expression>>> rusing
                        = c.opt(c.seq2(pkw_using, c.sep1(pExpr, c.pComma))).pa(rsql.next);
                List<Expression> usargs;
                if (rusing.v == null) {
                    usargs = null;
                } else {
                    usargs = rusing.v.f2;
                }
                return new Res<Ast.Statement>(new Ast.OpenDynamicRefCursorStatement(qualid, rsql.v, usargs), rusing.next);
            } else {
                Res<List<Token>> rsql = paBalancedParenAndNoSemi(s);
                return new Res<Ast.Statement>(new Ast.OpenStaticRefCursorStatement(qualid, rsql.v), rsql.next);
            }
        }
    }

    public Res<Ast.Statement> paCloseStatement(Seq s) {
        Res r = pkw_close.pa(s);
        if (r == null) {
            return null;
        }
        Res<Ast.QualId> rq = c.mustp(pQualId, "expecting qualid").pa(r.next);
        return new Res<Ast.Statement>(new Ast.CloseStatement(rq.v), rq.next);
    }

    public Res<Ast.Statement> paIfStatement(Seq s) {
        Res r = pkw_if.pa(s);
        if (r == null) {
            return null;
        }
        Res<Expression> re = pExpr.pa(r.next);
        Res rt = c.mustp(pkw_then, "expecting then").pa(re.next);
        Res<List<Ast.Statement>> rsl = paStatementList(rt.next);
        List<Ast.ExprAndStatements> l = new ArrayList<>();
        l.add(new Ast.ExprAndStatements(re.v, rsl.v));

        Seq next = rsl.next;
        //next is the loop variable
        while (true) {
            Res<String> rw = pkw_elsif.pa(next);
            if (rw == null) {
                break;
            }
            Res<Expression> rex = pExpr.pa(rw.next);
            Res<String> rt2 = pkw_then.pa(rex.next);
            Res<List<Ast.Statement>> rsl2 = paStatementList(rt2.next);
            l.add(new Ast.ExprAndStatements(rex.v, rsl2.v));
            next = rsl2.next;
        }
        List<Ast.Statement> elsestmts;
        if (pkw_else.pa(next) != null) {
            Res relse = pkw_else.pa(next);
            Res<List<Ast.Statement>> rsl2 = paStatementList(relse.next);
            must(rsl2, relse.next, "expecting statemnet list");
            elsestmts = rsl2.v;
            next = rsl2.next;
        } else {
            elsestmts = null;
        }
        Res rend_if = pkw_end_if.pa(next);
        return new Res<Ast.Statement>(new Ast.IfStatement(l, elsestmts), rend_if.next);
    }

    public Res<Ast.Statement> paFetchStatement(Seq s) {
        Res r = pkw_fetch.pa(s);
        if (r == null) {
            return null;
        }
        Res<Ast.QualId> rq = pQualId.pa(r.next);
        Res<Boolean> rbc = c.bopt(pkw_bulk_collect).pa(rq.next);
        Res rinto = pkw_into.pa(rbc.next);
        must(rinto, rq.next, "into");
        Res<List<Ast.LValue>> re = c.sep1(pLValue, c.pComma).pa(rinto.next);

        Res rlimit = pkw_limit.pa(re.next);
        if (rlimit == null) {
            return new Res<Ast.Statement>(new Ast.FetchStatement(rq.v, re.v, rbc.v, null), re.next);
        } else {
            Res<Ast.Expression> rlimitexpr = c.mustp(pExpr, "expression").pa(rlimit.next);
            return new Res<Ast.Statement>(
                    new Ast.FetchStatement(rq.v, re.v, rbc.v, rlimitexpr.v), rlimitexpr.next);
        }
    }

    public Res<Ast.Statement> paExitStatement(Seq s) {
        Res r = pkw_exit.pa(s);
        if (r == null) {
            return null;
        }
        Res<Ast.Ident> ri = c.opt(pIdent).pa(r.next);
        Res<T2<String, Ast.Expression>> rc = c.opt(c.seq2(pkw_when, pExpr)).pa(ri.next);
        Ast.Expression cond;
        if (rc.v == null) {
            cond = null;
        } else {
            cond = rc.v.f2;
        }
        return new Res<Ast.Statement>(new Ast.ExitStatement(ri.v, cond), rc.next);
    }

    public Res<Ast.Statement> paContinueStatement(Seq s) {
        Res r = pkw_continue.pa(s);
        if (r == null) {
            return null;
        }
        Res<Ast.Ident> ri = c.opt(pIdent).pa(r.next);
        Res<T2<String, Ast.Expression>> rc = c.seq2(pkw_when, pExpr).pa(ri.next);
        Ast.Expression cond;
        Seq next;
        if (rc == null) {
            cond = null;
            next = ri.next;
        } else {
            cond = rc.v.f2;
            next = rc.next;
        }
        return new Res<Ast.Statement>(new Ast.ContinueStatement(ri.v, cond), next);
    }

    public Res<Ast.Statement> paPipeRowStatement(Seq s) {
        Res r = pkw_pipe_row.pa(s);
        if (r == null) {
            return null;
        }
        Res<List<Ast.Expression>> re = c.sep1(pExpr, c.pComma).pa(r.next);
        must(re, r.next, "list of expressions");
        return new Res<Ast.Statement>(new Ast.PipeRowStatement(re.v), re.next);
    }

    public Pa<Ast.ExecuteImmediateParameter> pExecuteImmediateParameter
            = new Pa<Ast.ExecuteImmediateParameter>() {
                @Override
                public Res<Ast.ExecuteImmediateParameter> par(Seq s) {
                    Res<Ast.ParamMode> r = paParamModeOption(s);
                    if (r == null) {
                        return null;
                    }
                    if (r.v != null && r.v.nocopy) {
                        throw new ParseException("nocopy not allowed", s);
                    }
                    Res<Expression> rex = pExpr.pa(r.next);
                    if (r.v == null && rex == null) {
                        return null;
                    }
                    Ast.ParamModeType pt;
                    if (r.v == null) {
                        pt = Ast.ParamModeType.IN;
                    } else {
                        pt = r.v.paramModeType;
                    }
                    return new Res<>(new Ast.ExecuteImmediateParameter(pt, rex.v), rex.next);
                }
            };

    public Res<List<Ast.ExecuteImmediateParameter>> paUsingOption(Seq s) {
        Res r = pkw_using.pa(s);
        if (r == null) {
            return new Res<>(null, s);
        }
        Res<List<Ast.ExecuteImmediateParameter>> rexl = c.sep1(pExecuteImmediateParameter, c.pComma).pa(r.next);
        // or just return rexl ?
        return new Res<>(rexl.v, rexl.next);
    }

    public Res<Boolean> paIntoClause(Seq s) {
        Res r = pkw_bulk_collect.pa(s);
        if (r == null) {
            Res rinto = pkw_into.pa(s);
            if (rinto == null) {
                return null;
            }
            return new Res<>(false, rinto.next);
        } else {
            Res rinto = pkw_into.pa(r.next);
            if (rinto == null) {
                throw new ParseException("expecting into", s);
            }
            return new Res<>(true, rinto.next);
        }
    }

    public Res<Ast.Statement> paExecuteImmediate(Seq s) {
        Res r = pkw_execute_immediate.pa(s);
        if (r == null) {
            return null;
        }
        Res<Ast.Expression> rex = pExpr.pa(r.next);
        Res<Boolean> rinto = paIntoClause(rex.next);
        if (rinto != null) {
            Res<List<Ast.LValue>> rl = c.sep1(pLValue, c.pComma).pa(rinto.next);
            Res<List<Ast.ExecuteImmediateParameter>> rexl = paUsingOption(rl.next);
            return new Res<Ast.Statement>(new Ast.ExecuteImmediateInto(rex.v, rinto.v, rl.v, rexl.v), rexl.next);
        } else {
            // dml execute immediate mit returning clause
            Res<List<Ast.ExecuteImmediateParameter>> rexl = paUsingOption(rex.next);
            Res r_returning = pkw_returning.pa(rexl.next);
            if (r_returning == null) {
                return new Res<Ast.Statement>(new Ast.ExecuteImmediateDML(rex.v, rexl.v, null, null), rexl.next);
            } else {
                Res<List<Ast.Ident>> rc = c.sep1(pIdent, c.pComma).pa(r_returning.next);
                Res r_into = pkw_into.pa(rc.next);
                Res<List<Ast.LValue>> rlv = c.sep1(pLValue, c.pComma).pa(r_into.next);
                return new Res<Ast.Statement>(new Ast.ExecuteImmediateDML(rex.v, rexl.v, rc.v, rlv.v), rlv.next);
            }
        }
    }

    /*
     and pProcedureDefinitionOrDeclaration s =
     bind(pProcedureHeading,
     fn h => orn[tr(seq2(pIsOrAs,
     orn[tr(seq2(pBodyDeclareSection,pBody),
     fn (d,(b,e)) =>  ProcedureDefinition (h, Block (d,b,e))),
     tr(pLang,fn(name,str) => LProcedureDefinition (h,name,str))]),#2),
     tr(nix,fn () =>  ProcedureDeclaration h)]) s



     and pFunctionDefinitionOrDeclaration s =
     bind(pFunctionHeading,
     fn h => orn[tr(cseq2(pIsOrAs,
     orn[tr(seq2(pBodyDeclareSection,pBody),
     fn(d,(b,e)) =>  FunctionDefinition (h,Block (d,b,e))),
     tr(pLang,fn(name,str) => LFunctionDefinition (h,name,str))]),#2),
     tr(nix,fn () => FunctionDeclaration h)]) s
                  
     */

    /*
     aDeclarations(r.next);
     }

     // this committed, since we already saw a declare or a begin
     Res<Ast.Statement> paBlock_committed(Seq s) {
     Res<List<Ast.Declaration>> rd = paDeclareSectionOption(s);
     Res<T2<List<Ast.Statement>, Ast.ExceptionBlock>> rse
     = paBody(rd.next);
     must(rse, rd.next, "expecting a begin");
     Res rend = c.mustp(c.seq2(pkw_end, c.opt(pIdent)), "expect end").pa(rse.next);
     return new Res<>(new Ast.PLSQLBlock(rd.v, rse.v.f1, rse.v.f2), rend.next);
     }
     */
    public Res< Ast.Block> paProcOrFunBody(Seq s) {
        Res<List<Ast.Declaration>> rd = paDeclarations(s);
        Res<T2<List<Ast.Statement>, Ast.ExceptionBlock>> rse
                = paBody(rd.next);
        must(rse, rd.next, "expecting a begin");
        //Res rend = c.mustp(c.seq2(pkw_end, c.opt(pIdent)), "expect end").pa(rse.next);
        return new Res<>(new Ast.Block(rd.v, rse.v.f1, rse.v.f2), rse.next);
    }

    public Res<Ast.Declaration> paProcedureDefinitionOrDeclaration(Seq s) {
        Res<Ast.ProcedureHeading> r = paProcedureHeading(s);
        if (r == null) {
            return null;
        }
        Res r_is_or_as = pIsOrAs.pa(r.next);
        if (r_is_or_as == null) {
            return new Res<Ast.Declaration>(new Ast.ProcedureDeclaration(r.v), r.next);
        }
        if (pkw_language.pa(r_is_or_as.next) != null) {
            Res<T2<String, String>> r3 = pLang.pa(r_is_or_as.next);
            must(r3, r_is_or_as.next, "java proc");
            return new Res<Ast.Declaration>(new Ast.ExtProcedureDefinition(r.v, r3.v.f1, r3.v.f2), r3.next);
        }
        Res<Ast.Block> rb = paProcOrFunBody(r_is_or_as.next);
        return new Res<Ast.Declaration>(new Ast.ProcedureDefinition(r.v, rb.v), rb.next);
    }

    public Res<Ast.Declaration> paFunctionDefinitionOrDeclaration(Seq s) {
        Res<Ast.FunctionHeading> r = paFunctionHeading(s);
        if (r == null) {
            return null;
        }
        Res r_is_or_as = pIsOrAs.pa(r.next);
        if (r_is_or_as == null) {
            return new Res<Ast.Declaration>(new Ast.FunctionDeclaration(r.v), r.next);
        }
        if (pkw_language.pa(r_is_or_as.next) != null) {
            Res<T2<String, String>> r3 = pLang.pa(r_is_or_as.next);
            must(r3, r_is_or_as.next, "java proc");
            return new Res<Ast.Declaration>(new Ast.ExtFunctionDefinition(r.v, r3.v.f1, r3.v.f2), r3.next);
        }
        Res<Ast.Block> rb = paProcOrFunBody(r_is_or_as.next);
        return new Res<Ast.Declaration>(new Ast.FunctionDefinition(r.v, rb.v), rb.next);
    }

    /*
     fun pPackageBody s = 
     tr(cseq5(seq3(kw "package",kw "body",pObjectName),
     pIsOrAs,
     pBodyDeclareSection,   
     opt(pPackBodyBlock),
     cseq3(kw "end",opt(pIdent),tSemi)),
     fn ((_,_,name),_,decls,body,_) => 
     PackageBody {packageName=name, declarations=decls, body=body})
     s
     */
    public Pa<Ast.PackageBody> pPackageBody = new Pa<Ast.PackageBody>() {

        @Override
        protected Res<Ast.PackageBody> par(Seq s) {
            Res r = pkw_package_body.pa(s);
            if (r == null) {
                return null;
            }
            Res<Ast.ObjectName> rn = paObjectName(r.next);
            Res r2 = c.mustp(pIsOrAs, "is or as").pa(rn.next);
            Res<List<Ast.Declaration>> rdecls = paDeclarations(r2.next);
            if (pkw_begin.pa(rdecls.next) == null) {
                Res rend = c.mustp(c.seq2(pkw_end, c.opt(pIdent)), "expect end").pa(rdecls.next);
                Res rsemi = c.mustp(c.pSemi, "semi").pa(rend.next);
                return new Res<>(new Ast.PackageBody(rn.v, rdecls.v, null, null), rsemi.next);
            } else {
                Res<T2<List<Ast.Statement>, Ast.ExceptionBlock>> rb = paBody(rdecls.next);
                Res rsemi = c.mustp(c.pSemi, "semi").pa(rb.next);
                return new Res<>(new Ast.PackageBody(rn.v, rdecls.v, rb.v.f1, rb.v.f2), rsemi.next);
            }
        }

    };

    public Pa<Ast.PackageBody> pCRPackageBody = new Pa<Ast.PackageBody>() {
        @Override
        public Res<Ast.PackageBody> par(Seq s) {
            Res r = paCreateOrReplace(s);
            if (r == null) {
                return null;
            }
            return pPackageBody.pa(r.next);
        }
    };

    public T2<Ast.PackageSpec, Ast.PackageBody> paCRPackageSpecAndBody(Seq s) {
        Res<Ast.PackageSpec> rs = pCRPackage.pa(s);
        Res rslash = c.token(TokenType.Div).pa(rs.next);
        must(rslash, rs.next, "slash");
        Res<Ast.PackageBody> rb = pCRPackageBody.pa(rslash.next);
        Res rslash2 = c.token(TokenType.Div).pa(rb.next);
        must(rslash2, rb.next, "slash");
        return new T2<>(rs.v, rb.v);
    }
}
