package com.minidesk.service.impl;

import com.minidesk.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendEmail(String toEmail, String name, String password) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("🎫 Bienvenue sur MiniDesk - Vos identifiants");
            helper.setText(buildEmailBody(name, toEmail, password), true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email : " + e.getMessage());
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

                <p style="color: #e67e22;">
                    ⚠️ Pour des raisons de sécurité, veuillez changer votre mot de passe
                    après votre première connexion.
                </p>

                <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                <p style="color: #999; font-size: 12px;">
                    Cet email a été envoyé automatiquement, merci de ne pas y répondre.
                </p>
            </div>
            """.formatted(name, email, password);
    }
}