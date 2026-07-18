package com.yiweibao.repository;

import com.yiweibao.entity.Equipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    @Query("SELECT e FROM Equipment e WHERE " +
           "(:keyword IS NULL OR e.name LIKE %:keyword% OR e.code LIKE %:keyword% OR e.workshop LIKE %:keyword%)")
    Page<Equipment> search(String keyword, Pageable pageable);

    List<Equipment> findByStatusIn(List<Integer> statuses);
}
