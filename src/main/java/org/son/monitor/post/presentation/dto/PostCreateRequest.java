package org.son.monitor.post.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import org.son.monitor.post.domain.Post;
import org.son.monitor.user.domain.User;

public record PostCreateRequest(

        @NotBlank(message = "제목은 필수입니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        String content
) {
    public Post toEntity(User author) {
        return Post.builder()
                .title(title)
                .content(content)
                .author(author)
                .build();
    }
}