package com.example.backend.email.service;

import com.example.backend.tool.core.model.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.name}")
    private String appName;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // =========================
    // 🔒 VALIDATION HELPERS
    // =========================

    private boolean hasRecipient(Tool tool) {
        return tool.getSubmittedByEmail() != null
                && !tool.getSubmittedByEmail().isBlank();
    }

    private String safeName(Tool tool) {
        return tool.getSubmittedByName() != null
                ? tool.getSubmittedByName()
                : "there";
    }

    private void send(SimpleMailMessage msg) {
        try {
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Email sending failed: {}", e.getMessage(), e);
        }
    }

    // =========================
    // ✅ TOOL APPROVED EMAIL
    // =========================

    public void sendToolApprovedEmail(Tool tool) {

        if (!hasRecipient(tool)) {
            log.warn("No submitter email for approved tool {}", tool.getId());
            return;
        }

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(tool.getSubmittedByEmail());
        msg.setSubject("🎉 Your AI tool has been approved!");

        msg.setText("""
Hi %s,

Great news! 🎉

Your AI tool "%s" has been approved and is now live on %s.

You can view it here:
%s/tools/%s

Thank you for submitting your tool!

— %s Team
""".formatted(
                safeName(tool),
                tool.getName(),
                appName,
                frontendUrl,
                tool.getSlug(),
                appName
        ));

        send(msg);
    }

    // =========================
    // ❌ TOOL REJECTED EMAIL
    // =========================

    public void sendToolRejectedEmail(Tool tool) {

        if (!hasRecipient(tool)) {
            log.warn("No submitter email for rejected tool {}", tool.getId());
            return;
        }

        String reason = tool.getRejectionReason() != null
                ? tool.getRejectionReason()
                : "No specific reason provided";

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(tool.getSubmittedByEmail());
        msg.setSubject("❌ Your AI tool submission was rejected");

        msg.setText("""
Hi %s,

Thank you for submitting your AI tool "%s".

Unfortunately, it was not approved at this time.

Reason provided by our review team:
"%s"

You’re welcome to improve your tool and submit again.

— %s Team
""".formatted(
                safeName(tool),
                tool.getName(),
                reason,
                appName
        ));

        send(msg);
    }
}