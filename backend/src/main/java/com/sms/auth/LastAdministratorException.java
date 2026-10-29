package com.sms.auth;

public class LastAdministratorException extends RuntimeException {
    public LastAdministratorException() { super("At least one administrator must remain"); }
}
