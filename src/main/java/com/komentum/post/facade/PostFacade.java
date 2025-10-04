package com.komentum.post.facade;

import com.komentum.post.domain.Tag;
import com.komentum.post.dto.PostDto.PostDetail;
import com.komentum.post.dto.PostDto.PostResponse;
import com.komentum.post.dto.PostSummary;
import com.komentum.post.service.PostService;
import com.komentum.post.service.PreferService;
import com.komentum.post.service.TagService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostFacade {
    private PostService postService;
    private PreferService preferService;
    private TagService tagService;

    public PostResponse getPostResponseByPostId(long postId) {
        PostSummary postSummary = postService.getPostSummaryByPostId(postId);
        List<Tag> tags = tagService.getTagsByPostId(postId);
        return PostResponse.from(postSummary, tags);
    }
}
