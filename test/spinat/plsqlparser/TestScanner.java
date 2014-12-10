/*
 * Copyright (C) 2014 rav
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

    static void chk(String str, int i, String s) {
        ArrayList<Token> a = Scanner.scanAll(str);
        Assert.assertEquals(a.get(i).str, s);
    }

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
        chk("1 \"a\"  2",new String[]{"1"," ","\"a\"","  ","2"});
    }

}
