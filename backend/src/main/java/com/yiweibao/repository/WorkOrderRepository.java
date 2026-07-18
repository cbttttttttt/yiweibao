package com.yiweibao.repository;

import com.yiweibao.entity.WorkOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    Page<WorkOrder> findByStatusIn(List<Integer> statuses, Pageable pageable);

    long countByStatusIn(List<Integer> statuses);

    @Query("SELECT COUNT(w) FROM WorkOrder w WHERE w.createdAt >= :start")
    long countSince(@Param("start") LocalDateTime start);

    @Query("SELECT w.faultCategory, COUNT(w) FROM WorkOrder w GROUP BY w.faultCategory")
    List<Object[]> countByFaultCategory();

    @Query(value = "SELECT w.fault_category, COUNT(*), " +
           "AVG(TIMESTAMPDIFF(HOUR, w.created_at, w.completed_at)) " +
           "FROM work_orders w WHERE w.status = 2 AND w.completed_at IS NOT NULL " +
           "GROUP BY w.fault_category", nativeQuery = true)
    List<Object[]> avgTimeByFaultCategory();

    @Query(value = "SELECT AVG(TIMESTAMPDIFF(HOUR, created_at, completed_at)) " +
           "FROM work_orders WHERE status = 2 AND completed_at IS NOT NULL", nativeQuery = true)
    Double avgRepairHours();

    @Query("SELECT w.equipment.id, w.equipment.name, w.equipment.workshop, COUNT(w) as cnt " +
           "FROM WorkOrder w GROUP BY w.equipment.id, w.equipment.name, w.equipment.workshop " +
           "ORDER BY cnt DESC")
    List<Object[]> countByEquipment(Pageable pageable);

    List<WorkOrder> findByEquipmentIdOrderByCreatedAtDesc(Long equipmentId);

    Page<WorkOrder> findByEquipmentId(Long equipmentId, Pageable pageable);

    Page<WorkOrder> findByEquipmentIdAndStatusIn(Long equipmentId, List<Integer> statuses, Pageable pageable);
}
