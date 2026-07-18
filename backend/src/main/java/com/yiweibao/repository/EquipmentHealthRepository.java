package com.yiweibao.repository;

import com.yiweibao.entity.EquipmentHealth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EquipmentHealthRepository extends JpaRepository<EquipmentHealth, Long> {
    Optional<EquipmentHealth> findByEquipmentId(Long equipmentId);
}
