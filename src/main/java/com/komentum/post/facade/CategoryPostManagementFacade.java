package com.komentum.post.facade;

import com.komentum.post.service.CategoryPostService;
import com.komentum.post.service.CategoryService;
import com.komentum.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryPostManagementFacade {

  private final PostService postService;
  private final CategoryService categoryService;
  private final CategoryPostService categoryPostService;
}
