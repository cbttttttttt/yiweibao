package com.yiweibao.repository;

import com.yiweibao.entity.DiagnosisRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DiagnosisRuleRepository extends JpaRepository<DiagnosisRule, Long> {
    List<DiagnosisRule> findByActiveTrueOrderByPriorityAsc();

    List<DiagnosisRule> findByFaultCategoryAndActiveTrueOrderByPriorityAsc(String faultCategory);

    @Query("SELECT r FROM DiagnosisRule r WHERE r.active = true AND " +
           "(r.name LIKE %:keyword% OR r.symptomDescription LIKE %:keyword% OR " +
           "r.possibleCause LIKE %:keyword% OR r.recommendedAction LIKE %:keyword% OR " +
           "r.keywords LIKE %:keyword% OR r.faultCategory LIKE %:keyword%) " +
           "ORDER BY r.severityLevel DESC, r.priority ASC")
    List<DiagnosisRule> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT r FROM DiagnosisRule r WHERE r.active = true AND r.faultCategory = :category AND " +
           "(r.name LIKE %:keyword% OR r.symptomDescription LIKE %:keyword% OR " +
           "r.possibleCause LIKE %:keyword% OR r.recommendedAction LIKE %:keyword% OR " +
           "r.keywords LIKE %:keyword%) " +
           "ORDER BY r.severityLevel DESC, r.priority ASC")
    List<DiagnosisRule> searchByCategoryAndKeyword(@Param("category") String category,
                                                     @Param("keyword") String keyword);
}
