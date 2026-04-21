package com.ally.blogapp.repository;

import com.ally.blogapp.model.Role;
import com.ally.blogapp.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    Page<User> findByRole(Role role, Pageable pageable);
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    @Query("""
        SELECT u FROM User u
        LEFT JOIN u.posts p
        GROUP BY u
        ORDER BY COUNT(p) DESC
        """)
    List<User> findTopAuthors(Pageable pageable);

    @Query("""
        SELECT COUNT(p) FROM Post p
        WHERE p.author.id = :userId
          AND p.status = com.ally.blogapp.model.PostStatus.PUBLISHED
        """)
    long countPublishedPostsByUserId(@Param("userId") Long userId);
}
