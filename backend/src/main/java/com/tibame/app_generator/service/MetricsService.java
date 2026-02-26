package com.tibame.app_generator.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MetricsService {

    private final AtomicInteger importTotal = new AtomicInteger(0);
    private final AtomicInteger importSuccess = new AtomicInteger(0);
    private final AtomicInteger importFailed = new AtomicInteger(0);

    private final AtomicInteger analysisTotal = new AtomicInteger(0);
    private final AtomicInteger analysisSuccess = new AtomicInteger(0);
    private final AtomicInteger analysisFailed = new AtomicInteger(0);
    private final AtomicLong analysisDurationSum = new AtomicLong(0);

    public void incrementImportTotal() {
        importTotal.incrementAndGet();
    }

    public void incrementImportSuccess() {
        importSuccess.incrementAndGet();
    }

    public void incrementImportFailed() {
        importFailed.incrementAndGet();
    }

    public void incrementAnalysisTotal() {
        analysisTotal.incrementAndGet();
    }

    public void recordAnalysisSuccess(long durationMs) {
        analysisSuccess.incrementAndGet();
        analysisDurationSum.addAndGet(durationMs);
    }

    public void incrementAnalysisFailed() {
        analysisFailed.incrementAndGet();
    }

    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("import.total", importTotal.get());
        metrics.put("import.success", importSuccess.get());
        metrics.put("import.failed", importFailed.get());

        metrics.put("analysis.total", analysisTotal.get());
        metrics.put("analysis.success", analysisSuccess.get());
        metrics.put("analysis.failed", analysisFailed.get());

        long successCount = analysisSuccess.get();
        double avgDuration = successCount > 0 ? (double) analysisDurationSum.get() / successCount : 0.0;
        metrics.put("analysis.avg_duration_ms", Math.round(avgDuration * 100.0) / 100.0);

        return metrics;
    }
}
