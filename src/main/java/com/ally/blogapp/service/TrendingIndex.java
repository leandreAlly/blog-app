package com.ally.blogapp.service;

import com.ally.blogapp.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * In-memory trending index.
 *
 * The native trending query is moderately expensive (left-joins comments
 * and reviews, groups by post, sorts) and serves a hot read endpoint
 * (/api/posts/trending). Running it on every request burns DB time on
 * a result that barely changes minute to minute. This component:
 *
 *   1. Pulls raw engagement counts once per refresh window.
 *   2. Combines DB counts with the live view delta from ViewCountService —
 *      something the SQL alone cannot see — to produce a current score.
 *   3. Picks the top K via a bounded min-heap (O(N log K) instead of the
 *      O(N log N) full sort the SQL would do).
 *   4. Publishes the result as a volatile snapshot of post IDs; readers
 *      lock-free.
 *
 * The capacity (100) is the largest top-K we will serve from the cache;
 * larger requests fall back to the DB query.
 */
@Service
public class TrendingIndex {

    private static final Logger log = LoggerFactory.getLogger(TrendingIndex.class);
    private static final int CAPACITY = 100;

    private final PostRepository postRepository;
    private final ViewCountService viewCountService;

    private volatile List<Long> snapshot = List.of();

    public TrendingIndex(PostRepository postRepository, ViewCountService viewCountService) {
        this.postRepository = postRepository;
        this.viewCountService = viewCountService;
    }

    public List<Long> topIds(int limit) {
        List<Long> snap = snapshot;
        if (snap.isEmpty() || limit > snap.size()) return List.of();
        return snap.subList(0, limit);
    }

    public int capacity() {
        return CAPACITY;
    }

    @Scheduled(fixedDelayString = "${blog.trending.refresh-ms:60000}", initialDelay = 5000)
    @Transactional(readOnly = true)
    public void refresh() {
        List<Object[]> rows = postRepository.findTrendingCandidates();
        if (rows.isEmpty()) {
            snapshot = List.of();
            return;
        }

        // Min-heap of size <= CAPACITY, ordered by score ascending so the
        // smallest (worst) is always at the head and is the one we evict.
        PriorityQueue<Scored> minHeap = new PriorityQueue<>(
                CAPACITY, Comparator.comparingDouble(Scored::score));

        for (Object[] row : rows) {
            Long id = ((Number) row[0]).longValue();
            long comments = ((Number) row[1]).longValue();
            long reviews = ((Number) row[2]).longValue();
            long views = ((Number) row[3]).longValue() + viewCountService.currentDelta(id);

            double score = score(comments, reviews, views);

            if (minHeap.size() < CAPACITY) {
                minHeap.offer(new Scored(id, score));
            } else if (score > minHeap.peek().score()) {
                minHeap.poll();
                minHeap.offer(new Scored(id, score));
            }
        }

        // Drain heap and reverse-sort to get descending score order.
        List<Scored> top = new ArrayList<>(minHeap);
        top.sort(Comparator.comparingDouble(Scored::score).reversed());

        List<Long> ids = new ArrayList<>(top.size());
        for (Scored s : top) ids.add(s.id());
        snapshot = List.copyOf(ids);

        log.debug("trending index refreshed: {} candidates → top {} cached", rows.size(), ids.size());
    }

    private static double score(long comments, long reviews, long views) {
        return comments * 2.0 + reviews * 3.0 + Math.log1p(views);
    }

    private record Scored(Long id, double score) {}
}
