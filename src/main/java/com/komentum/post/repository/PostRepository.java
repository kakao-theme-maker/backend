package com.komentum.post.repository;

import com.komentum.post.domain.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

  //업로드 수 count
  int countByUser_PublicUserId(String userPublicUserId);

  List<Post> findByUser_PublicUserId(String publicUserId);

}
