package spinat.plsqlparser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

// a class to store edits to a string 
// and a method to apply a list of such edits to a string
// you can replace a range of characters or insert at a position
// then one can in the case the ordering ist first the Trailing and then the Leading

public class Patch {

    public static enum Position{
        LEADING,TRAILING
    }
    
    public final int start;
    public final int end;
    public final String txt;
    public final Position position;

    
    public Patch(int start, int end, String txt) {
        if (start>=end) {
            throw new RuntimeException("start must be smaller then end");
        }
        this.start = start;
        this.end = end;
        this.txt = txt;
        this.position = Position.LEADING;
    }
    
    public Patch(int start, Position position, String txt) {
        this.start = start;
        this.end = start;
        this.txt = txt;
        this.position = position;
    }

    static int intCompare(int x, int y) {
        if (x < y) {
            return -1;
        }
        if (x > y) {
            return 1;
        }
        return 0;
    }

    // leading means before the character at position start
    // trailing means after the character at position start-1
    // the order of the patches in patches determines the oder of inserts
    // the TRAILING patches for one position are inserted as they are in the list
    // the LEADING patches formone position are inserted in reverse order of the
    // patches list
    public static String applyPatches(String s, ArrayList<Patch> patches) {
        final HashMap<Patch,Integer> listpos = new HashMap<Patch, Integer>();
        for(int i=0;i<patches.size();i++) {
            listpos.put(patches.get(i), i);
        }
        ArrayList<Patch> a = new ArrayList<>();
        a.addAll(patches);
        Collections.sort(a, new Comparator<Patch>() {
            @Override
            public int compare(Patch p1, Patch p2) {
                if (p1.start < p1.end) {
                    if (p2.start >= p1.end) {
                        return -1;
                    } else if (p2.end <= p1.start) {
                        return 1;
                    } else {
                        throw new RuntimeException("BUG: patch overlap");
                    }
                } 
                // now we know p1 is an insert patch
                if (p2.start < p2.end) {
                    if (p1.start <= p2.start) {
                        return -1;
                    } else if(p1.start>=p2.end) {
                        return 1;
                    } else {
                         throw new RuntimeException("BUG: patch overlap");
                    }
                }
                // now we know both are insert patches
                int a = intCompare(p1.start , p2.start);
                if (a!=0) {
                    return a;
                }
                // OK same position
                if (p1.position == Position.LEADING) {
                    if (p2.position == Position.TRAILING) {
                        return 1;
                    } else {
                        // both are leading
                        // leading are insert in reverse order
                        return -intCompare(listpos.get(p1),listpos.get(p2));
                    }
                } else if (p2.position == Position.LEADING) {
                    return -1;
                } else {
                    // both are trailing, nprmal order
                    return intCompare(listpos.get(p1),listpos.get(p2));
                }
            }
        });
        StringBuilder b = new StringBuilder();
        int pos = 0;
        for (Patch patch : a) {
            b.append(s.substring(pos, patch.start));
            b.append(patch.txt);
            pos = patch.end;
        }
        b.append(s.substring(pos));
        return b.toString();
    }
}
