package org.son.monitor.post.application;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserService userService;

    public List<PostResponse> findAll() {
        return postRepository.findAllWithAuthor().stream()
                .map(PostResponse::from)
                .toList();
    }

    public PostResponse findById(Long id) {
        return PostResponse.from(getPost(id));
    }

    @Transactional
    public PostResponse create(PostCreateRequest request) {
        Post post = request.toEntity(userService.getUser(request.authorId()));
        return PostResponse.from(postRepository.save(post));
    }

    @Transactional
    public PostResponse update(Long id, PostUpdateRequest request) {
        Post post = getPost(id);
        post.update(request.title(), request.content());
        return PostResponse.from(post);
    }

    @Transactional
    public void delete(Long id) {
        postRepository.delete(getPost(id));
    }

    public Post getPost(Long id) {
        return postRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }
}