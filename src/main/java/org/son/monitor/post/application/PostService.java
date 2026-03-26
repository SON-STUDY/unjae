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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Page<PostResponse> findAll(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.findAllWithAuthor(pageable).map(PostResponse::from);
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
    public PostResponse update(Long id, Long userId, PostUpdateRequest request) {
        Post post = getPost(id);
        if (!post.getAuthor().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.POST_FORBIDDEN);
        }
        post.update(request.title(), request.content());
        return PostResponse.from(post);
    }

    @Logging
    @Transactional
    public void delete(Long id, Long userId) {
        Post post = getPost(id);
        if (!post.getAuthor().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.POST_FORBIDDEN);
        }
        postRepository.delete(post);
        postDeletedCounter.increment();
    }

    public Post getPost(Long id) {
        return postRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }
}
