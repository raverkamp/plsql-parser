package spinat.plsqlparser;

public class ScanException extends RuntimeException {

    // this exception is throw if an error during parsing occurs
    // unknown character, uncloses string
    public ScanException(String s) {
        super(s);
    }
}
