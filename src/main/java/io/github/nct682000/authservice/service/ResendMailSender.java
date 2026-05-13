package io.github.nct682000.authservice.service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@ConditionalOnProperty(name = "auth.mail.provider", havingValue = "resend")
public class ResendMailSender implements MailSender {

    private static final String RESEND_API_BASE_URL = "https://api.resend.com";
    private static final String SEND_EMAIL_PATH = "/emails";
    private static final String SUBJECT = "Your password reset code";

    private final RestClient restClient;
    private final String fromHeader;

    public ResendMailSender(
            @Value("${auth.mail.resend.api-key}") String apiKey,
            @Value("${auth.mail.from-address}") String fromAddress,
            @Value("${auth.mail.from-name:Auth Service}") String fromName,
            RestClient.Builder restClientBuilder) {

        this.restClient = restClientBuilder
                .baseUrl(RESEND_API_BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        // Resend accepts both "name@domain" and "Display Name <name@domain>".
        // The display name shows up in the recipient's inbox sender column.
        this.fromHeader = fromName + " <" + fromAddress + ">";
    }

    @Override
    public void sendPasswordResetOtp(String email, String plainOtp) {
        ResendEmailRequest body = new ResendEmailRequest(
                fromHeader,
                List.of(email),
                SUBJECT,
                htmlBody(plainOtp),
                plaintextBody(plainOtp)
        );

        try {
            restClient.post()
                    .uri(SEND_EMAIL_PATH)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            // NOTE: do not log the OTP value here — only the address and event.
            log.info("Password reset OTP dispatched via Resend to {}", email);
        } catch (RestClientResponseException ex) {
            // Resend returns 4xx with a JSON body like {"name":"validation_error","message":"..."}.
            // Surface the body in logs so configuration mistakes (unverified domain,
            // bad API key, sandbox-recipient mismatch) are obvious without enabling
            // wire-level logging.
            log.error("Resend API rejected OTP dispatch to {} — status={} body={}",
                    email, ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            throw new MailDispatchException("Resend rejected the request: " + ex.getStatusCode(), ex);
        } catch (Exception ex) {
            // Network errors, JSON serialization failures, etc.
            log.error("Failed to dispatch password reset OTP to {} via Resend", email, ex);
            throw new MailDispatchException(ex);
        }
    }

    /** Plaintext fallback rendered by mail clients that don't display HTML. */
    private String plaintextBody(String otp) {
        return """
                Hi,

                Use the following code to reset your password:

                    %s

                This code expires in 10 minutes and can only be used once.
                If you did not request a password reset, ignore this email — your
                account remains safe.

                — Auth Service
                """.formatted(otp);
    }

    /**
     * Inline-styled HTML body. Email clients strip {@code <style>} blocks
     * and external CSS, so inline styles are the only reliable way to
     * format transactional emails consistently.
     */
    private String htmlBody(String otp) {
        return """
                <!doctype html>
                <html>
                  <body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; color: #1f2937; line-height: 1.5; padding: 24px; background: #f8fafc;">
                    <div style="max-width: 480px; margin: 0 auto; background: #ffffff; border-radius: 8px; padding: 32px; border: 1px solid #e5e7eb;">
                      <h2 style="margin: 0 0 16px 0; color: #111827;">Password reset</h2>
                      <p style="margin: 0 0 16px 0;">Use the code below to reset your password:</p>
                      <div style="font-size: 28px; font-weight: 700; letter-spacing: 6px; text-align: center; padding: 16px; background: #f3f4f6; border-radius: 6px; color: #111827; margin: 0 0 16px 0;">
                        %s
                      </div>
                      <p style="margin: 0 0 8px 0; color: #6b7280; font-size: 14px;">This code expires in 10 minutes and can only be used once.</p>
                      <p style="margin: 0; color: #6b7280; font-size: 14px;">If you did not request a password reset, you can safely ignore this email.</p>
                    </div>
                  </body>
                </html>
                """.formatted(otp);
    }

    private record ResendEmailRequest(
            String from,
            List<String> to,
            String subject,
            String html,
            String text
    ) {}
}
