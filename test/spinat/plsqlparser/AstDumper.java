package spinat.plsqlparser;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import spinat.plsqlparser.Ast.CNumber;

public class AstDumper {

    final Appendable a;
    final String spaces;

    public static void dumpObject(Appendable a, Object o) {
        AstDumper ad = new AstDumper(a);
        try {
            ad.dump(o, 0);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private AstDumper(Appendable a) {
        this.a = a;
        char[] c = new char[1000000];
        for (int i = 0; i < c.length; i++) {
            c[i] = ' ';
        }
        spaces = new String(c);
    }

    private void dump(Object o, int level) throws IOException, IllegalArgumentException, IllegalAccessException {
        if (o == null) {
            a.append("null\n");
            return;
        }
        if (o instanceof Ast.Ident) {
            a.append(((Ast.Ident) o).val);
            a.append("\n");
            return;
        }
        if (o instanceof String) {
            a.append("" + o);
            a.append("\n");
            return;
        }
        if (o instanceof Number) {
            a.append("" + o);
            a.append("\n");
            return;
        } if (o instanceof Boolean) {
            a.append("" + o);
            a.append("\n");
            return;
        }
        if (o instanceof Enum) {
            a.append("" + o);
            a.append("\n");
            return;
        }
        if (o instanceof Ast.CNumber) {
            a.append("" + ((Ast.CNumber) o).val);
            a.append("\n");
            return;
        }
        if (o instanceof Ast.CString) {
            a.append("" + ((Ast.CString) o).val);
            a.append("\n");
            return;
        }

        if (o instanceof List) {
            List l = (List) o;
            a.append("<list>\n");
            for (int i = 0; i < l.size(); i++) {
                a.append(spaces.substring(0, level));
                a.append("" + i + ":");
                dump(l.get(i), level + 1);
            }
            return;
        }
        Class c = o.getClass();
        a.append(c.getSimpleName());
        a.append("\n");
        Field[] fields = c.getFields();
        for (Field f : fields) {
            a.append(spaces.substring(0, level + 1));
            a.append(f.getName());
            a.append(": ");
            Object o2 = f.get(o);
            dump(o2, level + 2);
        }
    }
}
