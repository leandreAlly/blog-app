package com.ally.blogapp.repository;

import com.ally.blogapp.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
    List<Comment> findByUserId(Long userId);

    Page<Comment> findByPostId(Long postId, Pageable pageable);
    Page<Comment> findByUserId(Long userId, Pageable pageable);
    long countByPostId(Long postId);

    @Query("""
        SELECT c FROM Comment c
        JOIN c.user u
        WHERE LOWER(u.username) = LOWER(:username)
        ORDER BY c.createdAt DESC
        """)
    Page<Comment> findByAuthorName(@Param("username") String username, Pageable pageable);

    @Query("SELECT c FROM Comment c ORDER BY c.createdAt DESC")
    List<Comment> findRecent(Pageable pageable);
}
