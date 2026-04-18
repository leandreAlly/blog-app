package com.ally.blogapp.model;

public class BenchmarkResult {
    private final String queryName;
    private final double avgMs;
    private final double minMs;
    private final double maxMs;
    private final int iterations;
    private final String explainOutput;

    public BenchmarkResult(String queryName, double avgMs, double minMs, double maxMs,
                           int iterations, String explainOutput) {
        this.queryName = queryName;
        this.avgMs = avgMs;
        this.minMs = minMs;
        this.maxMs = maxMs;
        this.iterations = iterations;
        this.explainOutput = explainOutput;
    }

    public String getQueryName()    { return queryName; }
    public double getAvgMs()        { return avgMs; }
    public double getMinMs()        { return minMs; }
    public double getMaxMs()        { return maxMs; }
    public int    getIterations()   { return iterations; }
    public String getExplainOutput(){ return explainOutput; }
}
