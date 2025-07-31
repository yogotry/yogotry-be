package com.yogotry.domain.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AcodeDto {
    private String acode;
    private String name;
    private String description;
}
