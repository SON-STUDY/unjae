package org.son.monitor.comment.infrastructure;

import org.son.monitor.comment.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.post.id = :postId")
    List<Comment> findAllByPostIdWithAuthor(Long postId);

    @Query("SELECT c FROM Comment c JOIN FETCH c.author JOIN FETCH c.post WHERE c.id = :id")
    Optional<Comment> findByIdWithDetails(Long id);
}