package com.aiworkspace.aicore.application.port.out;

import com.aiworkspace.aicore.application.dto.UsageMetricEvent;

public interface UsageMetricsPort {

    void record(UsageMetricEvent event);
}
