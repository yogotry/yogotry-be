package com.yogotry.domain.code.controller;

import com.yogotry.domain.code.dto.AcodeDto;
import com.yogotry.domain.code.dto.BcodeDto;
import com.yogotry.domain.code.entity.Bcode;
import com.yogotry.domain.code.service.CodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 코드 API 요청을 처리하는 컨트롤러 클래스
 * 코드 조회 관련 REST API 엔드포인트를 제공
 */
@RestController
@RequestMapping("/api/codes")
@RequiredArgsConstructor
public class CodeController {

    private final CodeService codeService;

    /**
     * 특정 Acode(코드 종류)의 상세 Bcode 목록 조회 API
     * GET /api/codes/{acode}
     *
     * @param acode 조회할 Acode 기본 키(예: COUNTRY)
     * @return 해당 Acode에 속하는 BcodeDto 리스트(JSON)
     */
    @GetMapping("/{acode}")
    public List<BcodeDto> getBcodesByAcode(@PathVariable String acode) {
        return codeService.findBcodesByAcode(acode);
    }

    /**
     * 전체 Acode 목록 조회 API
     * GET /api/codes/acodes
     */
    @GetMapping("/acodes")
    public List<AcodeDto> getAllAcodes() {
        return codeService.findAllAcodes();
    }
}
