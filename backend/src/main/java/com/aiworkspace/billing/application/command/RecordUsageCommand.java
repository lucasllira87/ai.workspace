package com.aiworkspace.billing.application.command;

import java.util.UUID;

public record RecordUsageCommand(UUID userId, String module, String operation,
                                  long tokenCount, int documentCount, long storageBytes) {}
