package org.son.monitor.comment.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.son.monitor.comment.application.CommentService;
import org.son.monitor.comment.presentation.dto.CommentCreateRequest;
import org.son.monitor.comment.presentation.dto.CommentResponse;
import org.son.monitor.comment.presentation.dto.CommentUpdateRequest;
import org.son.monitor.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ApiResponse<List<CommentResponse>> findAllByPostId(@RequestParam Long postId) {
        return ApiResponse.ok(commentService.findAllByPostId(postId));
    }

    @GetMapping("/{id}")
    public ApiResponse<CommentResponse> findById(@PathVariable Long id) {
        return ApiResponse.ok(commentService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentResponse> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CommentCreateRequest request) {
        return ApiResponse.created(commentService.create(request, userId));
    }

    @PutMapping("/{id}")
    public ApiResponse<CommentResponse> update(@PathVariable Long id, @Valid @RequestBody CommentUpdateRequest request) {
        return ApiResponse.ok(commentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        commentService.delete(id);
        return ApiResponse.noContent();
    }
}