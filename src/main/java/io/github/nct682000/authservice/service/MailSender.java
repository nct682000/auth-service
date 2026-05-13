package io.github.nct682000.authservice.service;

public interface MailSender {

    void sendPasswordResetOtp(String email, String plainOtp);
}
