package com.btg.fundmanagement.service;

import com.btg.fundmanagement.entity.Fund;
import com.btg.fundmanagement.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private SesClient sesClient;
    @Mock private SnsClient snsClient;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService("us-east-1", "sender@test.com");
        ReflectionTestUtils.setField(notificationService, "sesClient", sesClient);
        ReflectionTestUtils.setField(notificationService, "snsClient", snsClient);
    }

    private User createUser(String preference) {
        var user = new User();
        user.setUserId("u1");
        user.setEmail("user@test.com");
        user.setName("Test");
        user.setNotificationPreference(preference);
        user.setPhone("+57300");
        return user;
    }

    private Fund createFund() {
        var fund = new Fund();
        fund.setFundId("f1");
        fund.setName("FPV_RECAUDADORA");
        fund.setMinimumAmount(75000);
        return fund;
    }

    @Test
    void notifySubscription_email_sendsSesEmail() {
        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(SendEmailResponse.builder().build());

        notificationService.notifySubscription(createUser("EMAIL"), createFund());

        verify(sesClient).sendEmail(any(SendEmailRequest.class));
        verify(snsClient, never()).publish(any(PublishRequest.class));
    }

    @Test
    void notifySubscription_sms_sendsSnsSms() {
        when(snsClient.publish(any(PublishRequest.class))).thenReturn(PublishResponse.builder().build());

        notificationService.notifySubscription(createUser("SMS"), createFund());

        verify(snsClient).publish(any(PublishRequest.class));
        verify(sesClient, never()).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void notifySubscription_unknownPreference_noNotificationSent() {
        notificationService.notifySubscription(createUser("PUSH"), createFund());

        verify(sesClient, never()).sendEmail(any(SendEmailRequest.class));
        verify(snsClient, never()).publish(any(PublishRequest.class));
    }

    @Test
    void notifySubscription_emailFailure_doesNotThrow() {
        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenThrow(new RuntimeException("SES error"));

        assertDoesNotThrow(() -> notificationService.notifySubscription(createUser("EMAIL"), createFund()));
    }

    @Test
    void notifySubscription_smsFailure_doesNotThrow() {
        when(snsClient.publish(any(PublishRequest.class))).thenThrow(new RuntimeException("SNS error"));

        assertDoesNotThrow(() -> notificationService.notifySubscription(createUser("SMS"), createFund()));
    }

    @Test
    void notifySubscription_smsNullPhone_doesNotSend() {
        var user = createUser("SMS");
        user.setPhone(null);

        notificationService.notifySubscription(user, createFund());

        verify(snsClient, never()).publish(any(PublishRequest.class));
    }

    @Test
    void notifySubscription_smsBlankPhone_doesNotSend() {
        var user = createUser("SMS");
        user.setPhone("  ");

        notificationService.notifySubscription(user, createFund());

        verify(snsClient, never()).publish(any(PublishRequest.class));
    }
}
