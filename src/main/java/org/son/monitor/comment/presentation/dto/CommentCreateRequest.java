package org.son.monitor.comment.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.son.monitor.comment.domain.Comment;
import org.son.monitor.post.domain.Post;
import org.son.monitor.user.domain.User;

public record CommentCreateRequest(

        @NotNull(message = "게시글 ID는 필수입니다.")
        Long postId,

        @NotBlank(message = "내용은 필수입니다.")
        String content,

        @NotNull(message = "작성자 ID는 필수입니다.")
        Long authorId
) {
    public Comment toEntity(Post post, User author) {
        return Comment.builder()
                .post(post)
                .content(content)
                .author(author)
                .build();
    }
}
