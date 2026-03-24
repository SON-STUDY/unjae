package org.son.monitor.post.application;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.son.monitor.common.annotation.Logging;
import org.son.monitor.common.exception.BusinessException;
import org.son.monitor.common.exception.ErrorCode;
import org.son.monitor.post.domain.Post;
import org.son.monitor.post.infrastructure.PostRepository;
import org.son.monitor.post.presentation.dto.PostCreateRequest;
import org.son.monitor.post.presentation.dto.PostResponse;
import org.son.monitor.post.presentation.dto.PostUpdateRequest;
import org.son.monitor.user.application.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserService userService;
    private final Counter postCreatedCounter;
    private final Counter postDeletedCounter;

    public PostService(PostRepository postRepository, UserService userService, MeterRegistry registry) {
        this.postRepository = postRepository;
        this.userService = userService;
        this.postCreatedCounter = Counter.builder("business.post.created.total")
                .description("게시글 생성 누적 수")
                .register(registry);
        this.postDeletedCounter = Counter.builder("business.post.deleted.total")
                .description("게시글 삭제 누적 수")
                .register(registry);
    }

    @Logging
    public List<PostResponse> findAll() {
        return postRepository.findAllWithAuthor().stream()
                .map(PostResponse::from)
                .toList();
    }

    @Logging
    public PostResponse findById(Long id) {
        return PostResponse.from(getPost(id));
    }

    @Logging
    @Transactional
    public PostResponse create(PostCreateRequest request, Long userId) {
        Post post = request.toEntity(userService.getUser(userId));
        PostResponse response = PostResponse.from(postRepository.save(post));
        postCreatedCounter.increment();
        return response;
    }

    @Logging
    @Transactional
    public PostResponse update(Long id, PostUpdateRequest request) {
        Post post = getPost(id);
        post.update(request.title(), request.content());
        return PostResponse.from(post);
    }

    @Logging
    @Transactional
    public void delete(Long id) {
        postRepository.delete(getPost(id));
        postDeletedCounter.increment();
    }

    public Post getPost(Long id) {
        return postRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }
}
