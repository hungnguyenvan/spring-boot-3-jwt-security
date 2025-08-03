package com.alibou.security.document.web.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDocumentMetadataRequest {
    private String title;
    private String description;
    private String tags;
    private String category;
    private Integer documentFieldId;
    private Integer productionYearId;
    private Integer manufacturerId;
    private Integer productSeriesId;
    private Integer productId;
}
