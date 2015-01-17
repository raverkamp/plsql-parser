package spinat.plsqlparser;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
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

    public void checkExpr(String s) {
        Seq seq = scan(s);
        Parser p = new Parser();
        Res<Ast.Expression> r = p.paExpr(seq);
        assertNotNull(r);
        System.out.println(r.next);
        assertTrue(r.next.head().ttype == TokenType.TheEnd);
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

        checkExpr("a");
        checkExpr("\"a\"");
        checkExpr("a.b");

        checkExpr("a(b)");

        checkExpr("a+b");
        checkExpr("a.i+b*x");
        checkExpr("sin(a+k*10)");
        checkExpr("12**88");
        checkExpr("a%b");
        checkExpr("a.b.c(f=>12 > 5) +4");
        checkExpr("a.b.c(f=>12 > 5) + case 1 when 1 then 2 else 4 end");
        checkExpr("case when 1 then 2 else 4 end");
        checkExpr("date '2001-1-1'");
        checkExpr("trunc(date '2001-1-1')");
        checkExpr("-11*-8");

    }

    public void tpa(Pa p, String s) {
        Seq seq = scan(s);
        Res r = p.pa(seq);
        assertNotNull(r);
        System.out.println(r.v);
        //System.out.println(r.next);
        assertTrue(r.next.head().ttype == TokenType.TheEnd || r.next.head().ttype == TokenType.Div);
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

        tpa(p.pItemDeclaration, "a b");
        tpa(p.pAndExpr, "ptFinLigne > 0");
        tpa(p.pExpr, "q'[<script>]'");

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
    }

    @Test
    public void testTypeDeclaration() {
        Parser p = new Parser();
        tpa(p.pDeclaration, "subtype a is integer RANGE 1 .. 2");
        tpa(p.pDeclaration, "subtype a is integer RANGE 1 .. 2 not null");
        tpa(p.pDeclaration, "subtype a is integer RANGE -1 .. 2 not null");
        tpa(p.pDeclaration, "subtype a is integer not null");
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

    @Test
    public void test5() {
        testPackage("c:/users/rav/documents/plsql-repo/PL_FPDF.pks");
        testPackage("c:/users/rav/documents/plsql-repo/logger.pks");
        testPackage("c:/users/rav/documents/plsql-repo/excp.pks");
        testPackage("c:/users/rav/documents/plsql-repo/io.pks");
        testPackage("c:/users/rav/documents/plsql-repo/env.pks");
    }

    @Test
    public void test6() {
        testPackageBody("c:/users/rav/documents/plsql-repo/plog.pkb");
        testPackageBody("c:/users/rav/documents/plsql-repo/plog_out_dbms_output.pkb");
    }

    @Test
    public void test7() throws Exception {
        Path dir = Paths.get("c:/users/rav/documents/plsql-repo/demo_feuerstein");
        try (DirectoryStream<Path> stream
                = Files.newDirectoryStream(dir, "*.pks")) {
            for (Path entry : stream) {
                System.out.println(entry.getFileName());
                testPackage(dir.resolve(entry).toString());
            }
        }
    }

    @Test
    public void test8() throws Exception {
        Path dir = Paths.get("c:/users/rav/documents/plsql-repo/demo_feuerstein");
        try (DirectoryStream<Path> stream
                = Files.newDirectoryStream(dir, "*.pkb")) {
            for (Path entry : stream) {
                System.out.println(entry.getFileName());
                testPackageBody(dir.resolve(entry).toString());
            }
        }
    }
}
