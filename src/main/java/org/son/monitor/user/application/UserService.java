package org.son.monitor.user.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse findById(Long id) {
        return UserResponse.from(getUser(id));
    }

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        UserResponse response = UserResponse.from(userRepository.save(request.toEntity()));
        log.info("user.created userId={} email={}", response.id(), response.email());
        return response;
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = getUser(id);
        user.update(request.name());
        UserResponse response = UserResponse.from(user);
        log.info("user.updated userId={} name={}", id, response.name());
        return response;
    }

    @Transactional
    public void delete(Long id) {
        userRepository.delete(getUser(id));
        log.info("user.deleted userId={}", id);
    }

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}