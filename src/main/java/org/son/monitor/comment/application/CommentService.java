package org.son.monitor.comment.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.son.monitor.comment.domain.Comment;
import org.son.monitor.comment.infrastructure.CommentRepository;
import org.son.monitor.comment.presentation.dto.CommentCreateRequest;
import org.son.monitor.comment.presentation.dto.CommentResponse;
import org.son.monitor.comment.presentation.dto.CommentUpdateRequest;
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

    public List<CommentResponse> findAllByPostId(Long postId) {
        return commentRepository.findAllByPostIdWithAuthor(postId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    public CommentResponse findById(Long id) {
        return CommentResponse.from(getComment(id));
    }

    @Transactional
    public CommentResponse create(CommentCreateRequest request, Long userId) {
        Comment comment = request.toEntity(
                postService.getPost(request.postId()),
                userService.getUser(userId)
        );
        CommentResponse response = CommentResponse.from(commentRepository.save(comment));
        log.info("comment.created commentId={} postId={} userId={}", response.id(), response.postId(), userId);
        return response;
    }

    @Transactional
    public CommentResponse update(Long id, CommentUpdateRequest request) {
        Comment comment = getComment(id);
        comment.update(request.content());
        CommentResponse response = CommentResponse.from(comment);
        log.info("comment.updated commentId={} postId={}", id, response.postId());
        return response;
    }

    @Transactional
    public void delete(Long id) {
        Comment comment = getComment(id);
        Long postId = comment.getPost().getId();
        commentRepository.delete(comment);
        log.info("comment.deleted commentId={} postId={}", id, postId);
    }

    private Comment getComment(Long id) {
        return commentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
    }
}