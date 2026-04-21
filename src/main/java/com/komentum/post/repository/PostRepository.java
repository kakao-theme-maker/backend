package com.komentum.post.repository;

import com.komentum.post.domain.Post;
import com.komentum.post.domain.enums.PostType;
import com.komentum.user.domain.User;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

  //업로드 수 count
  int countByUser_PublicUserId(String userPublicUserId);

  List<Post> findByUser_PublicUserId(String publicUserId);

  List<Post> findByUserIn(Collection<User> users);

  Optional<Post> findByPostIdAndPostType(Long postId, PostType postType);
}
