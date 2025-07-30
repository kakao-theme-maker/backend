package com.theme.post.repository;

import com.theme.post.domain.Tag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

  List<Tag> findAllByPost_PostId(Long postPostId);
}
