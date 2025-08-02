package com.alibou.security.booktype;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookTypeResponse {

    private Integer id;
    private String name;
    private String description;
    private Boolean active;
    private String category;
    private String colorCode;
    private Integer sortOrder;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private Integer createdBy;
    private Integer lastModifiedBy;

    public static BookTypeResponse fromEntity(BookType bookType) {
        return BookTypeResponse.builder()
                .id(bookType.getId())
                .name(bookType.getName())
                .description(bookType.getDescription())
                .active(bookType.getActive())
                .category(bookType.getCategory())
                .colorCode(bookType.getColorCode())
                .sortOrder(bookType.getSortOrder())
                .createdDate(bookType.getCreatedDate())
                .lastModifiedDate(bookType.getLastModifiedDate())
                .createdBy(bookType.getCreatedBy())
                .lastModifiedBy(bookType.getLastModifiedBy())
                .build();
    }
}
