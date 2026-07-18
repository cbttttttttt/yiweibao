package com.yiweibao.repository;

import com.yiweibao.entity.DiagnosisCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DiagnosisCaseRepository extends JpaRepository<DiagnosisCase, Long> {
    List<DiagnosisCase> findByStatus(Integer status);
    Page<DiagnosisCase> findByStatus(Integer status, Pageable pageable);
    List<DiagnosisCase> findByEquipmentIdOrderByCreatedAtDesc(Long equipmentId);
}
