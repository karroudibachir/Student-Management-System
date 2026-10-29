package com.sms.student;
public class DuplicateEmailException extends RuntimeException { public DuplicateEmailException(){super("A student with this email already exists");} }
