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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ReaderController {

    @FXML private VBox contentArea;
    @FXML private TextField searchField;
    @FXML private Label userLabel;

    private final PostService postService = new PostService();
    private final UserService userService = new UserService();
    private final CommentService commentService = new CommentService();
    private final ReviewService reviewService = new ReviewService();
    private final TagService tagService = new TagService();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @FXML
    public void initialize() {
        User user = SceneManager.getCurrentUser();
        userLabel.setText(user.getUsername());
        showPostList();
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            showPostList();
            return;
        }
        List<Post> posts = postService.search(keyword);
        displayPostList(posts);
    }

    @FXML
    private void handleHome() {
        searchField.clear();
        showPostList();
    }

    @FXML
    private void handleLogout() {
        SceneManager.setCurrentUser(null);
        SceneManager.switchScene("login-view.fxml", "Login");
    }

    private void showPostList() {
        List<Post> posts = postService.findPublished();
        displayPostList(posts);
    }

    private void displayPostList(List<Post> posts) {
        contentArea.getChildren().clear();

        if (posts.isEmpty()) {
            Label empty = new Label("No posts found.");
            empty.setStyle("-fx-font-size: 16px; -fx-text-fill: #999;");
            contentArea.getChildren().add(empty);
            return;
        }

        for (Post post : posts) {
            contentArea.getChildren().add(createPostCard(post));
        }
    }

    private VBox createPostCard(Post post) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(25, 0, 25, 0));
        card.setStyle("-fx-border-color: transparent transparent #e0e0e0 transparent; -fx-border-width: 0 0 1 0;");
        card.setCursor(javafx.scene.Cursor.HAND);

        // Author & date
        String authorName = userService.findById(post.getAuthorId())
                .map(User::getUsername).orElse("Unknown");
        Label meta = new Label(authorName + "  ·  " + post.getCreatedAt().format(FORMATTER));
        meta.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");

        // Title
        Label title = new Label(post.getTitle());
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: black;");
        title.setWrapText(true);

        // Content preview
        String preview = post.getContent().length() > 200
                ? post.getContent().substring(0, 200) + "..."
                : post.getContent();
        Label content = new Label(preview);
        content.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
        content.setWrapText(true);

        // Tags
        List<Tag> tags = tagService.findByPostId(post.getId());
        HBox tagsBox = new HBox(8);
        for (Tag tag : tags) {
            Label tagLabel = new Label(tag.getName());
            tagLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666; -fx-background-color: #f0f0f0; -fx-padding: 3 10; -fx-background-radius: 12;");
            tagsBox.getChildren().add(tagLabel);
        }

        // Rating & comments count
        double avg = reviewService.getAverageRating(post.getId());
        int commentCount = commentService.findByPostId(post.getId()).size();
        String footer = (avg > 0 ? String.format("★ %.1f", avg) : "No reviews")
                + "  ·  " + commentCount + " comment" + (commentCount != 1 ? "s" : "");
        Label footerLabel = new Label(footer);
        footerLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");

        card.getChildren().addAll(meta, title, content, tagsBox, footerLabel);

        card.setOnMouseClicked(e -> showPostDetail(post.getId()));

        return card;
    }

    private void showPostDetail(Long postId) {
        contentArea.getChildren().clear();

        Post post = postService.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        VBox article = new VBox(15);
        article.setPadding(new Insets(10, 0, 30, 0));

        // Back link
        Hyperlink backLink = new Hyperlink("← Back to all posts");
        backLink.setStyle("-fx-text-fill: black; -fx-font-size: 13px;");
        backLink.setOnAction(e -> showPostList());

        // Author & date
        String authorName = userService.findById(post.getAuthorId())
                .map(User::getUsername).orElse("Unknown");
        Label meta = new Label(authorName + "  ·  " + post.getCreatedAt().format(FORMATTER));
        meta.setStyle("-fx-font-size: 13px; -fx-text-fill: #999;");

        // Title
        Label title = new Label(post.getTitle());
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: black;");
        title.setWrapText(true);

        // Tags
        List<Tag> tags = tagService.findByPostId(postId);
        HBox tagsBox = new HBox(8);
        for (Tag tag : tags) {
            Label tagLabel = new Label(tag.getName());
            tagLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666; -fx-background-color: #f0f0f0; -fx-padding: 3 10; -fx-background-radius: 12;");
            tagsBox.getChildren().add(tagLabel);
        }

        // Rating
        double avg = reviewService.getAverageRating(postId);
        Label ratingLabel = new Label(avg > 0 ? String.format("★ %.1f / 5", avg) : "No reviews yet");
        ratingLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #999;");

        // Separator
        Separator sep1 = new Separator();

        // Content
        Label content = new Label(post.getContent());
        content.setStyle("-fx-font-size: 15px; -fx-text-fill: #333; -fx-line-spacing: 5;");
        content.setWrapText(true);

        // Separator
        Separator sep2 = new Separator();

        article.getChildren().addAll(backLink, meta, title, tagsBox, ratingLabel, sep1, content, sep2);

        // ---- Reviews Section ----
        Label reviewsHeader = new Label("Reviews");
        reviewsHeader.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: black;");
        article.getChildren().add(reviewsHeader);

        List<Review> reviews = reviewService.findByPostId(postId);
        if (reviews.isEmpty()) {
            Label noReviews = new Label("No reviews yet. Be the first!");
            noReviews.setStyle("-fx-font-size: 13px; -fx-text-fill: #999; -fx-font-style: italic;");
            article.getChildren().add(noReviews);
        } else {
            for (Review review : reviews) {
                article.getChildren().add(createReviewCard(review));
            }
        }

        // Review form (if user hasn't reviewed yet)
        User current = SceneManager.getCurrentUser();
        boolean alreadyReviewed = reviews.stream().anyMatch(r -> r.getUserId().equals(current.getId()));
        if (!alreadyReviewed) {
            article.getChildren().add(createReviewForm(postId, article, ratingLabel));
        }

        article.getChildren().add(new Separator());

        // ---- Comments Section ----
        Label commentsHeader = new Label("Comments");
        commentsHeader.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: black;");
        article.getChildren().add(commentsHeader);

        VBox commentsBox = new VBox(10);
        loadComments(postId, commentsBox);
        article.getChildren().add(commentsBox);

        // Comment form
        article.getChildren().add(createCommentForm(postId, commentsBox));

        contentArea.getChildren().add(article);
    }

    private VBox createReviewCard(Review review) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: #fafafa; -fx-border-color: #eee; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4;");

        String username = userService.findById(review.getUserId())
                .map(User::getUsername).orElse("Unknown");
        String stars = "★".repeat(review.getRating()) + "☆".repeat(5 - review.getRating());
        Label header = new Label(username + "  " + stars + "  ·  " + review.getCreatedAt().format(FORMATTER));
        header.setStyle("-fx-font-size: 12px; -fx-text-fill: #999; -fx-font-weight: bold;");
        card.getChildren().add(header);

        if (review.getContent() != null && !review.getContent().isEmpty()) {
            Label body = new Label(review.getContent());
            body.setWrapText(true);
            body.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            card.getChildren().add(body);
        }
        return card;
    }

    private VBox createReviewForm(Long postId, VBox article, Label ratingLabel) {
        VBox form = new VBox(10);
        form.setPadding(new Insets(15, 0, 0, 0));

        Label formTitle = new Label("Write a Review");
        formTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: black;");

        HBox ratingRow = new HBox(10);
        ratingRow.setAlignment(Pos.CENTER_LEFT);
        Label ratingText = new Label("Rating:");
        ratingText.setStyle("-fx-font-size: 13px;");
        ComboBox<Integer> ratingCombo = new ComboBox<>();
        ratingCombo.getItems().addAll(1, 2, 3, 4, 5);
        ratingCombo.setValue(5);
        ratingRow.getChildren().addAll(ratingText, ratingCombo);

        TextArea reviewInput = new TextArea();
        reviewInput.setPromptText("Write your review (optional)...");
        reviewInput.setPrefRowCount(2);
        reviewInput.setWrapText(true);
        reviewInput.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 3;");

        Label error = new Label();
        error.setStyle("-fx-text-fill: black; -fx-font-size: 12px;");

        Button submitBtn = new Button("Submit Review");
        submitBtn.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 3; -fx-cursor: hand;");
        submitBtn.setOnAction(e -> {
            try {
                Review review = new Review(postId, SceneManager.getCurrentUser().getId(),
                        ratingCombo.getValue(), reviewInput.getText().trim());
                reviewService.create(review);
                showPostDetail(postId);
            } catch (Exception ex) {
                error.setText(ex.getMessage());
            }
        });

        form.getChildren().addAll(formTitle, ratingRow, reviewInput, error, submitBtn);
        return form;
    }

    private void loadComments(Long postId, VBox container) {
        container.getChildren().clear();
        List<Comment> comments = commentService.findByPostId(postId);

        if (comments.isEmpty()) {
            Label empty = new Label("No comments yet. Be the first!");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #999; -fx-font-style: italic;");
            container.getChildren().add(empty);
            return;
        }

        for (Comment comment : comments) {
            VBox card = new VBox(4);
            card.setPadding(new Insets(12));
            card.setStyle("-fx-background-color: #fafafa; -fx-border-color: #eee; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4;");

            String username = userService.findById(comment.getUserId())
                    .map(User::getUsername).orElse("Unknown");
            Label header = new Label(username + "  ·  " + comment.getCreatedAt().format(FORMATTER));
            header.setStyle("-fx-font-size: 12px; -fx-text-fill: #999; -fx-font-weight: bold;");

            Label body = new Label(comment.getContent());
            body.setWrapText(true);
            body.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");

            card.getChildren().addAll(header, body);

            // Delete own comments
            if (comment.getUserId().equals(SceneManager.getCurrentUser().getId())) {
                Hyperlink deleteLink = new Hyperlink("Delete");
                deleteLink.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
                deleteLink.setOnAction(e -> {
                    commentService.delete(comment.getId());
                    loadComments(postId, container);
                });
                card.getChildren().add(deleteLink);
            }

            container.getChildren().add(card);
        }
    }

    private VBox createCommentForm(Long postId, VBox commentsBox) {
        VBox form = new VBox(10);
        form.setPadding(new Insets(15, 0, 0, 0));

        Label formTitle = new Label("Add a Comment");
        formTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: black;");

        TextArea commentInput = new TextArea();
        commentInput.setPromptText("Write your comment...");
        commentInput.setPrefRowCount(3);
        commentInput.setWrapText(true);
        commentInput.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 3;");

        Label error = new Label();
        error.setStyle("-fx-text-fill: black; -fx-font-size: 12px;");

        Button submitBtn = new Button("Post Comment");
        submitBtn.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 3; -fx-cursor: hand;");
        submitBtn.setOnAction(e -> {
            String text = commentInput.getText().trim();
            if (text.isEmpty()) {
                error.setText("Comment cannot be empty.");
                return;
            }
            try {
                Comment comment = new Comment(postId, SceneManager.getCurrentUser().getId(), text);
                commentService.create(comment);
                commentInput.clear();
                error.setText("");
                loadComments(postId, commentsBox);
            } catch (Exception ex) {
                error.setText(ex.getMessage());
            }
        });

        form.getChildren().addAll(formTitle, commentInput, error, submitBtn);
        return form;
    }
}
