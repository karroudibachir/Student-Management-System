package com.sms.auth;

import com.sms.student.StudentResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class StudentEmailService {
    private final ObjectProvider<JavaMailSender> mailSenders;
    private final String host;
    private final String from;
    private final String loginUrl;

    public StudentEmailService(ObjectProvider<JavaMailSender> mailSenders,
            @Value("${spring.mail.host:}") String host,
            @Value("${sms.mail.from:}") String from,
            @Value("${sms.mail.login-url:http://localhost:8080/login}") String loginUrl) {
        this.mailSenders = mailSenders;
        this.host = host;
        this.from = from;
        this.loginUrl = loginUrl;
    }

    public void ensureConfigured() {
        if (host.isBlank() || from.isBlank() || mailSenders.getIfAvailable() == null) {
            throw new EmailDeliveryException("Configure SMTP settings and SMS_MAIL_FROM before adding students");
        }
    }

    public void sendCredentials(StudentResponse student, String temporaryPassword) {
        sendCredentials(student.firstName(), student.email(), temporaryPassword);
    }

    public void sendCredentialsForExistingRecord(String emailAddress, StudentCredentials credentials) {
        sendCredentials(credentials.user().displayName(), emailAddress, credentials.temporaryPassword());
    }

    private void sendCredentials(String displayName, String emailAddress, String temporaryPassword) {
        JavaMailSender sender = mailSenders.getIfAvailable();
        if (host.isBlank() || from.isBlank() || sender == null) {
            throw new EmailDeliveryException("Student email is not configured. Set the SMTP settings and try again");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(emailAddress);
        message.setSubject("Your StudentSpace account");
        message.setText("Hello " + displayName + ",\n\n"
                + "Your student account is ready.\n\n"
                + "Login: " + emailAddress + "\n"
                + "Temporary password: " + temporaryPassword + "\n"
                + "Sign in: " + loginUrl + "\n\n"
                + "Please keep this email private.\n");
        try {
            sender.send(message);
        } catch (MailException exception) {
            throw new EmailDeliveryException("Could not send the student login email. Check the SMTP configuration", exception);
        }
    }
}
