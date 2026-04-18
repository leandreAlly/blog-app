package com.ally.blogapp.controller;

import com.ally.blogapp.model.BenchmarkResult;
import com.ally.blogapp.util.QueryBenchmark;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BenchmarkController {

    @FXML private TextField keywordField;
    @FXML private Button runBtn;
    @FXML private Button exportBtn;

    @FXML private Label likeAvgLabel;
    @FXML private Label likeMinLabel;
    @FXML private Label likeMaxLabel;
    @FXML private Label ftsAvgLabel;
    @FXML private Label ftsMinLabel;
    @FXML private Label ftsMaxLabel;
    @FXML private Label gainLabel;

    @FXML private TextArea explainArea;
    @FXML private Label statusLabel;

    private BenchmarkResult[] lastResults;
    private DashboardController dashboardController;

    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }

    @FXML
    public void initialize() {
        exportBtn.setDisable(true);
    }

    @FXML
    private void handleRunBenchmark() {
        String keyword = keywordField.getText().trim();
        if (keyword.isEmpty()) {
            statusLabel.setText("Please enter a keyword.");
            return;
        }

        statusLabel.setText("Running benchmark...");
        runBtn.setDisable(true);

        // Run on background thread to keep UI responsive
        Thread thread = new Thread(() -> {
            try {
                BenchmarkResult[] results = QueryBenchmark.run(keyword);
                javafx.application.Platform.runLater(() -> {
                    lastResults = results;
                    populateResults(results);
                    exportBtn.setDisable(false);
                    runBtn.setDisable(false);
                    statusLabel.setText("Done. " + results[0].getIterations() + " iterations measured.");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("Error: " + e.getMessage());
                    runBtn.setDisable(false);
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void populateResults(BenchmarkResult[] results) {
        BenchmarkResult like = results[0];
        BenchmarkResult fts  = results[1];

        likeAvgLabel.setText(String.format("%.2f ms", like.getAvgMs()));
        likeMinLabel.setText(String.format("%.2f ms", like.getMinMs()));
        likeMaxLabel.setText(String.format("%.2f ms", like.getMaxMs()));

        ftsAvgLabel.setText(String.format("%.2f ms", fts.getAvgMs()));
        ftsMinLabel.setText(String.format("%.2f ms", fts.getMinMs()));
        ftsMaxLabel.setText(String.format("%.2f ms", fts.getMaxMs()));

        double gain = ((like.getAvgMs() - fts.getAvgMs()) / like.getAvgMs()) * 100;
        gainLabel.setText(String.format("FTS is %.1f%% faster than LIKE", gain));

        explainArea.setText(
            "=== LIKE QUERY ===\n" + like.getExplainOutput() +
            "\n\n=== FTS QUERY ===\n" + fts.getExplainOutput()
        );
    }

    @FXML
    private void handleExportReport() {
        if (lastResults == null) return;

        String keyword = keywordField.getText().trim();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        BenchmarkResult like = lastResults[0];
        BenchmarkResult fts  = lastResults[1];
        double gain = ((like.getAvgMs() - fts.getAvgMs()) / like.getAvgMs()) * 100;

        String report = buildReport(keyword, timestamp, like, fts, gain);

        Path outputPath = Paths.get(System.getProperty("user.home"), "Documents", "blog-perf-report.txt");
        try (FileWriter writer = new FileWriter(outputPath.toFile())) {
            writer.write(report);
            statusLabel.setText("Report saved to " + outputPath);
        } catch (IOException e) {
            statusLabel.setText("Export failed: " + e.getMessage());
        }
    }

    private String buildReport(String keyword, String timestamp,
                               BenchmarkResult like, BenchmarkResult fts, double gain) {
        return String.format("""
                ============================================
                  Blog App — Query Performance Report
                  Generated: %s
                ============================================

                METHODOLOGY
                  Keyword tested : "%s"
                  Warmup runs    : 5  (discarded)
                  Measured runs  : %d

                RESULTS
                  %-20s  Avg (ms)   Min (ms)   Max (ms)
                  ──────────────────────────────────────────────
                  %-20s  %8.2f   %8.2f   %8.2f
                  %-20s  %8.2f   %8.2f   %8.2f

                PERFORMANCE GAIN: %.1f%% faster with FTS

                ============================================
                EXPLAIN ANALYZE — %s
                ============================================
                %s

                ============================================
                EXPLAIN ANALYZE — %s
                ============================================
                %s

                CONCLUSION
                  The GIN-indexed tsvector column eliminates the full table scan required
                  by the LIKE operator's leading wildcard (%%), reducing average search
                  latency from %.2fms to %.2fms for the keyword "%s".
                """,
                timestamp,
                keyword,
                like.getIterations(),
                "Query", like.getQueryName(), fts.getQueryName(),
                like.getQueryName(), like.getAvgMs(), like.getMinMs(), like.getMaxMs(),
                fts.getQueryName(),  fts.getAvgMs(),  fts.getMinMs(),  fts.getMaxMs(),
                gain,
                like.getQueryName(), like.getExplainOutput(),
                fts.getQueryName(),  fts.getExplainOutput(),
                like.getAvgMs(), fts.getAvgMs(), keyword
        );
    }

    @FXML
    private void handleBack() {
        dashboardController.showAllPosts();
    }
}
