package com.ally.blogapp.service;

import com.ally.blogapp.dao.TagDao;
import com.ally.blogapp.dao.impl.TagDaoImpl;
import com.ally.blogapp.model.Tag;

import java.util.List;
import java.util.Optional;

public class TagService {

    private final TagDao tagDao;

    public TagService() {
        this.tagDao = new TagDaoImpl();
    }

    public Tag create(Tag tag) {
        if (tagDao.findByName(tag.getName()).isPresent()) {
            throw new IllegalArgumentException("Tag already exists");
        }
        return tagDao.save(tag);
    }

    public Tag findOrCreate(String name) {
        return tagDao.findByName(name).orElseGet(() -> tagDao.save(new Tag(name)));
    }

    public Optional<Tag> findById(Long id) {
        return tagDao.findById(id);
    }

    public Optional<Tag> findByName(String name) {
        return tagDao.findByName(name);
    }

    public List<Tag> findAll() {
        return tagDao.findAll();
    }

    public List<Tag> findByPostId(Long postId) {
        return tagDao.findByPostId(postId);
    }

    public void addTagToPost(Long postId, Long tagId) {
        tagDao.addTagToPost(postId, tagId);
    }

    public void removeTagFromPost(Long postId, Long tagId) {
        tagDao.removeTagFromPost(postId, tagId);
    }

    public void delete(Long id) {
        tagDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found"));
        tagDao.delete(id);
    }
}
