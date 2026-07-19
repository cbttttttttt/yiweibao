package com.yiweibao.repository;

import com.yiweibao.entity.DiagnosisCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DiagnosisCaseRepository extends JpaRepository<DiagnosisCase, Long> {
    List<DiagnosisCase> findByStatus(Integer status);
    Page<DiagnosisCase> findByStatus(Integer status, Pageable pageable);
    List<DiagnosisCase> findByEquipmentIdOrderByCreatedAtDesc(Long equipmentId);

    @Query("SELECT c FROM DiagnosisCase c WHERE " +
           "c.faultDesc LIKE %:keyword% OR c.diagnosis LIKE %:keyword% OR " +
           "c.repairAction LIKE %:keyword% OR c.faultCategory LIKE %:keyword% " +
           "ORDER BY c.createdAt DESC")
    List<DiagnosisCase> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT c FROM DiagnosisCase c WHERE c.faultCategory = :category AND " +
           "(c.faultDesc LIKE %:keyword% OR c.diagnosis LIKE %:keyword% OR " +
           "c.repairAction LIKE %:keyword%) " +
           "ORDER BY c.createdAt DESC")
    List<DiagnosisCase> searchByCategoryAndKeyword(@Param("category") String category,
                                                     @Param("keyword") String keyword);
}
