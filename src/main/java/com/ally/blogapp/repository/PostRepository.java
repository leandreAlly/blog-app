package com.ally.blogapp.repository;

import com.ally.blogapp.model.Post;
import com.ally.blogapp.model.PostStats;
import com.ally.blogapp.model.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN p.tags t WHERE t.id = :tagId AND p.status = com.ally.blogapp.model.PostStatus.PUBLISHED ORDER BY p.createdAt DESC")
    Page<Post> findPublishedByTagId(@Param("tagId") Long tagId, Pageable pageable);

    @Query(value = "SELECT * FROM posts WHERE search_vector @@ websearch_to_tsquery('english', :keyword)",
           countQuery = "SELECT count(*) FROM posts WHERE search_vector @@ websearch_to_tsquery('english', :keyword)",
           nativeQuery = true)
    Page<Post> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
        SELECT new com.ally.blogapp.model.PostStats(
            p.id, p.title, p.status, p.createdAt, p.publishedAt,
            COUNT(DISTINCT c.id), COUNT(DISTINCT r.id), COALESCE(AVG(r.rating), 0.0)
        )
        FROM Post p
        LEFT JOIN p.comments c
        LEFT JOIN p.reviews r
        WHERE p.author.id = :authorId
        GROUP BY p.id, p.title, p.status, p.createdAt, p.publishedAt
        ORDER BY p.createdAt DESC
        """)
    List<PostStats> getStatsByAuthorId(@Param("authorId") Long authorId);

    @Query(value = """
        SELECT p.* FROM posts p
        LEFT JOIN comments c ON c.post_id = p.id AND c.created_at >= NOW() - INTERVAL '7 days'
        LEFT JOIN reviews  r ON r.post_id = p.id AND r.created_at >= NOW() - INTERVAL '7 days'
        WHERE p.status = 'PUBLISHED'
          AND p.published_at >= NOW() - INTERVAL '30 days'
        GROUP BY p.id
        ORDER BY (COUNT(DISTINCT c.id) * 2 + COUNT(DISTINCT r.id) * 3) DESC,
                 p.published_at DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Post> findTrending(@Param("limit") int limit);
}
