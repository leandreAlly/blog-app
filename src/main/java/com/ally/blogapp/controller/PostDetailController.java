package com.ally.blogapp.controller;

import com.ally.blogapp.model.*;
import com.ally.blogapp.service.*;
import com.ally.blogapp.util.SceneManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class PostDetailController {

    @FXML private Label titleLabel;
    @FXML private Label authorLabel;
    @FXML private Label dateLabel;
    @FXML private Label statusLabel;
    @FXML private Label tagsLabel;
    @FXML private Label contentLabel;
    @FXML private Label avgRatingLabel;

    // Comments
    @FXML private VBox commentsContainer;
    @FXML private TextArea commentInput;
    @FXML private Label commentError;

    // Reviews
    @FXML private VBox reviewsContainer;
    @FXML private ComboBox<Integer> ratingCombo;
    @FXML private TextArea reviewInput;
    @FXML private Label reviewError;
    @FXML private VBox reviewFormBox;

    private DashboardController dashboardController;
    private final PostService postService = new PostService();
    private final UserService userService = new UserService();
    private final CommentService commentService = new CommentService();
    private final ReviewService reviewService = new ReviewService();
    private final TagService tagService = new TagService();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    private Long postId;

    @FXML
    public void initialize() {
        ratingCombo.getItems().addAll(1, 2, 3, 4, 5);
        ratingCombo.setValue(5);
    }

    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }

    public void loadPost(Long postId) {
        this.postId = postId;
        Post post = postService.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        titleLabel.setText(post.getTitle());

        String authorName = userService.findById(post.getAuthorId())
                .map(User::getUsername).orElse("Unknown");
        authorLabel.setText("By " + authorName);

        String dateStr = post.getPublishedAt() != null
                ? post.getPublishedAt().format(FORMATTER)
                : post.getCreatedAt().format(FORMATTER);
        dateLabel.setText(dateStr);

        statusLabel.setText(post.getStatus().name());
        String badgeStyle = switch (post.getStatus()) {
            case PUBLISHED -> "-fx-background-color: black; -fx-text-fill: white;";
            case DRAFT -> "-fx-background-color: white; -fx-text-fill: black; -fx-border-color: black; -fx-border-width: 1;";
            case ARCHIVED -> "-fx-background-color: #ccc; -fx-text-fill: black;";
        };
        statusLabel.setStyle("-fx-font-size: 11px; " + badgeStyle
                + " -fx-padding: 2 8; -fx-background-radius: 10; -fx-border-radius: 10;");

        // Tags
        List<Tag> tags = tagService.findByPostId(postId);
        String tagStr = tags.stream().map(Tag::getName).collect(Collectors.joining(", "));
        tagsLabel.setText(tagStr.isEmpty() ? "No tags" : tagStr);

        contentLabel.setText(post.getContent());

        // Rating
        double avg = reviewService.getAverageRating(postId);
        avgRatingLabel.setText(avg > 0 ? String.format("★ %.1f / 5", avg) : "No reviews yet");

        // Hide review form if user already reviewed
        User current = SceneManager.getCurrentUser();
        if (reviewService.findByPostId(postId).stream()
                .anyMatch(r -> r.getUserId().equals(current.getId()))) {
            reviewFormBox.setVisible(false);
            reviewFormBox.setManaged(false);
        }

        loadComments();
        loadReviews();
    }

    private void loadComments() {
        commentsContainer.getChildren().clear();
        List<Comment> comments = commentService.findByPostId(postId);

        if (comments.isEmpty()) {
            Label empty = new Label("No comments yet. Be the first!");
            empty.setStyle("-fx-text-fill: #888; -fx-font-style: italic;");
            commentsContainer.getChildren().add(empty);
            return;
        }

        for (Comment comment : comments) {
            commentsContainer.getChildren().add(createCommentCard(comment));
        }
    }

    private VBox createCommentCard(Comment comment) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 6; -fx-border-color: #e9ecef; -fx-border-radius: 6;");

        String username = userService.findById(comment.getUserId())
                .map(User::getUsername).orElse("Unknown");
        Label header = new Label(username + "  •  " + comment.getCreatedAt().format(FORMATTER));
        header.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;");

        Label body = new Label(comment.getContent());
        body.setWrapText(true);
        body.setStyle("-fx-font-size: 13px;");

        card.getChildren().addAll(header, body);

        // Delete button for own comments
        User current = SceneManager.getCurrentUser();
        if (current.getId().equals(comment.getUserId())) {
            Button deleteBtn = new Button("Delete");
            deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> {
                commentService.delete(comment.getId());
                loadComments();
            });
            card.getChildren().add(deleteBtn);
        }

        return card;
    }

    @FXML
    private void handleAddComment() {
        commentError.setText("");
        String content = commentInput.getText().trim();

        if (content.isEmpty()) {
            commentError.setText("Comment cannot be empty.");
            return;
        }

        try {
            Comment comment = new Comment(postId, SceneManager.getCurrentUser().getId(), content);
            commentService.create(comment);
            commentInput.clear();
            loadComments();
        } catch (Exception e) {
            commentError.setText("Error: " + e.getMessage());
        }
    }

    private void loadReviews() {
        reviewsContainer.getChildren().clear();
        List<Review> reviews = reviewService.findByPostId(postId);

        if (reviews.isEmpty()) {
            Label empty = new Label("No reviews yet.");
            empty.setStyle("-fx-text-fill: #888; -fx-font-style: italic;");
            reviewsContainer.getChildren().add(empty);
            return;
        }

        for (Review review : reviews) {
            reviewsContainer.getChildren().add(createReviewCard(review));
        }
    }

    private VBox createReviewCard(Review review) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #fffbf0; -fx-background-radius: 6; -fx-border-color: #f0e6d2; -fx-border-radius: 6;");

        String username = userService.findById(review.getUserId())
                .map(User::getUsername).orElse("Unknown");

        String stars = "★".repeat(review.getRating()) + "☆".repeat(5 - review.getRating());
        Label header = new Label(username + "  " + stars + "  •  " + review.getCreatedAt().format(FORMATTER));
        header.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;");

        card.getChildren().add(header);

        if (review.getContent() != null && !review.getContent().isEmpty()) {
            Label body = new Label(review.getContent());
            body.setWrapText(true);
            body.setStyle("-fx-font-size: 13px;");
            card.getChildren().add(body);
        }

        return card;
    }

    @FXML
    private void handleAddReview() {
        reviewError.setText("");
        int rating = ratingCombo.getValue();
        String content = reviewInput.getText().trim();

        try {
            Review review = new Review(postId, SceneManager.getCurrentUser().getId(), rating, content);
            reviewService.create(review);
            reviewInput.clear();
            reviewFormBox.setVisible(false);
            reviewFormBox.setManaged(false);
            loadReviews();
            // Update average rating
            double avg = reviewService.getAverageRating(postId);
            avgRatingLabel.setText(String.format("★ %.1f / 5", avg));
        } catch (Exception e) {
            reviewError.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        dashboardController.showAllPosts();
    }
}
