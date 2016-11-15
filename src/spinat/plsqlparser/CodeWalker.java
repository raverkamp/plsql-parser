package spinat.plsqlparser;

import java.util.ArrayList;
import java.util.List;

public class CodeWalker {

    // fixme: the package contents are just a block, the statemnets might be missing
    public void walkPackageBody(Ast.PackageBody b) {
        walkDeclarations(b.declarations);
        // ok, for now package the body statements up into a block
        if (b.statements != null && b.statements.size() > 0) {
            Ast.Block block = new Ast.Block(new ArrayList<Ast.Declaration>(), b.statements, b.exceptionBlock);
            walkBlock(block);
        }
    }

    public void walkPackageSpec(Ast.PackageSpec b) {
        walkDeclarations(b.declarations);
    }

    public void walkProcedureDefinition(Ast.ProcedureDefinition b) {
        walkBlock(b.block);
    }

    public void walkFunctionDefinition(Ast.FunctionDefinition b) {
        walkBlock(b.block);
    }

    void walkDeclarations(List<Ast.Declaration> d) {
        if (d != null) {
            for (Ast.Declaration decl : d) {
                walkDeclaration(decl);
            }
        }
    }

    void walkActualParams(List<Ast.ActualParam> ps) {
        for (Ast.ActualParam p : ps) {
            walkExpression(p.expr);
        }
    }

    void walkCallParts(List<Ast.CallPart> cps) {
        for (Ast.CallPart cp : cps) {
            if (cp instanceof Ast.Component) {
                // nix
            } else if (cp instanceof Ast.CallOrIndexOp) {
                Ast.CallOrIndexOp o = (Ast.CallOrIndexOp) cp;
                walkActualParams(o.params);
            }
        }
    }

    // extract the sql%something expressions from an expression
    // not finished !
    public void walkExpression(Ast.Expression expr) {
        if (expr == null) {
            return;
        }

        if (expr instanceof Ast.OrExpr) {
            for (Ast.Expression e : ((Ast.OrExpr) expr).exprs) {
                walkExpression(e);
            }
        } else if (expr instanceof Ast.AndExpr) {
            for (Ast.Expression e : ((Ast.AndExpr) expr).exprs) {
                walkExpression(e);
            }
        } else if (expr instanceof Ast.CompareExpr) {
            Ast.CompareExpr e = (Ast.CompareExpr) expr;
            walkExpression(e.expr1);
            walkExpression(e.expr2);
        } else if (expr instanceof Ast.NotExpr) {
            Ast.NotExpr e = (Ast.NotExpr) expr;
            walkExpression(e.expr);
        } else if (expr instanceof Ast.ParenExpr) {
            Ast.ParenExpr e = (Ast.ParenExpr) expr;
            walkExpression(e.expr);
        } else if (expr instanceof Ast.BinopExpression) {
            Ast.BinopExpression e = (Ast.BinopExpression) expr;
            walkExpression(e.expr1);
            walkExpression(e.expr2);
        } else if (expr instanceof Ast.CaseBoolExpression) {
            Ast.CaseBoolExpression e = (Ast.CaseBoolExpression) expr;
            walkExpression(e.default_);
            for (Ast.CaseExpressionPart o : e.cases) {
                walkExpression(o.cond);
                walkExpression(o.result);
            }
        } else if (expr instanceof Ast.CaseMatchExpression) {
            Ast.CaseMatchExpression e = (Ast.CaseMatchExpression) expr;
            walkExpression(e.default_);
            for (Ast.CaseExpressionPart o : e.matches) {
                walkExpression(o.cond);
                walkExpression(o.result);
            }
        } else if (expr instanceof Ast.CastExpression) {
            Ast.CastExpression e = (Ast.CastExpression) expr;
            walkExpression(e.expr);
        } else if (expr instanceof Ast.ExtractDatePart) {
            Ast.ExtractDatePart e = (Ast.ExtractDatePart) expr;
            walkExpression(e.expr);
        } else if (expr instanceof Ast.InExpression) {
            Ast.InExpression e = (Ast.InExpression) expr;
            walkExpression(e.expr);
            for (Ast.Expression es : e.set) {
                walkExpression(es);
            }
        } else if (expr instanceof Ast.IsNullExpr) {
            Ast.IsNullExpr e = (Ast.IsNullExpr) expr;
            walkExpression(e.expr);
        } else if (expr instanceof Ast.LValue) {
            // yes! a(sql%rowcount) := true   .... sick
            Ast.LValue e = (Ast.LValue) expr;
            walkCallParts(e.callparts);
        } else if (expr instanceof Ast.LikeExpression) {
            Ast.LikeExpression e = (Ast.LikeExpression) expr;
            walkExpression(e.escape);
            walkExpression(e.expr1);
            walkExpression(e.expr2);
        } else if (expr instanceof Ast.MultisetExpr) {
            Ast.MultisetExpr e = (Ast.MultisetExpr) expr;
            walkExpression(e.e1);
            walkExpression(e.e2);
        } else if (expr instanceof Ast.NewExpression) {
            Ast.NewExpression e = (Ast.NewExpression) expr;
            walkCallParts(e.callParts);
        } else if (expr instanceof Ast.UnaryMinusExpression) {
            Ast.UnaryMinusExpression e = (Ast.UnaryMinusExpression) expr;
            walkExpression(e.expr);
        } else if (expr instanceof Ast.UnaryPlusExpression) {
            Ast.UnaryPlusExpression e = (Ast.UnaryPlusExpression) expr;
            walkExpression(e.expr);
        } else if (expr instanceof Ast.VarOrCallExpression) {
            Ast.VarOrCallExpression e = (Ast.VarOrCallExpression) expr;
            walkCallParts(e.callparts);
        } else if (expr instanceof Ast.BetweenExpression) {
            Ast.BetweenExpression be = (Ast.BetweenExpression) expr;
            walkExpression(be.expr);
            walkExpression(be.lower);
            walkExpression(be.upper);
        } else if (expr instanceof Ast.CString
                || expr instanceof Ast.CBool
                || expr instanceof Ast.CNumber
                || expr instanceof Ast.CDate
                || expr instanceof Ast.CNull
                || expr instanceof Ast.DollarDollar
                || expr instanceof Ast.CursorAttribute
                || expr instanceof Ast.CInterval
                || expr instanceof Ast.SqlAttribute) {
            // nothing to do                           
        } else {
            // throw new RuntimeException("missing check for expression type " + expr.getClass());
        }
    }

    public void walkExceptionDeclaration(Ast.ExceptionDeclaration ed) {
    }

    public void walkTypeDeclaration(Ast.TypeDeclaration ed) {
    }

    public void walkProcedureDeclaration(Ast.ProcedureDeclaration ed) {
    }

    public void walkFunctionDeclaration(Ast.FunctionDeclaration ed) {
    }

    public void walkExtProcedureDefinition(Ast.ExtProcedureDefinition ed) {
    }

    public void walkExtFunctionDefinition(Ast.ExtFunctionDefinition d) {
    }

    public void walkCursorDefinition(Ast.CursorDefinition ed) {
    }
    
    public void walkSimplePragma(Ast.SimplePragma ed) {
    }

    public void walkDeclaration(Ast.Declaration d) {

        if (d instanceof Ast.ProcedureDefinition) {
            walkProcedureDefinition((Ast.ProcedureDefinition) d);
        } else if (d instanceof Ast.FunctionDefinition) {
            walkFunctionDefinition((Ast.FunctionDefinition) d);
        } else if (d instanceof Ast.VariableDeclaration) {
            Ast.VariableDeclaration vd = (Ast.VariableDeclaration) d;
            walkExpression(vd.default_);
        } else if (d instanceof Ast.ExceptionDeclaration) {
            walkExceptionDeclaration((Ast.ExceptionDeclaration) d);
        } else if (d instanceof Ast.TypeDeclaration) {
            walkTypeDeclaration((Ast.TypeDeclaration) d);
        } else if (d instanceof Ast.ProcedureDeclaration) {
            walkProcedureDeclaration((Ast.ProcedureDeclaration) d);
        } else if (d instanceof Ast.FunctionDeclaration) {
            walkFunctionDeclaration((Ast.FunctionDeclaration) d);
        } else if (d instanceof Ast.CursorDefinition) {
            walkCursorDefinition((Ast.CursorDefinition) d);
        } else if (d instanceof Ast.ExtProcedureDefinition) {
            walkExtProcedureDefinition((Ast.ExtProcedureDefinition) d);
        } else if (d instanceof Ast.ExtFunctionDefinition) {
            walkExtFunctionDefinition((Ast.ExtFunctionDefinition) d);
        } else if (d instanceof Ast.SimplePragma) {
            walkSimplePragma((Ast.SimplePragma) d);
        } else {
            throw new RuntimeException("can not walk " + d.toString());
        }
    }

    public void walkStatement(Ast.Statement s) {
        if (s == null) {
            return;
        }

        if (s instanceof Ast.BlockStatement) {
            Ast.BlockStatement bs = (Ast.BlockStatement) s;
            walkBlock(bs.block);
        } else if (s instanceof Ast.IfStatement) {
            Ast.IfStatement ifs = (Ast.IfStatement) s;
            for (Ast.ExprAndStatements it : ifs.branches) {
                walkStatements(it.statements);
                walkExpression(it.expr);
            }
            if (ifs.elsebranch != null) {
                walkStatements(ifs.elsebranch);
            }
        } else if (s instanceof Ast.CaseCondStatement) {
            Ast.CaseCondStatement cs = (Ast.CaseCondStatement) s;
            for (Ast.ExprAndStatements ct : cs.branches) {
                walkStatements(ct.statements);
                walkExpression(ct.expr);
            }
            if (cs.defaultbranch != null) {
                walkStatements(cs.defaultbranch);
            }
        } else if (s instanceof Ast.CaseMatchStatement) {
            Ast.CaseMatchStatement cs = (Ast.CaseMatchStatement) s;
            for (Ast.ExprAndStatements ct : cs.branches) {
                walkStatements(ct.statements);
            }
            if (cs.defaultbranch != null) {
                walkStatements(cs.defaultbranch);
            }
        } else if (s instanceof Ast.BasicLoopStatement) {
            Ast.BasicLoopStatement sl = (Ast.BasicLoopStatement) s;
            walkStatements(sl.statements);
        } else if (s instanceof Ast.WhileLoopStatement) {
            Ast.WhileLoopStatement wl = (Ast.WhileLoopStatement) s;
            walkStatements(wl.statements);
            walkExpression(wl.condition);
        } else if (s instanceof Ast.FromToLoopStatement) {
            Ast.FromToLoopStatement fs = (Ast.FromToLoopStatement) s;
            walkStatements(fs.statements);
            walkExpression(fs.from);
            walkExpression(fs.to);
        } else if (s instanceof Ast.CursorLoopStatement) {
            Ast.CursorLoopStatement fs = (Ast.CursorLoopStatement) s;
            walkStatements(fs.statements);
        } else if (s instanceof Ast.SelectLoopStatement) {
            Ast.SelectLoopStatement fs = (Ast.SelectLoopStatement) s;
            walkStatements(fs.statements);
        } else if (s instanceof Ast.Assignment) {
            Ast.Assignment as = (Ast.Assignment) s;
            walkExpression(as.expression);
            // check for callparts
        } else if (s instanceof Ast.ReturnStatement) {
            Ast.ReturnStatement x = (Ast.ReturnStatement) s;
            walkExpression(x.expr);
        } else if (s instanceof Ast.ExitStatement) {
            Ast.ExitStatement x = (Ast.ExitStatement) s;
            walkExpression(x.condition);
        } else if (s instanceof Ast.ProcedureCall) {
            Ast.ProcedureCall pc = (Ast.ProcedureCall) s;
            for (Ast.CallPart cp : pc.callparts) {
                if (cp instanceof Ast.CallOrIndexOp) {
                    for (Ast.ActualParam ap : ((Ast.CallOrIndexOp) cp).params) {
                        walkExpression(ap.expr);
                    }
                }
            }
        } else if (s instanceof Ast.ExecuteImmediateDML) {
            // fixme
        } else if (s instanceof Ast.ForAllStatement) {
            Ast.ForAllStatement fa = (Ast.ForAllStatement) s;
            if (fa.bounds instanceof Ast.FromToBounds) {
                Ast.FromToBounds ftb = (Ast.FromToBounds) fa.bounds;
                walkExpression(ftb.from);
                walkExpression(ftb.to);
            } else if (fa.bounds instanceof Ast.ValuesBounds) {
                walkExpression(((Ast.ValuesBounds) fa.bounds).collection);
            } else if (fa.bounds instanceof Ast.IndicesBounds) {
                Ast.IndicesBounds ib = (Ast.IndicesBounds) fa.bounds;
                walkExpression(ib.idx_collection);
                walkExpression(ib.lower);
                walkExpression(ib.upper);
            }
        } else if (s instanceof Ast.OpenFixedCursorStatement) {
            Ast.OpenFixedCursorStatement o = (Ast.OpenFixedCursorStatement) s;
            if (o.params != null) {
                for (Ast.ActualParam ap : o.params) {
                    walkExpression(ap.expr);
                }
            }
        } else if (s instanceof Ast.PipeRowStatement) {
            Ast.PipeRowStatement ps = (Ast.PipeRowStatement) s;
            for (Ast.Expression e : ps.expressions) {
                walkExpression(e);
            }
        } else if (s instanceof Ast.OpenDynamicRefCursorStatement) {
            Ast.OpenDynamicRefCursorStatement rs = (Ast.OpenDynamicRefCursorStatement) s;
            walkExpression(rs.sqlexpr);
            if (rs.bindargs != null) {
                for (Ast.Expression e : rs.bindargs) {
                    walkExpression(e);
                }
            }
        } else if (s instanceof Ast.ExecuteImmediateInto) {
            Ast.ExecuteImmediateInto eit = (Ast.ExecuteImmediateInto) s;
            walkExpression(eit.sqlexpr);
            if (eit.usingparameters != null) {
                for (Ast.ExecuteImmediateParameter p : eit.usingparameters) {
                    walkExpression(p.value);
                }
            }
        } else // no simple else clause to make sire we catch everything
        if (s instanceof Ast.NullStatement
                || s instanceof Ast.Rollback
                || s instanceof Ast.ContinueStatement
                || s instanceof Ast.SqlStatement
                || s instanceof Ast.CloseStatement
                || s instanceof Ast.OpenStaticRefCursorStatement
                || s instanceof Ast.FetchStatement
                || s instanceof Ast.CloseStatement
                || s instanceof Ast.RaiseStatement
                || s instanceof Ast.GotoStatement
                || s instanceof Ast.Savepoint) {
            // nothing to do
        } else {
            throw new RuntimeException("no check for statement type " + s.getClass());
        }
    }

    public final void walkStatements(List<Ast.Statement> s) {
        for (Ast.Statement stm : s) {
            if (stm == null) {
            }
            walkStatement(stm);
        }
    }

    public void walkBlock(Ast.Block block) {
        walkDeclarations(block.declarations);
        walkStatements(block.statements);
        if (block.exceptionBlock != null) {
            for (Ast.ExceptionHandler ec : block.exceptionBlock.handlers) {
                walkStatements(ec.statements);
            }
            if (block.exceptionBlock.othershandler != null) {
                walkStatements(block.exceptionBlock.othershandler);
            }
        }
    }

}
