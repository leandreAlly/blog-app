package com.ally.blogapp.repository;

import com.ally.blogapp.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByPostId(Long postId);
    List<Review> findByUserId(Long userId);
    Optional<Review> findByPostIdAndUserId(Long postId, Long userId);
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    Page<Review> findByPostId(Long postId, Pageable pageable);
    Page<Review> findByUserId(Long userId, Pageable pageable);
    long countByPostId(Long postId);

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.post.id = :postId")
    double getAverageRatingByPostId(@Param("postId") Long postId);

    @Query("""
        SELECT r FROM Review r
        JOIN r.post p
        WHERE p.status = com.ally.blogapp.model.PostStatus.PUBLISHED
        ORDER BY r.rating DESC, r.createdAt DESC
        """)
    Page<Review> findTopRated(Pageable pageable);
}
