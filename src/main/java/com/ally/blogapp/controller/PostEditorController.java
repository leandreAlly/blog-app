package com.ally.blogapp.controller;

import com.ally.blogapp.model.Post;
import com.ally.blogapp.model.PostStatus;
import com.ally.blogapp.model.Tag;
import com.ally.blogapp.service.PostService;
import com.ally.blogapp.service.TagService;
import com.ally.blogapp.util.SceneManager;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PostEditorController {

    @FXML private Label editorTitle;
    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private TextField tagsField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label errorLabel;
    @FXML private Button saveBtn;
    @FXML private Button publishBtn;

    private DashboardController dashboardController;
    private final PostService postService = new PostService();
    private final TagService tagService = new TagService();
    private Post currentPost;
    private boolean isNew = true;

    private List<Tag> allTags = List.of();
    private final ContextMenu tagSuggestions = new ContextMenu();

    @FXML
    public void initialize() {
        statusCombo.getItems().addAll("DRAFT", "PUBLISHED", "ARCHIVED");
        statusCombo.setValue("DRAFT");

        allTags = tagService.findAll();

        tagsField.textProperty().addListener((obs, oldVal, newVal) -> showTagSuggestions(newVal));
        tagsField.focusedProperty().addListener((obs, was, isFocused) -> {
            if (!isFocused) tagSuggestions.hide();
        });
    }

    private void showTagSuggestions(String text) {
        int lastComma = text.lastIndexOf(',');
        String token = (lastComma >= 0 ? text.substring(lastComma + 1) : text).trim().toLowerCase();

        if (token.isEmpty()) {
            tagSuggestions.hide();
            return;
        }

        List<Tag> matches = allTags.stream()
                .filter(t -> t.getName().startsWith(token))
                .limit(8)
                .collect(Collectors.toList());

        if (matches.isEmpty()) {
            tagSuggestions.hide();
            return;
        }

        tagSuggestions.getItems().clear();
        for (Tag tag : matches) {
            MenuItem item = new MenuItem(tag.getName());
            item.setOnAction(e -> insertTag(tag.getName()));
            tagSuggestions.getItems().add(item);
        }

        if (!tagSuggestions.isShowing()) {
            tagSuggestions.show(tagsField, Side.BOTTOM, 0, 0);
        }
    }

    private void insertTag(String tagName) {
        String current = tagsField.getText();
        int lastComma = current.lastIndexOf(',');
        String prefix = lastComma >= 0 ? current.substring(0, lastComma + 1) + " " : "";
        tagsField.setText(prefix + tagName + ", ");
        tagsField.positionCaret(tagsField.getText().length());
        tagSuggestions.hide();
    }

    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }

    public void setNewPost() {
        isNew = true;
        editorTitle.setText("Create New Post");
        titleField.clear();
        contentArea.clear();
        tagsField.clear();
        statusCombo.setValue("DRAFT");
    }

    public void loadPost(Long postId) {
        isNew = false;
        editorTitle.setText("Edit Post");
        currentPost = postService.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        titleField.setText(currentPost.getTitle());
        contentArea.setText(currentPost.getContent());
        statusCombo.setValue(currentPost.getStatus().name());

        // Load existing tags
        List<Tag> tags = tagService.findByPostId(postId);
        String tagStr = tags.stream().map(Tag::getName).collect(Collectors.joining(", "));
        tagsField.setText(tagStr);
    }

    @FXML
    private void handleSave() {
        errorLabel.setText("");

        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        String status = statusCombo.getValue();

        if (title.isEmpty() || content.isEmpty()) {
            errorLabel.setText("Title and content are required.");
            return;
        }

        try {
            if (isNew) {
                Post post = new Post(SceneManager.getCurrentUser().getId(), title, content);
                post.setStatus(PostStatus.valueOf(status));
                if (status.equals("PUBLISHED")) {
                    post.setPublishedAt(LocalDateTime.now());
                }
                currentPost = postService.create(post);
                isNew = false;
            } else {
                currentPost.setTitle(title);
                currentPost.setContent(content);
                currentPost.setStatus(PostStatus.valueOf(status));
                if (status.equals("PUBLISHED") && currentPost.getPublishedAt() == null) {
                    currentPost.setPublishedAt(LocalDateTime.now());
                }
                currentPost = postService.update(currentPost);
            }

            // Handle tags
            saveTags(currentPost.getId());

            editorTitle.setText("Edit Post");
            errorLabel.setStyle("-fx-text-fill: #27ae60;");
            errorLabel.setText("Post saved successfully!");
        } catch (Exception e) {
            errorLabel.setStyle("-fx-text-fill: #e74c3c;");
            errorLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handlePublish() {
        statusCombo.setValue("PUBLISHED");
        handleSave();
    }

    @FXML
    private void handleBack() {
        dashboardController.showAllPosts();
    }

    private void saveTags(Long postId) {
        String tagText = tagsField.getText().trim();
        if (tagText.isEmpty()) return;

        // Remove existing tags
        List<Tag> existing = tagService.findByPostId(postId);
        for (Tag tag : existing) {
            tagService.removeTagFromPost(postId, tag.getId());
        }

        // Add new tags
        String[] tagNames = tagText.split(",");
        for (String name : tagNames) {
            String trimmed = name.trim().toLowerCase();
            if (!trimmed.isEmpty()) {
                Tag tag = tagService.findOrCreate(trimmed);
                tagService.addTagToPost(postId, tag.getId());
            }
        }
    }
}
