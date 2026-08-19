package com.example.backend.email.service;

import com.example.backend.tool.core.model.Tool;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.name:AItoolHub}")
    private String appName;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    // =========================
    // 🔒 VALIDATION HELPERS
    // =========================

    private boolean hasRecipient(Tool tool) {
        return tool.getSubmittedByEmail() != null
                && !tool.getSubmittedByEmail().isBlank();
    }

    private String safeName(Tool tool) {
        return tool.getSubmittedByName() != null && !tool.getSubmittedByName().isBlank()
                ? tool.getSubmittedByName()
                : "there";
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent, String plainTextFallback) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(plainTextFallback, htmlContent);

            mailSender.send(mimeMessage);
            log.info("Email successfully sent to: {} with subject: {}", to, subject);
        } catch (Exception e) {
            log.error("Email sending failed to {}: {}", to, e.getMessage());
        }
    }

    // =========================
    // ✅ TOOL APPROVED EMAIL (ASYNC)
    // =========================

    @Async
    public void sendToolApprovedEmail(Tool tool) {
        if (!hasRecipient(tool)) {
            log.warn("No submitter email for approved tool {}", tool.getId());
            return;
        }

        String recipient = tool.getSubmittedByEmail();
        String name = safeName(tool);
        String toolName = tool.getName();
        String toolUrl = frontendUrl + "/tools/" + tool.getSlug();
        String subject = "🎉 Your AI tool \"" + toolName + "\" has been approved!";

        String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 24px; color: #1e293b; }
                        .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 16px; border: 1px solid #e2e8f0; padding: 32px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }
                        .badge { display: inline-block; background-color: #ecfdf5; color: #047857; font-weight: 700; font-size: 12px; padding: 4px 12px; border-radius: 9999px; margin-bottom: 16px; border: 1px solid #a7f3d0; }
                        h1 { color: #0f172a; font-size: 22px; font-weight: 800; margin-top: 0; }
                        p { font-size: 15px; line-height: 1.6; color: #475569; }
                        .btn { display: inline-block; background-color: #2563eb; color: #ffffff !important; text-decoration: none; padding: 12px 24px; border-radius: 10px; font-weight: 600; font-size: 14px; margin: 20px 0; }
                        .footer { margin-top: 32px; padding-top: 20px; border-top: 1px solid #f1f5f9; font-size: 13px; color: #94a3b8; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <span class="badge">SUBMISSION APPROVED</span>
                        <h1>Great news, %s! 🎉</h1>
                        <p>Your AI tool <strong>%s</strong> has passed review and is now live in the <strong>%s</strong> directory.</p>
                        <p>Users from around the world can now discover, bookmark, and review your tool.</p>
                        <a href="%s" class="btn">View Your Tool Live →</a>
                        <p>Thank you for contributing to the AI ecosystem!</p>
                        <div class="footer">
                            <p>— The %s Team<br><a href="%s" style="color: #2563eb; text-decoration: none;">%s</a></p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(name, toolName, appName, toolUrl, appName, frontendUrl, frontendUrl);

        String plainText = """
                Hi %s,

                Great news! 🎉
                Your AI tool "%s" has been approved and is now live on %s.

                View it here:
                %s

                Thank you for contributing to the community!

                — %s Team
                """.formatted(name, toolName, appName, toolUrl, appName);

        sendHtmlEmail(recipient, subject, htmlContent, plainText);
    }

    // =========================
    // ❌ TOOL REJECTED EMAIL (ASYNC)
    // =========================

    @Async
    public void sendToolRejectedEmail(Tool tool) {
        if (!hasRecipient(tool)) {
            log.warn("No submitter email for rejected tool {}", tool.getId());
            return;
        }

        String recipient = tool.getSubmittedByEmail();
        String name = safeName(tool);
        String toolName = tool.getName();
        String reason = tool.getRejectionReason() != null && !tool.getRejectionReason().isBlank()
                ? tool.getRejectionReason()
                : "The submission did not meet our listing quality guidelines.";
        String subject = "Update regarding your submission: \"" + toolName + "\"";

        String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 24px; color: #1e293b; }
                        .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 16px; border: 1px solid #e2e8f0; padding: 32px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }
                        .badge { display: inline-block; background-color: #fef2f2; color: #b91c1c; font-weight: 700; font-size: 12px; padding: 4px 12px; border-radius: 9999px; margin-bottom: 16px; border: 1px solid #fecaca; }
                        .reason-box { background-color: #f8fafc; border-left: 4px solid #ef4444; padding: 14px 18px; border-radius: 0 8px 8px 0; margin: 18px 0; font-size: 14px; color: #334155; }
                        h1 { color: #0f172a; font-size: 22px; font-weight: 800; margin-top: 0; }
                        p { font-size: 15px; line-height: 1.6; color: #475569; }
                        .btn { display: inline-block; background-color: #0f172a; color: #ffffff !important; text-decoration: none; padding: 12px 24px; border-radius: 10px; font-weight: 600; font-size: 14px; margin: 20px 0; }
                        .footer { margin-top: 32px; padding-top: 20px; border-top: 1px solid #f1f5f9; font-size: 13px; color: #94a3b8; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <span class="badge">SUBMISSION UPDATE</span>
                        <h1>Hi %s,</h1>
                        <p>Thank you for submitting <strong>%s</strong> to <strong>%s</strong>.</p>
                        <p>After reviewing your submission, our team was unable to approve it at this time.</p>
                        <div class="reason-box">
                            <strong>Feedback from Review Team:</strong><br>
                            %s
                        </div>
                        <p>You are welcome to update your tool details and submit again once the issues are resolved.</p>
                        <a href="%s/add-tool" class="btn">Submit Updated Tool →</a>
                        <div class="footer">
                            <p>— The %s Team<br><a href="%s" style="color: #2563eb; text-decoration: none;">%s</a></p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(name, toolName, appName, reason, frontendUrl, appName, frontendUrl, frontendUrl);

        String plainText = """
                Hi %s,

                Thank you for submitting "%s" to %s.

                Unfortunately, your submission was not approved at this time.
                Reason: %s

                You are welcome to improve your submission and try again.

                — %s Team
                """.formatted(name, toolName, appName, reason, appName);

        sendHtmlEmail(recipient, subject, htmlContent, plainText);
    }
}