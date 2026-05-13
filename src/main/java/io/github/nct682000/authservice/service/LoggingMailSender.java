package io.github.nct682000.authservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "auth.mail.provider", havingValue = "logging", matchIfMissing = true)
public class LoggingMailSender implements MailSender {

    @Override
    public void sendPasswordResetOtp(String email, String plainOtp) {
        log.info("[DEV-MAIL] Password reset OTP for {} = {} (10-minute window)",
                email, plainOtp);
    }
}
