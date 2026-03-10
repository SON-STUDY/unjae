package org.son.monitor.post.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.son.monitor.common.response.ApiResponse;
import org.son.monitor.post.application.PostService;
import org.son.monitor.post.presentation.dto.PostCreateRequest;
import org.son.monitor.post.presentation.dto.PostResponse;
import org.son.monitor.post.presentation.dto.PostUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ApiResponse<List<PostResponse>> findAll() {
        return ApiResponse.ok(postService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<PostResponse> findById(@PathVariable Long id) {
        return ApiResponse.ok(postService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostResponse> create(@Valid @RequestBody PostCreateRequest request) {
        return ApiResponse.created(postService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PostResponse> update(@PathVariable Long id, @Valid @RequestBody PostUpdateRequest request) {
        return ApiResponse.ok(postService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return ApiResponse.noContent();
    }
}