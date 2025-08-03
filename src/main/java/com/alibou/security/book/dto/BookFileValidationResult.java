package com.alibou.security.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookFileValidationResult {
    private Boolean isValid;
    private String fileName;
    private String message;
    private String errorCode;
}
