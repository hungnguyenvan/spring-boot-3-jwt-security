package com.alibou.security.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrphanCleanupResult {
    private Integer cleanedFiles;
    private Long freedSpace;
    private String message;
}
