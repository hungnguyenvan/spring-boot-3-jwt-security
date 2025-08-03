package com.alibou.security.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookProcessingResult {
    private Boolean success;
    private String message;
    private String bookId;
    private String fileName;
}
