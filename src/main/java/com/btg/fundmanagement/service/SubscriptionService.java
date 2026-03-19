package com.btg.fundmanagement.service;

import com.btg.fundmanagement.dto.Responses;
import com.btg.fundmanagement.entity.Subscription;
import com.btg.fundmanagement.entity.Transaction;
import com.btg.fundmanagement.entity.User;
import com.btg.fundmanagement.exception.ApiException;
import com.btg.fundmanagement.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final FundRepository fundRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<User> userTable;
    private final DynamoDbTable<Subscription> subscriptionTable;
    private final DynamoDbTable<Transaction> transactionTable;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               FundRepository fundRepository,
                               UserRepository userRepository,
                               TransactionRepository transactionRepository,
                               NotificationService notificationService,
                               DynamoDbEnhancedClient enhancedClient,
                               @Value("${aws.dynamodb.table.users}") String usersTable,
                               @Value("${aws.dynamodb.table.subscriptions}") String subscriptionsTable,
                               @Value("${aws.dynamodb.table.transactions}") String transactionsTable) {
        this.subscriptionRepository = subscriptionRepository;
        this.fundRepository = fundRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.enhancedClient = enhancedClient;
        this.userTable = enhancedClient.table(usersTable, TableSchema.fromBean(User.class));
        this.subscriptionTable = enhancedClient.table(subscriptionsTable, TableSchema.fromBean(Subscription.class));
        this.transactionTable = enhancedClient.table(transactionsTable, TableSchema.fromBean(Transaction.class));
    }

    public Responses.Message subscribe(String userId, String fundId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException.UserNotFound(userId));
        var fund = fundRepository.findById(fundId)
                .orElseThrow(() -> new ApiException.FundNotFound(fundId));

        subscriptionRepository.findByUserAndFund(userId, fundId)
                .ifPresent(_ -> { throw new ApiException.AlreadySubscribed(fund.getName()); });

        if (user.getBalance() < fund.getMinimumAmount()) {
            throw new ApiException.InsufficientBalance(fund.getName());
        }

        user.setBalance(user.getBalance() - fund.getMinimumAmount());

        var subscription = new Subscription();
        subscription.setUserId(userId);
        subscription.setFundId(fundId);
        subscription.setAmount(fund.getMinimumAmount());
        subscription.setSubscribedAt(Instant.now().toString());

        var transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setUserId(userId);
        transaction.setFundId(fundId);
        transaction.setFundName(fund.getName());
        transaction.setType("SUBSCRIBE");
        transaction.setAmount(fund.getMinimumAmount());
        transaction.setCreatedAt(Instant.now().toString());

        enhancedClient.transactWriteItems(TransactWriteItemsEnhancedRequest.builder()
                .addPutItem(userTable, user)
                .addPutItem(subscriptionTable, subscription)
                .addPutItem(transactionTable, transaction)
                .build());

        notificationService.notifySubscription(user, fund);

        return new Responses.Message("Suscrito exitosamente al fondo " + fund.getName()
                + ". Saldo actual: COP $" + String.format("%,d", user.getBalance()));
    }

    public Responses.Message cancel(String userId, String fundId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException.UserNotFound(userId));
        var subscription = subscriptionRepository.findByUserAndFund(userId, fundId)
                .orElseThrow(() -> new ApiException.NotSubscribed(fundId));
        var fund = fundRepository.findById(fundId)
                .orElseThrow(() -> new ApiException.FundNotFound(fundId));

        user.setBalance(user.getBalance() + subscription.getAmount());

        var transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setUserId(userId);
        transaction.setFundId(fundId);
        transaction.setFundName(fund.getName());
        transaction.setType("CANCEL");
        transaction.setAmount(subscription.getAmount());
        transaction.setCreatedAt(Instant.now().toString());

        enhancedClient.transactWriteItems(TransactWriteItemsEnhancedRequest.builder()
                .addPutItem(userTable, user)
                .addDeleteItem(subscriptionTable, Key.builder()
                        .partitionValue(userId)
                        .sortValue(fundId)
                        .build())
                .addPutItem(transactionTable, transaction)
                .build());

        return new Responses.Message("Suscripcion cancelada al fondo " + fund.getName()
                + ". Saldo actual: COP $" + String.format("%,d", user.getBalance()));
    }

    public List<Responses.SubscriptionInfo> findByUser(String userId) {
        return subscriptionRepository.findByUserId(userId).stream()
                .map(s -> {
                    var fundName = fundRepository.findById(s.getFundId())
                            .map(f -> f.getName()).orElse("Desconocido");
                    return new Responses.SubscriptionInfo(
                            s.getUserId(), s.getFundId(), fundName,
                            s.getAmount(), s.getSubscribedAt());
                })
                .toList();
    }
}
