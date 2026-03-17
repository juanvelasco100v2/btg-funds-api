package com.btg.fundmanagement.service;

import com.btg.fundmanagement.dto.Responses;
import com.btg.fundmanagement.entity.Subscription;
import com.btg.fundmanagement.entity.Transaction;
import com.btg.fundmanagement.exception.ApiException;
import com.btg.fundmanagement.repository.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final FundRepository fundRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               FundRepository fundRepository,
                               UserRepository userRepository,
                               TransactionRepository transactionRepository,
                               NotificationService notificationService) {
        this.subscriptionRepository = subscriptionRepository;
        this.fundRepository = fundRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.notificationService = notificationService;
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
        userRepository.save(user);

        var subscription = new Subscription();
        subscription.setUserId(userId);
        subscription.setFundId(fundId);
        subscription.setAmount(fund.getMinimumAmount());
        subscription.setSubscribedAt(Instant.now().toString());
        subscriptionRepository.save(subscription);

        var transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setUserId(userId);
        transaction.setFundId(fundId);
        transaction.setFundName(fund.getName());
        transaction.setType("SUBSCRIBE");
        transaction.setAmount(fund.getMinimumAmount());
        transaction.setCreatedAt(Instant.now().toString());
        transactionRepository.save(transaction);

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
        userRepository.save(user);

        subscriptionRepository.delete(userId, fundId);

        var transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setUserId(userId);
        transaction.setFundId(fundId);
        transaction.setFundName(fund.getName());
        transaction.setType("CANCEL");
        transaction.setAmount(subscription.getAmount());
        transaction.setCreatedAt(Instant.now().toString());
        transactionRepository.save(transaction);

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
