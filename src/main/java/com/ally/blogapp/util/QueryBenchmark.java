package com.ally.blogapp.util;

import com.ally.blogapp.model.BenchmarkResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QueryBenchmark {

    private static final int WARMUP     = 5;
    private static final int ITERATIONS = 50;

    private static final String LIKE_SQL =
            "SELECT * FROM posts WHERE LOWER(title) LIKE ? OR LOWER(content) LIKE ? ORDER BY created_at DESC";

    private static final String FTS_SQL =
            "SELECT * FROM posts WHERE search_vector @@ websearch_to_tsquery('english', ?) ORDER BY created_at DESC";

    public static BenchmarkResult[] run(String keyword) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        BenchmarkResult likeResult = measure(conn, "LIKE (before)", LIKE_SQL, true, keyword);
        BenchmarkResult ftsResult  = measure(conn, "FTS  (after)",  FTS_SQL,  false, keyword);

        return new BenchmarkResult[]{ likeResult, ftsResult };
    }

    private static BenchmarkResult measure(Connection conn, String name, String sql,
                                           boolean twoParams, String keyword) throws SQLException {
        String likePattern = "%" + keyword.toLowerCase() + "%";

        // Warmup — results discarded
        for (int i = 0; i < WARMUP; i++) {
            executeQuery(conn, sql, twoParams, likePattern, keyword);
        }

        double min = Double.MAX_VALUE;
        double max = 0;
        double total = 0;

        for (int i = 0; i < ITERATIONS; i++) {
            long start = System.nanoTime();
            executeQuery(conn, sql, twoParams, likePattern, keyword);
            double ms = (System.nanoTime() - start) / 1_000_000.0;

            total += ms;
            if (ms < min) min = ms;
            if (ms > max) max = ms;
        }

        double avg = total / ITERATIONS;
        String explain = explainAnalyze(conn, sql, twoParams, likePattern, keyword);

        return new BenchmarkResult(name, avg, min, max, ITERATIONS, explain);
    }

    private static void executeQuery(Connection conn, String sql, boolean twoParams,
                                     String likePattern, String keyword) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (twoParams) {
                stmt.setString(1, likePattern);
                stmt.setString(2, likePattern);
            } else {
                stmt.setString(1, keyword);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) { /* consume */ }
            }
        }
    }

    private static String explainAnalyze(Connection conn, String sql, boolean twoParams,
                                         String likePattern, String keyword) throws SQLException {
        String explainSql = "EXPLAIN ANALYZE " + sql;
        StringBuilder sb = new StringBuilder();
        try (PreparedStatement stmt = conn.prepareStatement(explainSql)) {
            if (twoParams) {
                stmt.setString(1, likePattern);
                stmt.setString(2, likePattern);
            } else {
                stmt.setString(1, keyword);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sb.append(rs.getString(1)).append("\n");
                }
            }
        }
        return sb.toString().trim();
    }
}