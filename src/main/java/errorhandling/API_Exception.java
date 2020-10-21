package errorhandling;

public class API_Exception extends Exception {

    public static final String INPUTS_MISSING = "One or more required inputs are missing";
    public static final String PERSON_NOT_FOUND = "Person not found";
    int errorCode;

    public API_Exception(String message,int errCode) {
        super(message);
        this.errorCode = errCode;
    }
    public API_Exception(String message) {
        super(message);
        this.errorCode = 400;
    }

    public int getErrorCode() {
        return errorCode;
    }

    
}
