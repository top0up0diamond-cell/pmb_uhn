package com.uhn.pmb.dto;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidationStatusResponse {
    private boolean success;
    private boolean found;
    private String status;
    private String lastReason;
    private String lastAction;
    private LocalDateTime updatedAt;
}
