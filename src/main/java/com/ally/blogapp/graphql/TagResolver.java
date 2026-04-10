package com.ally.blogapp.graphql;

import com.ally.blogapp.model.Tag;
import com.ally.blogapp.service.TagService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class TagResolver {

    private final TagService tagService;

    public TagResolver(TagService tagService) {
        this.tagService = tagService;
    }

    @QueryMapping
    public Tag tag(@Argument Long id) {
        return tagService.findById(id);
    }

    @QueryMapping
    public List<Tag> tags() {
        return tagService.findAll();
    }

    @MutationMapping
    public Tag createTag(@Argument String name) {
        return tagService.create(name);
    }

    @MutationMapping
    public Boolean deleteTag(@Argument Long id) {
        tagService.delete(id);
        return true;
    }
}
