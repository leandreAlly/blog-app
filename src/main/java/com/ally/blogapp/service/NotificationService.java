package com.ally.blogapp.service;

import com.ally.blogapp.config.AsyncConfig;
import com.ally.blogapp.event.CommentCreatedEvent;
import com.ally.blogapp.event.PostPublishedEvent;
import com.ally.blogapp.event.ReviewCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    /**
     * Fires AFTER_COMMIT and on the notification pool, so a rolled-back
     * publish never produces a phantom notification and the request thread
     * is freed before the (potentially slow) dispatch runs.
     */
    @Async(AsyncConfig.NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPostPublished(PostPublishedEvent event) {
        simulateDispatch();
        log.info("notification: post {} '{}' by author {} published",
                event.postId(), event.title(), event.authorId());
    }

    @Async(AsyncConfig.NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCommentCreated(CommentCreatedEvent event) {
        simulateDispatch();
        log.info("notification: comment {} on post {} by user {}",
                event.commentId(), event.postId(), event.userId());
    }

    @Async(AsyncConfig.NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReviewCreated(ReviewCreatedEvent event) {
        simulateDispatch();
        log.info("notification: review {} on post {} rating={} by user {}",
                event.reviewId(), event.postId(), event.rating(), event.userId());
    }

    private void simulateDispatch() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
