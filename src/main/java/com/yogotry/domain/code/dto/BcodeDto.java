package com.yogotry.domain.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BcodeDto {
    private String bcode;
    private String name;
    private String description;
    private AcodeDto acode;  // AcodeDto 포함
}
