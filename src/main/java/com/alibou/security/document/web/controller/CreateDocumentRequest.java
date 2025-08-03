package com.alibou.security.document.web.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDocumentRequest {
    @NotBlank
    private String title;
    
    private String description;
    
    @NotNull
    private Integer documentFieldId;
    
    @NotNull
    private Integer productionYearId;
    
    @NotNull
    private Integer manufacturerId;
    
    @NotNull
    private Integer productSeriesId;
    
    @NotNull
    private Integer productId;
    
    private String tags;
    private String category;
}
