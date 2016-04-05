package spinat.plsqlparser;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author rav
 */
public class TestParser {

    public TestParser() {
    }

    Seq scan(String s) {
        ArrayList<Token> a = Scanner.scanAll(s);
        ArrayList<Token> r = new ArrayList<>();
        for (Token t : a) {
            if (Scanner.isRelevant(t)) {
                r.add(t);
            }
        }
        return new Seq(r);
    }

    public void tpa(Pa p, String s) {
        Seq seq = scan(s);
        Res r = p.pa(seq);
        assertNotNull(r);
        System.out.println(r.v);
        assertTrue(r.next.head().ttype == TokenType.TheEnd || r.next.head().ttype == TokenType.Div);
    }

    @Test
    public void test1() {
        String s = "1+2";
        Seq se = scan(s);
        Parser p = new Parser();
        Res<Ast.Expression> r = p.paAtomExpr(se);
        System.out.println(r.v);
        Res<Ast.Expression> r2 = p.paExpr(se);
        System.out.println(r2.v);
    }

    @Test
    public void test2() {
        Parser p = new Parser();
        tpa(p.pExpr, "a");
        tpa(p.pExpr, "\"a\"");
        tpa(p.pExpr, "a.b");
        tpa(p.pExpr, "1+2");
        tpa(p.pExpr, "1.9+0.8");
        tpa(p.pExpr, "a(b)");
        tpa(p.pExpr, "a+b");
        tpa(p.pExpr, "a.i+b*x");
        tpa(p.pExpr, "sin(a+k*10)");
        tpa(p.pExpr, "12**88");
        tpa(p.pExpr, "a%b");
        tpa(p.pExpr, "a.b.c(f=>12 > 5) +4");
        tpa(p.pExpr, "a.b.c(f=>12 > 5) + case 1 when 1 then 2 else 4 end");
        tpa(p.pExpr, "case when 1 then 2 else 4 end");
        tpa(p.pExpr, "date '2001-1-1'");
        tpa(p.pExpr, "trunc(date '2001-1-1')");
        tpa(p.pExpr, "-11*-8");
        tpa(p.pExpr, "cast(a as timestamp)");
        tpa(p.pExpr, "1+cast(a , timestamp)");
        tpa(p.pExpr, "\"CAST\"(sysdate as timestamp)");
        tpa(p.pExpr, "\"CAST\"(sysdate , timestamp)");
        tpa(p.pExpr, "\"CAST\"(sysdate , timestamp,12)");

    }

    @Test
    public void test3() {
        Parser p = new Parser();
        tpa(p.pExpr, "l_source (index_inout).text NOT LIKE '%*/%'");
        tpa(p.pExpr, "(l_source (index_inout).text NOT LIKE '%*/%')");
        tpa(p.pExpr, "string_in LIKE c_mask");
        tpa(p.pExpr, "lower($$PLSQL_UNIT)");
        tpa(p.pIdent, "\"a\"");

        tpa(p.pExpr, "\"a\"");

        tpa(p.pDataType, "a");
        tpa(p.pDataType, "a.b");

        tpa(p.pDeclaration, "TYPE vat IS VARRAY(100) OF EMPLOYEES%ROWTYPE");
        tpa(p.pDeclaration, "TYPE EMPLOYEES_rc IS REF CURSOR RETURN EMPLOYEES%ROWTYPE");
        tpa(p.pDeclaration, "C_SCOPE_PREFIX constant VARCHAR2(31) := lower($$PLSQL_UNIT) || '.'");
        tpa(p.pDeclaration, "a number");
        tpa(p.pDeclaration, "a b");
        tpa(p.pDeclaration, "a b.c");
        tpa(p.pDeclaration, "hits aaa_coverage_tool.bool_tab");
        tpa(p.pDeclaration, "\"$hits\" aaa_coverage_tool.bool_tab");
        tpa(p.pDeclaration, " \"a\" b");
        tpa(p.pDeclaration, " \"a\" exception");
        tpa(p.pExpr, "PLTEXT IS NULL");
        tpa(p.pAndExpr, "ptFinLigne > 0 AND ptDebLigne >12");
        tpa(p.pDataType, "a");
        tpa(p.pDataType, "a.b");
        tpa(p.pDataType, "a.b%type");
        tpa(p.pDataType, "a.b%rowtype");
        tpa(p.pDataType, "CHAR(1)");
        tpa(p.pDataType, "varchar2(1000 char)");
        tpa(p.pDataType, "varchar(1000 char)");
        tpa(p.pDataType, "interval day to second");
        tpa(p.pDataType, "interval year to month");
        tpa(p.pDataType, "interval day(3) to second(4)");
        tpa(p.pDataType, "interval year(9) to month");
        tpa(p.pDataType, "interval");
        tpa(p.pDataType, "\"INTERVAL\"");

        tpa(p.pItemDeclaration, "a b");
        tpa(p.pAndExpr, "ptFinLigne > 0");
        tpa(p.pExpr, "q'[<script>]'");
        tpa(p.pExpr, "extract(year from to_date(bla))");
        tpa(p.pExpr, "\"EXTRACT\"(year from sysdate)");
        tpa(p.pExpr, "extract(1,2,3)");
        tpa(p.pExpr, "a multiset union all b(c.d, e.f)");
        tpa(p.pDeclaration, "a interval day to second");
        tpa(p.pExpr, "sysdate + interval '1' hour");
        tpa(p.pExpr, "bla+0.1/(z+0.7)");
    }

    @Test
    public void testIntervals() {
        Parser p = new Parser();
        tpa(p.pExpr, "INTERVAL '4 5:12:10.222' DAY TO SECOND(3)");
        tpa(p.pExpr, "INTERVAL '4 5:12' DAY TO MINUTE");
        tpa(p.pExpr, "INTERVAL '400 5' DAY(3) TO HOUR");
        tpa(p.pExpr, "INTERVAL '400' DAY(3)");
        tpa(p.pExpr, "INTERVAL '11:12:10.2222222' HOUR TO SECOND(7)");
        tpa(p.pExpr, "INTERVAL '11:20' HOUR TO MINUTE");
        tpa(p.pExpr, "INTERVAL '10' HOUR	");
        tpa(p.pExpr, "INTERVAL '10:22' MINUTE TO SECOND");
        tpa(p.pExpr, "INTERVAL '10' MINUTE");
        tpa(p.pExpr, "INTERVAL '4' DAY");
        tpa(p.pExpr, "INTERVAL '25' HOUR	");
        tpa(p.pExpr, "INTERVAL '40' MINUTE");
        tpa(p.pExpr, "INTERVAL '120' HOUR(3)");
        tpa(p.pExpr, "INTERVAL '30.12345' SECOND(2,4)");
        tpa(p.pExpr, "INTERVAL '123-2' YEAR(3) TO MONTH");
        tpa(p.pExpr, "INTERVAL '123' YEAR(3)");
        tpa(p.pExpr, "INTERVAL '300' MONTH(3)");
        tpa(p.pExpr, "INTERVAL '4' YEAR");
        tpa(p.pExpr, "INTERVAL '50' MONTH");
    }

    @Test
    public void testsqlAttributes() {
        Parser p = new Parser();
        tpa(p.pExpr, "1+2*(sql%rowcount +90)");
        tpa(p.pExpr, "1+2*(sql%\"ROWCOUNT\" +90)");
    }

    @Test
    public void test4() {
        Parser p = new Parser();
        tpa(p.pDeclaration, "pragma bla");
        tpa(p.pDeclaration, "pragma restrict_references (bla,wnds,bla)");

        tpa(p.pDeclaration, "procedure append_param(\n"
                + "    p_params in out nocopy logger.tab_param,\n"
                + "    p_name in varchar2,\n"
                + "    p_val in varchar2)");
        tpa(p.pCRPackage, " create or replace package a as g_logger_version constant varchar2(10) := 'x.x.x'; end;");
        tpa(p.pCRPackage, "create or replace package a is subtype bla is varchar2(200); end;");
        tpa(p.pCRPackage, "create or replace package a is subtype bla is x%rowtype; x p; procedure b(x integer); subtype bla is x%rowtype; end;");
        tpa(p.pStatement, " a:=b");
        tpa(p.pStatement, " select a into b from dual");
        tpa(p.pStatement, " with a as (select *from dual) select a into b from a");
        tpa(p.pStatement, " for r in (select * from dual) loop null; end loop");
        tpa(p.pStatement, " for r in (with aa as (select * from dual) select * from aa) loop null; end loop");
        tpa(p.pStatement, " fetch a into a,b,c");

        tpa(p.pStatement, "htp.p(q'[<script><!--\n"
                + "    function setit(res) {\n"
                + "      console.log(\"setit\");\n"
                + "      var i = document.getElementById(\"ip\");\n"
                + "      i.value = res;\n"
                + "    }\n"
                + "    --></script>\n"
                + "    ]')");
        tpa(p.pStatement, "continue");
        tpa(p.pStatement, "continue when 1>2");
        tpa(p.pStatement, "continue bla");
        tpa(p.pStatement, "continue bla when 3<a(4)");
        tpa(p.pStatement, "begin fetch a bulk collect into bla,blub;end");
        tpa(p.pStatement, "begin fetch a bulk collect into bla,blub limit 123;end");
        tpa(p.pStatement, "select dummy as \"x\" into y from dual");
        tpa(p.pStatement, "x:=1");
        tpa(p.pStatement, "\"x\":=1");
        tpa(p.pStatement, "open c for with a as (select * from dual) select * from a");
        tpa(p.pStatement, "  pipe row(lo_res)");
        tpa(p.pStatement, "loop null;  END LOOP bla");
        tpa(p.pStatement, "begin case bla when 'a' then null; else assa(1,2,3);  END CASE bla; end");
        tpa(p.pStatement, "begin case bla when 'a' then bla(77); else a:=x;  END CASE; end");
        tpa(p.pStatement, " l_returnvalue := to_char(l_time_utc, 'Dy, DD Mon YYYY HH24:MI:SS', 'NLS_DATE_LANGUAGE = AMERICAN') || ' GMT'");
    }

    @Test
    public void testForall() {
        Parser p = new Parser();
        tpa(p.pStatement, " forall i in 1 ..y \n"
                + "      insert into xxx(a,b)\n"
                + "      select sss,jjj\n"
                + "      from   kkkkk");
    }

    @Test
    public void testForallVal() {
        Parser p = new Parser();
        tpa(p.pStatement, "forall j in values of tab\n"
                + "   update atable\n"
                + "   set y = tab(j)\n"
                + "   where x = tab(j)");
    }

    @Test
    public void testForallInd() {
        Parser p = new Parser();
        tpa(p.pStatement, "forall j in indices of tab\n"
                + "   update atable\n"
                + "   set y = tab(j)\n"
                + "   where x = tab(j)");
    }

    @Test
    public void testForallInd2() {
        Parser p = new Parser();
        tpa(p.pStatement, "forall j in indices of tab between 12 and z*9 \n"
                + "   update atable\n"
                + "   set y = tab(j)\n"
                + "   where x = tab(j)");
    }

    @Test
    public void testForLoopManyParens() {
        Parser p = new Parser();
        tpa(p.pStatement, "for j in (select a,b,c from t1 union all select a,b,c from t2) loop null; end loop");
        tpa(p.pStatement, "for j in ((select a,b,c from t1) union all select a,b,c from t2) loop null; end loop");
        tpa(p.pStatement, "for j in (select a,b,c from t1 union all (select a,b,c from t2)) loop null; end loop");
        tpa(p.pStatement, "for j in ((select a,b,c from t1 union all select a,b,c from t2)) loop null; end loop");
    }

    @Test
    public void testTypeDeclaration() {
        Parser p = new Parser();
        tpa(p.pDeclaration, "subtype a is integer RANGE 1 .. 2");
        tpa(p.pDeclaration, "subtype a is integer RANGE 1 .. 2 not null");
        tpa(p.pDeclaration, "subtype a is integer RANGE -1 .. 2 not null");
        tpa(p.pDeclaration, "subtype a is integer not null");
        tpa(p.pDeclaration, "subtype a is interval year(9) to month");
    }

    @Test
    public void testLockTable() {
        Parser p = new Parser();
        tpa(p.pStatement, "lock table a in share mode nowait");
        tpa(p.pStatement, "lock table a,b,c in share update mode");
        tpa(p.pStatement, "lock table a,b,c in row share mode nowait");
        tpa(p.pStatement, "lock table a,b,c in row exclusive mode");
        tpa(p.pStatement, "lock table a,b,c in exclusive mode");
        tpa(p.pStatement, "lock table a in share row exclusive mode");
    }

    @Test
    public void testTrimStatement() {
        Parser p = new Parser();
        tpa(p.pStatement, "x:=trim(both '''' from trim(leading ' ' from va))");
        tpa(p.pStatement, "x:=trim(leading '''' from va)");
        tpa(p.pStatement, "x:=trim(LEADING '''' from va)");
        tpa(p.pStatement, "x:=trim(LEADING '''' FROM va)");
        tpa(p.pStatement, "x:=trim('''' from va)");
        tpa(p.pStatement, "x:=trim(va)");
        tpa(p.pStatement, "x:=trim(trim(va))");
        tpa(p.pStatement, "x:=TRIM(x from y)");
    }

    @Test
    public void testTrimExpresionMean() {
        Parser p = new Parser();
        // you can define your own trim function !
        // it will shadow the default definition
        tpa(p.pExpr, "trim(a,b)");
        tpa(p.pExpr, "\"TRIM\"(x from y)");
        tpa(p.pExpr, "\"TRIM\"('a' from ' ')");
    }

    public void testPackage(String filename) {
        Parser p = new Parser();
        String s = Util.loadFile(filename);
        tpa(p.pCRPackage, s);
    }

    public void testPackageBody(String filename) {
        Parser p = new Parser();
        String s = Util.loadFile(filename);
        tpa(p.pCRPackageBody, s);
    }

    public void testFolder(String folder) throws IOException {
        Path p = Paths.get(folder);
        for (Path a : java.nio.file.Files.newDirectoryStream(p)) {
            if (a.toString().endsWith(".pkb")) {
                System.out.print("test: " + a);
                testPackageBody(a.toString());
            }
            if (a.toString().endsWith(".pks")) {
                System.out.print("test: " + a);
                testPackage(a.toString());
            }
        }
    }

    @Test
    public void testAlexandria() throws IOException {
        testFolder("/home/roland/Documents/GitHub/plsql-parser/alexandria-ora");
    }
}
