package spinat.plsqlparser;

import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;

public class PatchTest {

    public PatchTest() {

    }

    @Test
    public void test1() {

        Patch p1 = new Patch(0, Patch.Position.LEADING, "a1");
        Patch p2 = new Patch(0, Patch.Position.LEADING, "a2");
        Patch p3 = new Patch(0, Patch.Position.TRAILING, "b1");
        Patch p4 = new Patch(0, Patch.Position.TRAILING, "b2");
        ArrayList<Patch> l = new ArrayList<>();
        l.add(p1);
        l.add(p2);
        l.add(p3);
        l.add(p4);

        assertEquals("b1b2a2a1", Patch.applyPatches("", l));
        assertEquals("b1b2a2a1xyz", Patch.applyPatches("xyz", l));

        l.clear();
        l.add(p4);
        l.add(p3);
        l.add(p2);
        l.add(p1);

        assertEquals("b2b1a1a2", Patch.applyPatches("", l));

        p1 = new Patch(1, Patch.Position.LEADING, "a1");
        p2 = new Patch(1, Patch.Position.LEADING, "a2");
        p3 = new Patch(1, Patch.Position.TRAILING, "b1");
        p4 = new Patch(1, Patch.Position.TRAILING, "b2");
        Patch p5 = new Patch(1, 2, "xyz");

        l.clear();
        l.add(p1);
        l.add(p2);
        l.add(p3);
        l.add(p4);
        l.add(p5);

        assertEquals("ub1b2a2a1xyzo", Patch.applyPatches("ufo", l));

        Patch p6 = new Patch(2, 3, "QW");
        l.add(0, p6);
        assertEquals("ub1b2a2a1xyzQWx", Patch.applyPatches("ufox", l));

        l.remove(p6);
        l.add(p6);
        assertEquals("ub1b2a2a1xyzQWx", Patch.applyPatches("ufox", l));

        Patch p7 = new Patch(3, 4, "X");
        l.add(p7);
        assertEquals("ub1b2a2a1xyzQWX", Patch.applyPatches("ufox", l));

        Patch p8 = new Patch(4, Patch.Position.LEADING, "L");
        l.add(0, p8);
        assertEquals("ub1b2a2a1xyzQWXL", Patch.applyPatches("ufox", l));

        Patch p9 = new Patch(4, Patch.Position.TRAILING, "T");
        l.add(p9);
        assertEquals("ub1b2a2a1xyzQWXTL", Patch.applyPatches("ufox", l));

    }

}
