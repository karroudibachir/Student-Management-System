package com.sms.student;
public class ResourceNotFoundException extends RuntimeException {
 public ResourceNotFoundException(Long id){super("Student with id "+id+" was not found");}
 public ResourceNotFoundException(String message){super(message);}
}
