package org.son.monitor.user.application;

import lombok.RequiredArgsConstructor;
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
        return UserResponse.from(userRepository.save(request.toEntity()));
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = getUser(id);
        user.update(request.name());
        return UserResponse.from(user);
    }

    @Transactional
    public void delete(Long id) {
        userRepository.delete(getUser(id));
    }

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}