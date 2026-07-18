package com.yiweibao.repository;

import com.yiweibao.entity.MachineData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MachineDataRepository extends JpaRepository<MachineData, Long> {

    @Query("SELECT md FROM MachineData md WHERE md.equipment.id = :equipmentId " +
           "AND md.timestamp >= :start AND md.timestamp <= :end ORDER BY md.timestamp ASC")
    List<MachineData> findByEquipmentAndTimeRange(
        @Param("equipmentId") Long equipmentId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query(value = "SELECT md.* FROM machine_data md " +
           "INNER JOIN (SELECT equipment_id, MAX(timestamp) AS max_ts FROM machine_data GROUP BY equipment_id) latest " +
           "ON md.equipment_id = latest.equipment_id AND md.timestamp = latest.max_ts",
           nativeQuery = true)
    List<MachineData> findLatestForAllEquipment();

    @Query("SELECT md FROM MachineData md WHERE md.equipment.id = :equipmentId " +
           "AND md.timestamp = (SELECT MAX(m.timestamp) FROM MachineData m WHERE m.equipment.id = :equipmentId)")
    MachineData findLatestByEquipment(@Param("equipmentId") Long equipmentId);

    @Query("SELECT md FROM MachineData md WHERE md.timestamp >= :since ORDER BY md.equipment.id, md.timestamp ASC")
    List<MachineData> findAllSince(@Param("since") LocalDateTime since);

    void deleteByTimestampBefore(LocalDateTime threshold);
}
