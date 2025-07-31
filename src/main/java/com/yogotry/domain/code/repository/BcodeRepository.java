package com.yogotry.domain.code.repository;

import com.yogotry.domain.code.entity.Bcode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BcodeRepository extends JpaRepository<Bcode, String> {
    List<Bcode> findByAcode_AcodeOrderBySortOrder(String acode);
}
