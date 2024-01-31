package uk.gov.companieshouse.accounts.user.exceptions;

public class BadRequestRuntimeException extends RuntimeException {

    public BadRequestRuntimeException(String message) {
        super(message);
    }
}
