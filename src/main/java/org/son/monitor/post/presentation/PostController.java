package org.son.monitor.post.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.son.monitor.common.response.ApiResponse;
import org.son.monitor.post.application.PostService;
import org.son.monitor.post.presentation.dto.PostCreateRequest;
import org.son.monitor.post.presentation.dto.PostResponse;
import org.son.monitor.post.presentation.dto.PostUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ApiResponse<Page<PostResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(postService.findAll(page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<PostResponse> findById(@PathVariable Long id) {
        return ApiResponse.ok(postService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostResponse> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody PostCreateRequest request) {
        return ApiResponse.created(postService.create(request, userId));
    }

    @PutMapping("/{id}")
    public ApiResponse<PostResponse> update(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody PostUpdateRequest request) {
        return ApiResponse.ok(postService.update(id, userId, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> delete(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        postService.delete(id, userId);
        return ApiResponse.noContent();
    }
}