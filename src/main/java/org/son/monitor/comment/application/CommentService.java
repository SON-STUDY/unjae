package org.son.monitor.comment.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.son.monitor.comment.domain.Comment;
import org.son.monitor.comment.infrastructure.CommentRepository;
import org.son.monitor.comment.presentation.dto.CommentCreateRequest;
import org.son.monitor.comment.presentation.dto.CommentResponse;
import org.son.monitor.comment.presentation.dto.CommentUpdateRequest;
import org.son.monitor.common.annotation.Logging;
import org.son.monitor.common.exception.BusinessException;
import org.son.monitor.common.exception.ErrorCode;
import org.son.monitor.post.application.PostService;
import org.son.monitor.user.application.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;
    private final UserService userService;

    @Logging
    public List<CommentResponse> findAllByPostId(Long postId) {
        return commentRepository.findAllByPostIdWithAuthor(postId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Logging
    public CommentResponse findById(Long id) {
        return CommentResponse.from(getComment(id));
    }

    @Logging
    @Transactional
    public CommentResponse create(CommentCreateRequest request, Long userId) {
        Comment comment = request.toEntity(
                postService.getPost(request.postId()),
                userService.getUser(userId)
        );
        return CommentResponse.from(commentRepository.save(comment));
    }

    @Logging
    @Transactional
    public CommentResponse update(Long id, CommentUpdateRequest request) {
        Comment comment = getComment(id);
        comment.update(request.content());
        return CommentResponse.from(comment);
    }

    @Logging
    @Transactional
    public void delete(Long id) {
        Comment comment = getComment(id);
        commentRepository.delete(comment);
    }

    private Comment getComment(Long id) {
        return commentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
    }
}
