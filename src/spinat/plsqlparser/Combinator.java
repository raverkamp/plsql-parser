package spinat.plsqlparser;

import java.util.ArrayList;
import java.util.List;

public class Combinator {

    public <X> Pa<X> mustp(final Pa<X> p, final String errm) {

        return new Pa<X>() {

            public String toString() {
                return "mustp(" + p.toString() + ")";
            }

            @Override
            public Res<X> par(Seq s) {
                Res<X> r = p.pa(s);
                if (r == null) {
                    throw new ParseException(errm, s);
                } else {
                    return r;
                }
            }
        };
    }

    public <X, Y> Pa<Y> commit(final Pa<X> p1, final Pa<Y> p2) {
        return new Pa<Y>() {
            public String toString() {
                return "commit(" + p1 + "->" + p2 + ")";
            }

            @Override
            public Res<Y> par(Seq s) {
                Res<X> r = p1.pa(s);
                if (r == null) {
                    return null;
                }
                Res<Y> r2 = p2.pa(r.next);
                if (r2 == null) {
                    throw new ParseException("expecting a " + p2, r.next);
                }
                return r2;
            }

        };
    }

    public <X, Y> Pa<T2<X, Y>> seq2(final Pa<X> p1, final Pa<Y> p2) {
        return new Pa<T2<X, Y>>() {

            public String toString() {
                return "seq2(" + p1 + ", " + p2 + ")";
            }

            @Override
            public Res<T2<X, Y>> par(Seq s) {
                Res<X> r1 = p1.pa(s);
                if (r1 == null) {
                    return null;
                } else {
                    Res<Y> r2 = p2.pa(r1.next);
                    if (r2 == null) {
                        return null;
                    } else {
                        return new Res<T2<X, Y>>(new T2<X, Y>(r1.v, r2.v), r2.next);
                    }
                }

            }
        };
    }

    public <X> Pa<X> opt(final Pa<X> p) {

        return new Pa<X>() {

            public String toString() {
                return "opt(" + p + ")";
            }

            @Override
            public Res<X> par(Seq s) {
                Res<X> r = p.pa(s);
                if (r == null) {
                    return new Res<>(null, s);
                } else {
                    return r;
                }
            }
        };
    }

    public <X> Pa<Boolean> bopt(final Pa<X> p) {

        return new Pa<Boolean>() {

            public String toString() {
                return "bopt(" + p + ")";
            }

            public Res<Boolean> par(Seq s) {
                Res<X> r = p.pa(s);
                if (r == null) {
                    return new Res<>(false, s);
                } else {
                    return new Res<>(true, r.next);
                }
            }
        };
    }

    public Pa<String> token(final TokenType tt) {
        return new Pa<String>() {

            public String toString() {
                return "token(" + tt + ")";
            }

            @Override
            public Res<String> par(Seq s) {
                if (s.head().ttype == tt) {
                    return new Res<>(s.head().str, s.tail());
                } else {
                    return null;
                }
            }
        };
    }

    public final Pa<String> pPOpen = token(TokenType.LParen);
    public final Pa<String> pPClose = token(TokenType.RParen);
    public final Pa<String> pComma = token(TokenType.Comma);
    public final Pa<String> pSemi = token(TokenType.Semi);
    public final Pa<String> pPower = token(TokenType.Power);
    public final Pa<String> pPercent = token(TokenType.Percent);
    public final Pa<String> pArrow = token(TokenType.Arrow);
    public final Pa<String> pDot = token(TokenType.Dot);
    public final Pa<String> pDotDot = token(TokenType.DotDot);
    public final Pa<String> pAssign = token(TokenType.Assign);
    public final Pa<String> pLabelStart = token(TokenType.LabelStart);
    public final Pa<String> pLabelEnd = token(TokenType.LabelEnd);

    public Pa<String> forkw(final String kw) {
        return new Pa<String>() {

            @Override
            public Res<String> par(Seq s) {
                if (s.head().ttype == TokenType.Ident && s.head().str.equalsIgnoreCase(kw)) {
                    return new Res<>(s.head().str, s.tail());
                } else {
                    return null;
                }
            }

            @Override
            public String toString() {
                return "forkw(" + kw + ")";
            }
        };
    }

    public Pa<String> forkw2(final String kw1, final String kw2) {
        final String x = kw1 + "/" + kw2;
        return new Pa<String>() {

            @Override
            public Res<String> par(Seq s) {
                if (s.head().ttype == TokenType.Ident && s.head().str.equalsIgnoreCase(kw1)) {
                    Token t = s.tail().head();
                    if (t.ttype == TokenType.Ident && t.str.equalsIgnoreCase(kw2)) {
                        return new Res<>(x, s.tail().tail());
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }

            public String toString() {
                return "forkw2(" + x + ")";
            }
        };
    }

    public <X> Pa<List<X>> many(final Pa<X> pa) {
        return new Pa<List<X>>() {
            @Override
            public Res<List<X>> par(Seq s) {
                List<X> l = new ArrayList<>();
                Seq next = s;
                while (true) {
                    Res<X> r = pa.pa(next);
                    if (r == null) {
                        return new Res<>(l, next);
                    }
                    l.add(r.v);
                    next = r.next;
                }
            }
        };
    }

    public <X> Pa<List<X>> sep(final Pa<X> pa, final Pa pasep) {
        return new Pa<List<X>>() {
            @Override
            public Res<List<X>> par(Seq s) {
                ArrayList<X> l = new ArrayList<>();
                Res<X> r = pa.pa(s);
                if (r == null) {
                    return new Res<List<X>>(l, s);
                } else {
                    l.add(r.v);
                }
                s = r.next;
                while (true) {
                    Res rr = pasep.pa(s);
                    if (rr == null) {
                        return new Res<List<X>>(l, s);
                    }
                    Res<X> rr2 = pa.pa(rr.next);
                    if (rr2 == null) {
                        throw new ParseException("expecteing one more thing", rr.next);
                    }
                    s = rr2.next;
                }
            }

            public String toString() {
                return "sep(" + pa + "," + pasep + ")";
            }
        };
    }

    public <X> Pa<List<X>> sep1(final Pa<X> pa, final Pa pasep) {
        final Pa<List<X>> pa1 = sep(pa, pasep);
        return new Pa<List<X>>() {
            @Override
            public Res<List<X>> par(Seq s) {
                Res<List<X>> r = pa1.pa(s);
                if (r == null || r.v.isEmpty()) {
                    return null;
                } else {
                    return r;
                }
            }

            public String toString() {
                return "sep1(" + pa + "," + pasep + ")";
            }
        };

    }

    public <X> Res<X> withParensCommit(final Pa<X> pa, Seq s) {
        Res<String> r = pPOpen.pa(s);
        if (r == null) {
            return null;
        }
        Res<X> r2 = pa.pa(r.next);
        if (r2 == null) {
            throw new ParseException("expecting to parse " + pa, s);
        }
        Res<String> r3 = pPClose.pa(r2.next);
        if (r3 == null) {
            throw new ParseException("expecting to parse ')' ", r2.next);
        }
        return new Res<>(r2.v, r3.next);
    }

    public <X, Y, Z> Pa<T3<X, Y, Z>> seq3(final Pa<X> px, final Pa<Y> py, final Pa<Z> pz) {
        return new Pa<T3<X, Y, Z>>() {
            @Override
            public Res<T3<X, Y, Z>> par(Seq s) {
                Res<X> rx = px.pa(s);
                if (rx == null) {
                    return null;
                }
                Res<Y> ry = py.pa(rx.next);
                if (ry == null) {
                    return null;
                }
                Res<Z> rz = pz.pa(ry.next);
                if (rz == null) {
                    return null;
                }
                return new Res<>(new T3<X, Y, Z>(rx.v, ry.v, rz.v), rz.next);
            }
        };
    }

    public <A, B, C, D> Pa<T4<A, B, C, D>> seq4(final Pa<A> px, final Pa<B> py, final Pa<C> pz, final Pa<D> pu) {
        return new Pa<T4<A, B, C, D>>() {
            @Override
            public Res<T4<A, B, C, D>> par(Seq s) {
                Res<A> rx = px.pa(s);
                if (rx == null) {
                    return null;
                }
                Res<B> ry = py.pa(rx.next);
                if (ry == null) {
                    return null;
                }
                Res<C> rz = pz.pa(ry.next);
                if (rz == null) {
                    return null;
                }
                Res<D> ru = pu.pa(rz.next);
                if (ru == null) {
                    return null;
                }
                return new Res<>(new T4<A, B, C, D>(rx.v, ry.v, rz.v, ru.v), ru.next);
            }
        };
    }

    public <X> Pa<X> orn(final Pa[] pas) {
        if (pas.length == 0) {
            throw new RuntimeException("empty array for orn");
        }
        return new Pa<X>() {

            @Override
            public Res<X> par(Seq s) {
                for (Pa p : pas) {
                    @SuppressWarnings("unchecked")
                    Pa<X> p2 = (Pa<X>) p;
                    Res<X> r = p2.pa(s);
                    if (r != null) {
                        return r;
                    }
                }
                return null;
            }
        };
    }

    public <X> Pa<X> or2(Pa<X> p1, Pa<X> p2) {
        return orn(new Pa[]{p1, p2});
    }
}
