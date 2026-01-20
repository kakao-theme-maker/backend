package com.komentum.post.repository;

import com.komentum.post.domain.CategoryPost;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryPostRepository extends JpaRepository<CategoryPost, Long> {

  Optional<CategoryPost> findByCategory_CategoryIdAndPost_PostId(long categoryId, long postId);
}
