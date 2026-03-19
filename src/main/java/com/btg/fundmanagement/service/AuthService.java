package com.btg.fundmanagement.service;

import com.btg.fundmanagement.dto.Requests;
import com.btg.fundmanagement.dto.Responses;
import com.btg.fundmanagement.entity.User;
import com.btg.fundmanagement.exception.ApiException;
import com.btg.fundmanagement.repository.UserRepository;
import com.btg.fundmanagement.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long initialBalance;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       @Value("${app.initial-balance}") long initialBalance) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.initialBalance = initialBalance;
    }

    public Responses.Auth register(Requests.Register request) {
        userRepository.findByEmail(request.email())
                .ifPresent(_ -> { throw new ApiException.EmailAlreadyExists(); });

        var user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setEmail(request.email());
        user.setName(request.name());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setBalance(initialBalance);
        user.setNotificationPreference(
                request.notificationPreference() != null ? request.notificationPreference() : "EMAIL");
        user.setPhone(request.phone());
        user.setRoleIds(Set.of("CLIENT"));
        user.setCreatedAt(Instant.now().toString());

        userRepository.save(user);

        var token = jwtService.generateToken(user.getUserId(), user.getEmail(), user.getRoleIds());
        return new Responses.Auth(token, user.getUserId(), user.getEmail(), user.getRoleIds());
    }

    public Responses.Auth setupAdmin(Requests.SetupAdmin request) {
        if (userRepository.hasAnyAdmin()) {
            throw new ApiException.AdminAlreadyExists();
        }

        userRepository.findByEmail(request.email())
                .ifPresent(_ -> { throw new ApiException.EmailAlreadyExists(); });

        var user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setEmail(request.email());
        user.setName(request.name());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setBalance(initialBalance);
        user.setNotificationPreference("EMAIL");
        user.setPhone(request.phone());
        user.setRoleIds(Set.of("ADMIN", "CLIENT"));
        user.setCreatedAt(Instant.now().toString());

        userRepository.save(user);

        var token = jwtService.generateToken(user.getUserId(), user.getEmail(), user.getRoleIds());
        return new Responses.Auth(token, user.getUserId(), user.getEmail(), user.getRoleIds());
    }

    public Responses.Auth login(Requests.Login request) {
        var user = userRepository.findByEmail(request.email())
                .orElseThrow(ApiException.InvalidCredentials::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ApiException.InvalidCredentials();
        }

        var token = jwtService.generateToken(user.getUserId(), user.getEmail(), user.getRoleIds());
        return new Responses.Auth(token, user.getUserId(), user.getEmail(), user.getRoleIds());
    }
}
