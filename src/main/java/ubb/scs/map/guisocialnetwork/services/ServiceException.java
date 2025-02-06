package ubb.scs.map.guisocialnetwork.services;

/**
 * thrown when a problem has been reported to the Service Layer
 */
public class ServiceException extends RuntimeException {
    public ServiceException(Throwable cause) {
        super(cause);
    }

    public ServiceException(String message) {
        super(message);
    }
}
