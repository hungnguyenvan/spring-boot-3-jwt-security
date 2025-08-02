package com.alibou.security.booktype;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookTypeRequest {

    @NotBlank(message = "Tên loại sách không được để trống")
    @Size(max = 100, message = "Tên loại sách không được vượt quá 100 ký tự")
    private String name;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;

    private Boolean active;

    @Size(max = 50, message = "Danh mục không được vượt quá 50 ký tự")
    private String category;

    @Size(max = 7, message = "Mã màu phải có định dạng #RRGGBB")
    private String colorCode;

    private Integer sortOrder;
}
