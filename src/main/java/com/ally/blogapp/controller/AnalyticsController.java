package com.ally.blogapp.controller;

import com.ally.blogapp.model.PostStats;
import com.ally.blogapp.model.PostStatus;
import com.ally.blogapp.service.PostService;
import com.ally.blogapp.util.SceneManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AnalyticsController {

    @FXML private Label totalPostsLabel;
    @FXML private Label publishedLabel;
    @FXML private Label draftsLabel;
    @FXML private Label archivedLabel;
    @FXML private Label totalCommentsLabel;
    @FXML private Label totalReviewsLabel;
    @FXML private Label overallRatingLabel;
    @FXML private VBox postStatsContainer;

    private DashboardController dashboardController;
    private final PostService postService = new PostService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }

    @FXML
    public void initialize() {
        Long authorId = SceneManager.getCurrentUser().getId();
        List<PostStats> stats = postService.getStatsByAuthorId(authorId);
        populateSummary(stats);
        populateTable(stats);
    }

    private void populateSummary(List<PostStats> stats) {
        int published = 0, drafts = 0, archived = 0;
        int totalComments = 0, totalReviews = 0;
        double ratingSum = 0;
        int ratedPosts = 0;

        for (PostStats s : stats) {
            if (s.getStatus() == PostStatus.PUBLISHED) published++;
            else if (s.getStatus() == PostStatus.DRAFT) drafts++;
            else archived++;

            totalComments += s.getCommentCount();
            totalReviews  += s.getReviewCount();

            if (s.getAvgRating() > 0) {
                ratingSum += s.getAvgRating();
                ratedPosts++;
            }
        }

        totalPostsLabel.setText(String.valueOf(stats.size()));
        publishedLabel.setText(published + " published");
        draftsLabel.setText(drafts + " drafts");
        archivedLabel.setText(archived + " archived");
        totalCommentsLabel.setText(String.valueOf(totalComments));
        totalReviewsLabel.setText(String.valueOf(totalReviews));
        overallRatingLabel.setText(ratedPosts > 0
                ? String.format("★ %.1f / 5", ratingSum / ratedPosts)
                : "No ratings yet");
    }

    private void populateTable(List<PostStats> stats) {
        postStatsContainer.getChildren().clear();

        if (stats.isEmpty()) {
            Label empty = new Label("No posts yet. Create your first post!");
            empty.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-font-size: 13px;");
            postStatsContainer.getChildren().add(empty);
            return;
        }

        // Header row
        postStatsContainer.getChildren().add(buildRow(
                "Post Title", "Status", "Comments", "Ratings", "Avg Rating", "Published", true
        ));

        for (PostStats s : stats) {
            String stars = s.getAvgRating() > 0
                    ? String.format("%.1f ★", s.getAvgRating())
                    : "—";
            String date = s.getPublishedAt() != null
                    ? s.getPublishedAt().format(FMT)
                    : "—";

            postStatsContainer.getChildren().add(buildRow(
                    s.getTitle(),
                    s.getStatus().name(),
                    String.valueOf(s.getCommentCount()),
                    String.valueOf(s.getReviewCount()),
                    stars,
                    date,
                    false
            ));
        }
    }

    private HBox buildRow(String title, String status, String comments,
                          String reviews, String rating, String date, boolean header) {
        HBox row = new HBox();
        row.setPadding(new Insets(10, 12, 10, 12));

        if (header) {
            row.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 4;");
        } else {
            row.setStyle("-fx-border-color: transparent transparent #f0f0f0 transparent; -fx-border-width: 1;");
        }

        String labelStyle = header
                ? "-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #555;"
                : "-fx-font-size: 13px; -fx-text-fill: #333;";

        Label titleLbl = new Label(title);
        titleLbl.setStyle(labelStyle);
        titleLbl.setWrapText(false);
        HBox.setHgrow(titleLbl, Priority.ALWAYS);
        titleLbl.setMaxWidth(Double.MAX_VALUE);

        Label statusLbl  = styledCell(status,   header, 100);
        Label commentLbl = styledCell(comments,  header, 80);
        Label reviewLbl  = styledCell(reviews,   header, 70);
        Label ratingLbl  = styledCell(rating,    header, 80);
        Label dateLbl    = styledCell(date,      header, 110);

        row.getChildren().addAll(titleLbl, statusLbl, commentLbl, reviewLbl, ratingLbl, dateLbl);
        return row;
    }

    private Label styledCell(String text, boolean header, double width) {
        Label lbl = new Label(text);
        lbl.setPrefWidth(width);
        lbl.setMinWidth(width);
        lbl.setAlignment(Pos.CENTER_LEFT);
        lbl.setStyle(header
                ? "-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #555;"
                : "-fx-font-size: 13px; -fx-text-fill: #333;");
        return lbl;
    }

    @FXML
    private void handleBack() {
        dashboardController.showAllPosts();
    }
}
