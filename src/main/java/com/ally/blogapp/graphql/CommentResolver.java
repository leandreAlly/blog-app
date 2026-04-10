package com.ally.blogapp.graphql;

import com.ally.blogapp.model.Comment;
import com.ally.blogapp.service.CommentService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class CommentResolver {

    private final CommentService commentService;

    public CommentResolver(CommentService commentService) {
        this.commentService = commentService;
    }

    @QueryMapping
    public Comment comment(@Argument Long id) {
        return commentService.findById(id);
    }

    @QueryMapping
    public List<Comment> commentsByPost(@Argument Long postId) {
        return commentService.findByPostId(postId);
    }

    @MutationMapping
    public Comment createComment(@Argument Long postId, @Argument Long userId,
                                 @Argument String content) {
        return commentService.create(postId, userId, content);
    }

    @MutationMapping
    public Comment updateComment(@Argument Long id, @Argument String content) {
        return commentService.update(id, content);
    }

    @MutationMapping
    public Boolean deleteComment(@Argument Long id) {
        commentService.delete(id);
        return true;
    }
}
