package com.btg.fundmanagement.service;

import com.btg.fundmanagement.entity.Fund;
import com.btg.fundmanagement.entity.Subscription;
import com.btg.fundmanagement.entity.User;
import com.btg.fundmanagement.exception.ApiException;
import com.btg.fundmanagement.repository.FundRepository;
import com.btg.fundmanagement.repository.SubscriptionRepository;
import com.btg.fundmanagement.repository.TransactionRepository;
import com.btg.fundmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private FundRepository fundRepository;
    @Mock private UserRepository userRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private NotificationService notificationService;
    @Mock private DynamoDbEnhancedClient enhancedClient;
    @Mock private DynamoDbTable<User> userTable;
    @Mock private DynamoDbTable<Subscription> subscriptionTable;
    @Mock private DynamoDbTable<com.btg.fundmanagement.entity.Transaction> transactionTable;

    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        lenient().when(enhancedClient.table(eq("users"), any())).thenReturn((DynamoDbTable) userTable);
        lenient().when(enhancedClient.table(eq("subscriptions"), any())).thenReturn((DynamoDbTable) subscriptionTable);
        lenient().when(enhancedClient.table(eq("transactions"), any())).thenReturn((DynamoDbTable) transactionTable);
        lenient().when(userTable.tableSchema()).thenReturn((TableSchema) TableSchema.fromBean(User.class));
        lenient().when(userTable.tableName()).thenReturn("users");
        lenient().when(subscriptionTable.tableSchema()).thenReturn((TableSchema) TableSchema.fromBean(Subscription.class));
        lenient().when(subscriptionTable.tableName()).thenReturn("subscriptions");
        lenient().when(transactionTable.tableSchema()).thenReturn((TableSchema) TableSchema.fromBean(com.btg.fundmanagement.entity.Transaction.class));
        lenient().when(transactionTable.tableName()).thenReturn("transactions");

        subscriptionService = new SubscriptionService(
                subscriptionRepository, fundRepository, userRepository,
                transactionRepository, notificationService, enhancedClient,
                "users", "subscriptions", "transactions");
    }

    private User createUser() {
        var user = new User();
        user.setUserId("u1");
        user.setEmail("test@test.com");
        user.setName("Test");
        user.setBalance(500000);
        user.setNotificationPreference("EMAIL");
        user.setRoleIds(Set.of("CLIENT"));
        return user;
    }

    private Fund createFund() {
        var fund = new Fund();
        fund.setFundId("f1");
        fund.setName("FPV_RECAUDADORA");
        fund.setMinimumAmount(75000);
        fund.setCategory("FPV");
        return fund;
    }

    private Subscription createSubscription() {
        var sub = new Subscription();
        sub.setUserId("u1");
        sub.setFundId("f1");
        sub.setAmount(75000);
        sub.setSubscribedAt("2024-01-01T00:00:00Z");
        return sub;
    }

    @Test
    void subscribe_success() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(createUser()));
        when(fundRepository.findById("f1")).thenReturn(Optional.of(createFund()));
        when(subscriptionRepository.findByUserAndFund("u1", "f1")).thenReturn(Optional.empty());

        var result = subscriptionService.subscribe("u1", "f1");

        assertNotNull(result);
        assertTrue(result.message().contains("Suscrito exitosamente"));
        assertTrue(result.message().contains("FPV_RECAUDADORA"));
        verify(enhancedClient).transactWriteItems(any(TransactWriteItemsEnhancedRequest.class));
        verify(notificationService).notifySubscription(any(User.class), any(Fund.class));
    }

    @Test
    void subscribe_userNotFound_throwsException() {
        when(userRepository.findById("u1")).thenReturn(Optional.empty());

        assertThrows(ApiException.UserNotFound.class, () -> subscriptionService.subscribe("u1", "f1"));
    }

    @Test
    void subscribe_fundNotFound_throwsException() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(createUser()));
        when(fundRepository.findById("f1")).thenReturn(Optional.empty());

        assertThrows(ApiException.FundNotFound.class, () -> subscriptionService.subscribe("u1", "f1"));
    }

    @Test
    void subscribe_alreadySubscribed_throwsException() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(createUser()));
        when(fundRepository.findById("f1")).thenReturn(Optional.of(createFund()));
        when(subscriptionRepository.findByUserAndFund("u1", "f1")).thenReturn(Optional.of(createSubscription()));

        assertThrows(ApiException.AlreadySubscribed.class, () -> subscriptionService.subscribe("u1", "f1"));
    }

    @Test
    void subscribe_insufficientBalance_throwsException() {
        var user = createUser();
        user.setBalance(10000);
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(fundRepository.findById("f1")).thenReturn(Optional.of(createFund()));
        when(subscriptionRepository.findByUserAndFund("u1", "f1")).thenReturn(Optional.empty());

        assertThrows(ApiException.InsufficientBalance.class, () -> subscriptionService.subscribe("u1", "f1"));
    }

    @Test
    void cancel_success() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(createUser()));
        when(subscriptionRepository.findByUserAndFund("u1", "f1")).thenReturn(Optional.of(createSubscription()));
        when(fundRepository.findById("f1")).thenReturn(Optional.of(createFund()));

        var result = subscriptionService.cancel("u1", "f1");

        assertNotNull(result);
        assertTrue(result.message().contains("Suscripcion cancelada"));
        verify(enhancedClient).transactWriteItems(any(TransactWriteItemsEnhancedRequest.class));
    }

    @Test
    void cancel_userNotFound_throwsException() {
        when(userRepository.findById("u1")).thenReturn(Optional.empty());

        assertThrows(ApiException.UserNotFound.class, () -> subscriptionService.cancel("u1", "f1"));
    }

    @Test
    void cancel_notSubscribed_throwsException() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(createUser()));
        when(subscriptionRepository.findByUserAndFund("u1", "f1")).thenReturn(Optional.empty());

        assertThrows(ApiException.NotSubscribed.class, () -> subscriptionService.cancel("u1", "f1"));
    }

    @Test
    void findByUser_returnsMappedSubscriptions() {
        var sub = createSubscription();
        when(subscriptionRepository.findByUserId("u1")).thenReturn(List.of(sub));
        when(fundRepository.findById("f1")).thenReturn(Optional.of(createFund()));

        var result = subscriptionService.findByUser("u1");

        assertEquals(1, result.size());
        assertEquals("u1", result.getFirst().userId());
        assertEquals("f1", result.getFirst().fundId());
        assertEquals("FPV_RECAUDADORA", result.getFirst().fundName());
        assertEquals(75000, result.getFirst().amount());
    }

    @Test
    void findByUser_fundNotFound_showsDesconocido() {
        var sub = createSubscription();
        when(subscriptionRepository.findByUserId("u1")).thenReturn(List.of(sub));
        when(fundRepository.findById("f1")).thenReturn(Optional.empty());

        var result = subscriptionService.findByUser("u1");

        assertEquals("Desconocido", result.getFirst().fundName());
    }

    @Test
    void findByUser_emptyList() {
        when(subscriptionRepository.findByUserId("u1")).thenReturn(List.of());

        var result = subscriptionService.findByUser("u1");

        assertTrue(result.isEmpty());
    }
}
