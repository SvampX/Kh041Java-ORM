package exceptions;

public class DataObtainingFailureException extends DataAccessException {

    public DataObtainingFailureException() {
        super();
    }

    public DataObtainingFailureException(String message) {
        super(message);
    }
}
