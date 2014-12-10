package spinat.plsqlparser;


// contains range information for parsing
// if a class is a subclass of this class
// it gets extended with range information during parsing

public abstract class Ranged {
    private int start = -1;
    private int end = -1;

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public void setRange(int start, int end) {
        if (start < 0 || end < 0) {
            throw new IllegalArgumentException();
        }
        if (this.start >= 0 || this.end >= 0) {
            throw new RuntimeException("start and end already set");
        }
        this.start = start;
        this.end = end;
    }

}
