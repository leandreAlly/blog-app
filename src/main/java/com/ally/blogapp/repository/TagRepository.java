package com.ally.blogapp.repository;

import com.ally.blogapp.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
    boolean existsByName(String name);

    Page<Tag> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("""
        SELECT t FROM Tag t
        LEFT JOIN t.posts p
        GROUP BY t
        ORDER BY COUNT(p) DESC
        """)
    List<Tag> findPopular(Pageable pageable);

    @Query("SELECT t FROM Tag t JOIN t.posts p WHERE p.id = :postId")
    List<Tag> findByPostId(@Param("postId") Long postId);
}
