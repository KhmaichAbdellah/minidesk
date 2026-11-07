package com.minidesk.service.impl;

import com.minidesk.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Override
    public void sendEmail(String toEmail, String name, String password) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            if (fromEmail != null && !fromEmail.isBlank()) {
                helper.setFrom(fromEmail, "MiniDesk Support");
            }
            helper.setTo(toEmail);
            helper.setSubject("🎫 Bienvenue sur MiniDesk - Vos identifiants");
            helper.setText(buildEmailBody(name, toEmail, password), true);

            mailSender.send(message);
            log.info("Email de bienvenue envoyé avec succès à : {}", toEmail);

        } catch (Exception e) {
            log.warn("Impossible d'envoyer l'email de bienvenue à {} : {}. Mot de passe généré : {}", toEmail, e.getMessage(), password);
        }
    }

    private String buildEmailBody(String name, String email, String password) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 30px;
                        border: 1px solid #e0e0e0; border-radius: 8px;">

                <h2 style="color: #2c3e50;">🎫 Bienvenue sur <strong>MiniDesk</strong></h2>

                <p>Bonjour <strong>%s</strong>,</p>

                <p>Votre compte a été créé avec succès. Voici vos identifiants de connexion :</p>

                <div style="background: #f4f4f4; padding: 15px; border-radius: 6px; margin: 20px 0;">
                    <p style="margin: 5px 0;">📧 <strong>Email :</strong> %s</p>
                    <p style="margin: 5px 0;">🔑 <strong>Mot de passe temporaire :</strong>
                        <span style="color: #e74c3c; font-size: 18px; font-weight: bold;">%s</span>
                    </p>
                </div>

                <p style="color: #7f8c8d; font-size: 13px;">
                    ⚠️ Pour des raisons de sécurité, nous vous recommandons de changer votre mot de passe
                    dès votre première connexion.
                </p>

                <hr style="border: none; border-top: 1px solid #e0e0e0; margin: 20px 0;" />

                <p style="color: #95a5a6; font-size: 12px; text-align: center;">
                    Cet email a été envoyé automatiquement par MiniDesk. Ne pas répondre.
                </p>
            </div>
            """.formatted(name, email, password);
    }
}
