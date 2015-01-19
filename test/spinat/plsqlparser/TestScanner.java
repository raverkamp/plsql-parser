package spinat.plsqlparser;

import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author rav
 */
public class TestScanner {

    public TestScanner() {
    }

    // decompose String str and check that token i is equal to s
    static void chk(String str, int i, String s) {
        ArrayList<Token> a = Scanner.scanAll(str);
        Assert.assertEquals(a.get(i).str, s);
    }

    // decompose the String s into tokens and the list of tokens is equal to
    // s
    static void chk(String str, String[] s) {
        ArrayList<Token> a = Scanner.scanAll(str);
        Assert.assertEquals(s.length, a.size() - 1);
        for (int i = 0; i < s.length; i++) {
            Assert.assertEquals(a.get(i).str, s[i]);
        }
    }

    @Test
    public void test1() {
        String s = " 1 a  1.2 1.3e4  --\n 1*2+3 1..2  'a' /* bla */";

        chk(s, 0, " ");
        chk(s, 1, "1");
        chk(s, 5, "1.2");
        chk(s, 7, "1.3e4");

        chk("--bla\n1.2e-3", 0, "--bla\n");
        chk("--bla\n1.2e-3", 1, "1.2e-3");
        chk("--bla\n1.2e+3", 1, "1.2e+3");
        chk("1..6", 0, "1");
        chk("1..6", 1, "..");
        chk("1..6", 2, "6");
        chk("1.2..6", 0, "1.2");
        chk("1.2..6", 1, "..");
        chk("1 'a''b''' x", 2, "'a''b'''");
        chk("1 'a''b''' x", 4, "x");
        chk("1 q'[<script>]' 2", new String[]{"1", " ", "q'[<script>]'", " ", "2"});
        chk("1 $$bla 2", new String[]{"1", " ", "$$bla", " ", "2"});
        chk("1 \"a\"  2", new String[]{"1", " ", "\"a\"", "  ", "2"});
        chk("\"$hits\" aaa_coverage_tool.bool_tab;", new String[]{"\"$hits\"", " ", "aaa_coverage_tool", ".", "bool_tab", ";"});
        chk("-1", new String[]{"-", "1"});
        chk("[0]", new String[]{"[", "0", "]"});
    }
}
