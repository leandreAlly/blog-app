package com.ally.blogapp.controller;

import com.ally.blogapp.model.Post;
import com.ally.blogapp.model.PostStatus;
import com.ally.blogapp.model.Role;
import com.ally.blogapp.model.User;
import com.ally.blogapp.service.PostService;
import com.ally.blogapp.service.ReviewService;
import com.ally.blogapp.service.UserService;
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

public class PostListController {

    @FXML private TextField searchField;
    @FXML private VBox postListContainer;
    @FXML private Label headerLabel;

    private DashboardController dashboardController;
    private final PostService postService = new PostService();
    private final UserService userService = new UserService();
    private final ReviewService reviewService = new ReviewService();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }

    public void loadAllPosts() {
        headerLabel.setText("All Posts");
        List<Post> posts = postService.findPublished();
        displayPosts(posts);
    }

    public void loadMyPosts() {
        headerLabel.setText("My Posts");
        User user = SceneManager.getCurrentUser();
        List<Post> posts = postService.findByAuthorId(user.getId());
        displayPosts(posts);
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadAllPosts();
            return;
        }
        headerLabel.setText("Search: \"" + keyword + "\"");
        List<Post> posts = postService.search(keyword);
        displayPosts(posts);
    }

    private void displayPosts(List<Post> posts) {
        postListContainer.getChildren().clear();

        if (posts.isEmpty()) {
            Label empty = new Label("No posts found.");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #888;");
            postListContainer.getChildren().add(empty);
            return;
        }

        for (Post post : posts) {
            postListContainer.getChildren().add(createPostCard(post));
        }
    }

    private VBox createPostCard(Post post) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4;");

        // Title
        Label title = new Label(post.getTitle());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: black;");
        title.setWrapText(true);

        // Author & date
        String authorName = userService.findById(post.getAuthorId())
                .map(User::getUsername).orElse("Unknown");
        String dateStr = post.getCreatedAt() != null ? post.getCreatedAt().format(FORMATTER) : "";
        Label meta = new Label("By " + authorName + "  |  " + dateStr);
        meta.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        // Status badge
        Label status = new Label(post.getStatus().name());
        String badgeStyle = switch (post.getStatus()) {
            case PUBLISHED -> "-fx-background-color: black; -fx-text-fill: white;";
            case DRAFT -> "-fx-background-color: white; -fx-text-fill: black; -fx-border-color: black; -fx-border-width: 1;";
            case ARCHIVED -> "-fx-background-color: #ccc; -fx-text-fill: black;";
        };
        status.setStyle("-fx-font-size: 11px; " + badgeStyle + " -fx-padding: 2 8; -fx-background-radius: 10; -fx-border-radius: 10;");

        // Rating
        double avg = reviewService.getAverageRating(post.getId());
        Label rating = new Label(avg > 0 ? String.format("★ %.1f", avg) : "No reviews");
        rating.setStyle("-fx-font-size: 12px; -fx-text-fill: black;");

        HBox topRow = new HBox(10, status, rating);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Content preview
        String preview = post.getContent().length() > 150
                ? post.getContent().substring(0, 150) + "..."
                : post.getContent();
        Label content = new Label(preview);
        content.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
        content.setWrapText(true);

        // Action buttons
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button viewBtn = new Button("View");
        viewBtn.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-background-radius: 3; -fx-cursor: hand;");
        viewBtn.setOnAction(e -> dashboardController.showPostDetail(post.getId()));
        actions.getChildren().add(viewBtn);

        User currentUser = SceneManager.getCurrentUser();
        if (currentUser.getId().equals(post.getAuthorId()) || currentUser.getRole() == Role.BLOGGER) {
            Button editBtn = new Button("Edit");
            editBtn.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: black; -fx-border-width: 1; -fx-background-radius: 3; -fx-border-radius: 3; -fx-cursor: hand;");
            editBtn.setOnAction(e -> dashboardController.showEditPost(post.getId()));
            actions.getChildren().add(editBtn);

            if (currentUser.getId().equals(post.getAuthorId())) {
                Button deleteBtn = new Button("Delete");
                deleteBtn.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-background-radius: 3; -fx-cursor: hand;");
                deleteBtn.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this post?");
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            postService.delete(post.getId());
                            loadMyPosts();
                        }
                    });
                });
                actions.getChildren().add(deleteBtn);
            }
        }

        HBox bottomRow = new HBox();
        HBox.setHgrow(actions, Priority.ALWAYS);
        bottomRow.getChildren().addAll(meta, actions);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(topRow, title, content, bottomRow);
        return card;
    }
}
