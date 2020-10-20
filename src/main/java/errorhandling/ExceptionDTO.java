package errorhandling;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionDTO {

    public ExceptionDTO(int code, String message, Throwable ex, boolean isDebug) {
        this.code = code;
        this.message = message;
        if (isDebug) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            this.stackTrace = sw.toString();
        }
    }


    final private int code;
    final private String message;
    private String stackTrace;
}
