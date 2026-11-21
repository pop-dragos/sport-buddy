package ro.utcn.sportbuddy.common.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

// Trimite emailuri prin SMTP (de exemplu Gmail, configurat în application-local.properties).
// Fără configurare (spring.mail.host lipsă), linkul doar se scrie în consola backend-ului: așa se poate
// testa local fără cont de email.
// Trimiterea e asincronă: cererea nu așteaptă serverul de email și nici nu se vede după timpul de
// răspuns dacă un cont există.
@Service
@EnableAsync
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final ObjectProvider<JavaMailSender> sender;
    private final String from;

    public MailService(ObjectProvider<JavaMailSender> sender,
                       @Value("${app.mail.from:${spring.mail.username:}}") String from) {
        this.sender = sender;
        this.from = from;
    }

    public boolean isConfigured() {
        return sender.getIfAvailable() != null;
    }

    // Email simplu: un salut, un paragraf, un buton cu linkul și linkul scris în clar sub el.
    @Async
    public void sendLink(String to, String subject, String greeting, String text, String buttonLabel,
                         String link, String footer) {
        JavaMailSender mail = sender.getIfAvailable();
        if (mail == null) {
            log.warn("Email neconfigurat (spring.mail.host lipsește). Către {}: [{}] -> {}", to, subject, link);
            return;
        }
        try {
            MimeMessage message = mail.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            if (!from.isBlank()) helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(plain(greeting, text, link, footer), html(greeting, text, buttonLabel, link, footer));
            mail.send(message);
            log.info("Email trimis către {}: {}", to, subject);
        } catch (MessagingException | MailException e) {
            // utilizatorul poate cere din nou emailul; linkul rămâne în log ca să nu se piardă la testare
            log.error("Emailul către {} nu a putut fi trimis ({}). Link: {}", to, e.getMessage(), link);
        }
    }

    private static String plain(String greeting, String text, String link, String footer) {
        return greeting + "\n\n" + text + "\n\n" + link + "\n\n" + footer + "\n\nSportBuddy";
    }

    private static String html(String greeting, String text, String buttonLabel, String link, String footer) {
        String l = HtmlUtils.htmlEscape(link);
        return """
                <div style="font-family:-apple-system,Segoe UI,Roboto,Helvetica,Arial,sans-serif;max-width:480px;margin:0 auto;padding:32px 24px;color:#1d1d1f">
                  <div style="font-size:20px;font-weight:700;margin-bottom:24px">SportBuddy</div>
                  <p style="font-size:16px;margin:0 0 12px">%s</p>
                  <p style="font-size:15px;line-height:1.5;margin:0 0 24px;color:#424245">%s</p>
                  <a href="%s" style="display:inline-block;background:#0071e3;color:#ffffff;text-decoration:none;padding:12px 22px;border-radius:980px;font-size:15px;font-weight:600">%s</a>
                  <p style="font-size:13px;line-height:1.5;margin:24px 0 0;color:#6e6e73">Dacă butonul nu merge, copiază linkul în browser:<br><a href="%s" style="color:#0071e3;word-break:break-all">%s</a></p>
                  <p style="font-size:13px;line-height:1.5;margin:16px 0 0;color:#6e6e73">%s</p>
                </div>
                """.formatted(HtmlUtils.htmlEscape(greeting), HtmlUtils.htmlEscape(text), l,
                HtmlUtils.htmlEscape(buttonLabel), l, l, HtmlUtils.htmlEscape(footer));
    }
}
