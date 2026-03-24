package org.son.monitor.user.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.son.monitor.common.annotation.Logging;
import org.son.monitor.common.exception.BusinessException;
import org.son.monitor.common.exception.ErrorCode;
import org.son.monitor.user.domain.User;
import org.son.monitor.user.infrastructure.UserRepository;
import org.son.monitor.user.presentation.dto.UserCreateRequest;
import org.son.monitor.user.presentation.dto.UserResponse;
import org.son.monitor.user.presentation.dto.UserUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Logging
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Logging
    public UserResponse findById(Long id) {
        return UserResponse.from(getUser(id));
    }

    @Logging
    @Transactional
    public UserResponse create(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        return UserResponse.from(userRepository.save(request.toEntity()));
    }

    @Logging
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = getUser(id);
        user.update(request.name());
        return UserResponse.from(user);
    }

    @Logging
    @Transactional
    public void delete(Long id) {
        userRepository.delete(getUser(id));
    }

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
