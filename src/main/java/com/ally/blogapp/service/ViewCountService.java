package com.ally.blogapp.service;

import com.ally.blogapp.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Thread-safe per-post view counter.
 *
 * Reads on /api/posts/{id} are a hot path; persisting one UPDATE per view
 * would serialize on the row lock and dwarf the cost of the read itself.
 * Instead, increments hit an in-memory ConcurrentHashMap<Long, LongAdder>
 * — both data structures support unsynchronized concurrent updates — and
 * a scheduled flush drains accumulated deltas to the DB in one UPDATE per
 * post. LongAdder is preferred over AtomicLong here because under
 * contention it striped-counts internally, trading exact mid-flight reads
 * for far better write throughput.
 */
@Service
public class ViewCountService {

    private static final Logger log = LoggerFactory.getLogger(ViewCountService.class);

    private final PostRepository postRepository;
    private final ConcurrentHashMap<Long, LongAdder> counters = new ConcurrentHashMap<>();

    public ViewCountService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public void record(Long postId) {
        counters.computeIfAbsent(postId, k -> new LongAdder()).increment();
    }

    public long currentDelta(Long postId) {
        LongAdder a = counters.get(postId);
        return a == null ? 0L : a.sum();
    }

    /**
     * Drain and persist every 30 seconds. Atomically reads-and-removes
     * each entry so concurrent record() calls during the flush land in
     * a fresh LongAdder for the next window rather than being lost.
     */
    @Scheduled(fixedDelayString = "${blog.view-count.flush-ms:30000}")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void flush() {
        if (counters.isEmpty()) return;
        int updated = 0;
        for (Map.Entry<Long, LongAdder> entry : counters.entrySet()) {
            LongAdder adder = counters.remove(entry.getKey());
            if (adder == null) continue;
            long delta = adder.sumThenReset();
            if (delta == 0L) continue;
            try {
                if (postRepository.addViewCount(entry.getKey(), delta) > 0) {
                    updated++;
                } else {
                    log.debug("view-count flush: post {} no longer exists, dropping {} views",
                            entry.getKey(), delta);
                }
            } catch (RuntimeException ex) {
                counters.computeIfAbsent(entry.getKey(), k -> new LongAdder()).add(delta);
                log.warn("view-count flush failed for post {}, returning {} to buffer", entry.getKey(), delta, ex);
            }
        }
        if (updated > 0) log.debug("view-count flush: persisted updates for {} posts", updated);
    }
}
