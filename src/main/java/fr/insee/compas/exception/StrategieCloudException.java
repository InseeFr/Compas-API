package fr.insee.compas.exception;

public class StrategieCloudException extends RuntimeException {

    public StrategieCloudException(String message, Throwable cause) {
        super(message, cause);
    }

    public StrategieCloudException(String message) {
        super(message);
    }
}
