package com.wisewealth.service;

import com.wisewealth.dto.UserDto;
import com.wisewealth.dto.UserUpdateRequest;
import com.wisewealth.entity.User;
import com.wisewealth.exception.ResourceNotFoundException;
import com.wisewealth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    public UserDto getUserById(Long userId) {
        log.info("Fetching user with id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return mapToUserDto(user);
    }

    @Transactional
    public UserDto updateUser(Long userId, UserUpdateRequest request) {
        log.info("Updating user with id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", userId);
        return mapToUserDto(updatedUser);
    }

    @Transactional
    public void deactivateUser(Long userId) {
        log.info("Deactivating user with id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setIsActive(false);
        userRepository.save(user);
        log.info("User deactivated: {}", userId);
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user with id: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
        log.info("User deleted: {}", userId);
    }

    public Page<UserDto> getAllUsers(Pageable pageable) {
        log.info("Fetching all users with pagination");
        return userRepository.findAll(pageable)
                .map(this::mapToUserDto);
    }

    public Page<UserDto> getActiveUsers(Pageable pageable) {
        log.info("Fetching active users");
        return userRepository.findByIsActive(true, pageable)
                .map(this::mapToUserDto);
    }

    @Transactional
    public void updateUserStatus(Long userId, Boolean isActive) {
        log.info("Updating user status for id: {}, isActive: {}", userId, isActive);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setIsActive(isActive);
        userRepository.save(user);
        log.info("User status updated: {}", userId);
    }

    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
