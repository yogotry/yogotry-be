package com.yogotry.domain.code.service;

import com.yogotry.domain.code.dto.AcodeDto;
import com.yogotry.domain.code.dto.BcodeDto;
import com.yogotry.domain.code.entity.Bcode;
import com.yogotry.domain.code.repository.AcodeRepository;
import com.yogotry.domain.code.repository.BcodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 코드 관련 비즈니스 로직을 처리하는 서비스 클래스
 * 주로 tb_bcode 테이블과 연관된 작업 수행
 */
@Service
@RequiredArgsConstructor
public class CodeService {

    private final BcodeRepository bcodeRepository;
    private final AcodeRepository acodeRepository;

    /**
     * 주어진 Acode(코드 종류) 값을 기준으로
     * 해당 Acode에 속하는 Bcode(상세 코드) 목록을 조회하여
     * BcodeDto 리스트로 반환
     *
     * @param acode tb_acode 테이블의 기본 키(예: COUNTRY, LANGUAGE 등)
     * @return 해당 Acode에 속하는 BcodeDto 리스트
     */
    public List<BcodeDto> findBcodesByAcode(String acode) {
        List<Bcode> bcodes = bcodeRepository.findByAcode_AcodeOrderBySortOrder(acode);

        return bcodes.stream()
                .map(b -> new BcodeDto(
                        b.getBcode(),
                        b.getName(),
                        b.getDescription(),
                        new AcodeDto(
                                b.getAcode().getAcode(),
                                b.getAcode().getName(),
                                b.getAcode().getDescription()
                        )
                ))
                .collect(Collectors.toList());
    }

    /**
     * 전체 Acode 목록 조회
     */
    public List<AcodeDto> findAllAcodes() {
        return acodeRepository.findAll().stream()
                .map(a -> new AcodeDto(
                        a.getAcode(),
                        a.getName(),
                        a.getDescription()
                ))
                .collect(Collectors.toList());
    }
}
