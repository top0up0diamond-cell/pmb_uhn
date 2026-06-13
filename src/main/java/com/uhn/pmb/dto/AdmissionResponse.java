package com.uhn.pmb.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdmissionResponse {
    private boolean success;
    private String message;
    private AdmissionFormDTO data;
}
