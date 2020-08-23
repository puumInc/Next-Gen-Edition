package org._movie_hub._next_gen_edition._custom;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import org.jetbrains.annotations.NotNull;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * @author Mandela aka puumInc
 */
public abstract class Email extends Watchdog {

    /**
     * <strong>
     * When sending an email to someone using java, it will only work if you get an APP PASSWORD from your email provider
     * </strong>
     */
    private final String DEVELOPER_EMAIL = "puumInc@outlook.com";

    public boolean send_automatic_reply_to_user(final @NotNull String receiverEmail) {
        @SuppressWarnings("SpellCheckingInspection") final String DEVELOPER_PASSWORD = "mrwkcthfbtnujbhd";
        try {
            final Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", "smtp-mail.outlook.com");
            properties.put("mail.smtp.port", 587);

            final Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(DEVELOPER_EMAIL, DEVELOPER_PASSWORD);
                }
            });
            session.setDebug(true);

            final Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(DEVELOPER_EMAIL));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(receiverEmail));
            message.setSubject("Movie hub");
            message.setText("Hello, this is an automatic reply. Thank you for your response, i will get back to you soon.\n"
                    .concat("Regards\n")
                    .concat("<puum.inc()/>"));
            Transport.send(message);

            return true;
        } catch (MessagingException e) {
            if (e.getLocalizedMessage().contains("Couldn't connect to host, port")) {
                Platform.runLater(() -> error_message("Could not connect to the Internet!", "Please ensure you are connected to the internet t continue").show());
            } else {
                Platform.runLater(() -> programmer_error(e).show());
                new Thread(write_stack_trace(e)).start();
            }
        }
        return false;
    }

    public boolean inform_developer(final @NotNull String receiverEmail, final String text) {
        final String EMAIL = "emandela60@gmail.com";
        @SuppressWarnings("SpellCheckingInspection") final String PASSWORD = "xirswwtvweuonbdg";

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("senderEmail", new Gson().toJsonTree(receiverEmail, String.class));
        jsonObject.add("message", new Gson().toJsonTree(text, String.class));

        try {
            final Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", "smtp.gmail.com");
            properties.put("mail.smtp.port", 587);

            final Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL, PASSWORD);
                }
            });
            session.setDebug(true);

            final Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(DEVELOPER_EMAIL));
            message.setSubject("Moviehub@COMMENTS");
            message.setText(new Gson().toJson(jsonObject, JsonObject.class));
            Transport.send(message);

            return true;
        } catch (MessagingException e) {
            if (e.getLocalizedMessage().contains("Couldn't connect to host, port")) {
                Platform.runLater(() -> error_message("Could not connect to the Internet!", "Please ensure you are connected to the internet t continue").show());
            } else {
                Platform.runLater(() -> programmer_error(e).show());
                new Thread(write_stack_trace(e)).start();
            }
        }
        return false;
    }

}
