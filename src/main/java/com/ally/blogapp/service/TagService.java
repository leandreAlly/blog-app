package com.ally.blogapp.service;

import com.ally.blogapp.exception.DuplicateResourceException;
import com.ally.blogapp.exception.ResourceNotFoundException;
import com.ally.blogapp.model.Tag;
import com.ally.blogapp.config.CacheConfig;
import com.ally.blogapp.repository.TagRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.POPULAR_TAGS, allEntries = true),
            @CacheEvict(value = CacheConfig.TAGS, allEntries = true)
    })
    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public Tag create(String name) {
        if (tagRepository.existsByName(name)) {
            throw new DuplicateResourceException("Tag already exists: " + name);
        }
        return tagRepository.save(new Tag(name));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public Tag findOrCreate(String name) {
        return tagRepository.findByName(name)
                .orElseGet(() -> tagRepository.save(new Tag(name)));
    }

    @Cacheable(value = CacheConfig.TAGS, key = "#id")
    public Tag findById(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));
    }

    @Cacheable(value = CacheConfig.TAGS, key = "'name:' + #name")
    public Tag findByName(String name) {
        return tagRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found: " + name));
    }

    public List<Tag> findAll() {
        return tagRepository.findAll();
    }

    public Page<Tag> findAll(Pageable pageable) {
        return tagRepository.findAll(pageable);
    }

    public Page<Tag> search(String name, Pageable pageable) {
        return tagRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Cacheable(value = CacheConfig.POPULAR_TAGS,
               key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public List<Tag> findPopular(Pageable pageable) {
        return tagRepository.findPopular(pageable);
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.TAGS, allEntries = true),
            @CacheEvict(value = CacheConfig.POPULAR_TAGS, allEntries = true)
    })
    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public void delete(Long id) {
        findById(id);
        tagRepository.deleteById(id);
    }
}
