package com.alibou.security.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookFileStatistics {
    private Long totalFiles;
    private Long totalSize;
    private Long orphanFiles;
    private String mostCommonFormat;
}
