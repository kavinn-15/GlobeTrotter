package com.example.loginapp.repository;

import com.example.loginapp.entity.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link CommunityPost}. Hibernate is the
 * underlying JPA provider; Spring Data generates the implementation at
 * runtime.
 */
public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    List<CommunityPost> findAllByOrderByCreatedAtDesc();

    List<CommunityPost> findByTitleContainingIgnoreCaseOrPlaceContainingIgnoreCaseOrderByCreatedAtDesc(
            String title, String place);
}
