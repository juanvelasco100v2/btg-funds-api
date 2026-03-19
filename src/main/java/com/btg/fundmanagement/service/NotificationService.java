package com.btg.fundmanagement.service;

import com.btg.fundmanagement.entity.Fund;
import com.btg.fundmanagement.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final SesClient sesClient;
    private final SnsClient snsClient;
    private final String senderEmail;

    public NotificationService(@Value("${aws.region}") String region,
                               @Value("${aws.ses.sender-email}") String senderEmail) {
        this.sesClient = SesClient.builder().region(Region.of(region)).build();
        this.snsClient = SnsClient.builder().region(Region.of(region)).build();
        this.senderEmail = senderEmail;
    }

    public void notifySubscription(User user, Fund fund) {
        var message = "Se ha suscrito exitosamente al fondo " + fund.getName()
                + " por un monto de COP $" + String.format("%,d", fund.getMinimumAmount()) + ".";

        switch (user.getNotificationPreference()) {
            case "EMAIL" -> sendEmail(user.getEmail(), "Suscripcion exitosa - " + fund.getName(), message);
            case "SMS" -> sendSms(user.getPhone(), message);
            default -> log.warn("Preferencia de notificacion no reconocida: {}", user.getNotificationPreference());
        }
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            sesClient.sendEmail(SendEmailRequest.builder()
                    .source(senderEmail)
                    .destination(Destination.builder().toAddresses(to).build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).charset("UTF-8").build())
                            .body(Body.builder()
                                    .text(Content.builder().data(body).charset("UTF-8").build())
                                    .build())
                            .build())
                    .build());
            log.info("Email enviado a {}", to);
        } catch (Exception e) {
            log.error("Error enviando email a {}: {}", to, e.getMessage());
        }
    }

    private void sendSms(String phoneNumber, String message) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            log.warn("Numero de telefono no disponible para enviar SMS");
            return;
        }
        try {
            snsClient.publish(PublishRequest.builder()
                    .phoneNumber(phoneNumber)
                    .message(message)
                    .build());
            log.info("SMS enviado a {}", phoneNumber);
        } catch (Exception e) {
            log.error("Error enviando SMS a {}: {}", phoneNumber, e.getMessage());
        }
    }
}
