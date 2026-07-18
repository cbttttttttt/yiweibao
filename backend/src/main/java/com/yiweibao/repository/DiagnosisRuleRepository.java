package com.yiweibao.repository;

import com.yiweibao.entity.DiagnosisRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DiagnosisRuleRepository extends JpaRepository<DiagnosisRule, Long> {
    List<DiagnosisRule> findByActiveTrueOrderByPriorityAsc();
}
