package com.alibou.security.book;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BookRequest {

    @NotBlank(message = "Tiêu đề sách không được để trống")
    @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
    private String title;

    @NotBlank(message = "Tác giả không được để trống")
    @Size(max = 255, message = "Tên tác giả không được vượt quá 255 ký tự")
    private String author;

    @Size(max = 20, message = "ISBN không được vượt quá 20 ký tự")
    private String isbn;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;
    
    @Size(max = 1000, message = "Tóm tắt không được vượt quá 1000 ký tự")
    private String synopsis;
    
    @Size(max = 100, message = "Thể loại không được vượt quá 100 ký tự")
    private String category;

    private Integer bookTypeId;

    @Size(max = 500, message = "Đường dẫn file không được vượt quá 500 ký tự")
    private String filePath;

    @Size(max = 100, message = "Tên file không được vượt quá 100 ký tự")
    private String fileName;

    @Size(max = 10, message = "Định dạng file không được vượt quá 10 ký tự")
    private String fileFormat;

    private Long fileSize;

    private Boolean isFree;

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    @Digits(integer = 8, fraction = 2, message = "Giá không hợp lệ")
    private BigDecimal price;

    private Boolean downloadable;

    private Boolean active;

    @Size(max = 255, message = "Nhà xuất bản không được vượt quá 255 ký tự")
    private String publisher;

    @Min(value = 1000, message = "Năm xuất bản phải từ 1000 trở lên")
    @Max(value = 9999, message = "Năm xuất bản không được vượt quá 9999")
    private Integer publicationYear;

    @Size(max = 50, message = "Ngôn ngữ không được vượt quá 50 ký tự")
    private String language;

    @Min(value = 1, message = "Số trang phải lớn hơn 0")
    private Integer pageCount;
}
