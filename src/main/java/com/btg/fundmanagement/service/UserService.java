package com.btg.fundmanagement.service;

import com.btg.fundmanagement.dto.Requests;
import com.btg.fundmanagement.dto.Responses;
import com.btg.fundmanagement.exception.ApiException;
import com.btg.fundmanagement.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Responses.UserInfo getProfile(String userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException.UserNotFound(userId));
        return new Responses.UserInfo(
                user.getUserId(), user.getEmail(), user.getName(),
                user.getBalance(), user.getNotificationPreference(),
                user.getPhone(), user.getRoleIds());
    }

    public Responses.UserInfo updateProfile(String userId, Requests.UpdateProfile request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException.UserNotFound(userId));

        if (request.notificationPreference() != null) {
            user.setNotificationPreference(request.notificationPreference());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }

        userRepository.save(user);

        return new Responses.UserInfo(
                user.getUserId(), user.getEmail(), user.getName(),
                user.getBalance(), user.getNotificationPreference(),
                user.getPhone(), user.getRoleIds());
    }

    public List<Responses.UserInfo> findAll() {
        return userRepository.findAll().stream()
                .map(u -> new Responses.UserInfo(
                        u.getUserId(), u.getEmail(), u.getName(),
                        u.getBalance(), u.getNotificationPreference(),
                        u.getPhone(), u.getRoleIds()))
                .toList();
    }
}
