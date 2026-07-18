package com.yiweibao.service;

import com.yiweibao.dto.WorkOrderCompleteRequest;
import com.yiweibao.service.DiagnosisService;
import com.yiweibao.dto.WorkOrderCreateRequest;
import com.yiweibao.entity.Equipment;
import com.yiweibao.entity.WorkOrder;
import com.yiweibao.repository.EquipmentRepository;
import com.yiweibao.repository.WorkOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final EquipmentRepository equipmentRepository;
    private final DiagnosisService diagnosisService;
    private final MachineDataGenerator machineDataGenerator;

    public WorkOrderService(WorkOrderRepository workOrderRepository,
                            EquipmentRepository equipmentRepository,
                            DiagnosisService diagnosisService,
                            MachineDataGenerator machineDataGenerator) {
        this.workOrderRepository = workOrderRepository;
        this.equipmentRepository = equipmentRepository;
        this.diagnosisService = diagnosisService;
        this.machineDataGenerator = machineDataGenerator;
    }

    public Page<WorkOrder> list(List<Integer> statuses, Long equipmentId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (equipmentId != null) {
            if (statuses != null && !statuses.isEmpty()) {
                return workOrderRepository.findByEquipmentIdAndStatusIn(equipmentId, statuses, pageable);
            }
            return workOrderRepository.findByEquipmentId(equipmentId, pageable);
        }
        if (statuses == null || statuses.isEmpty()) {
            statuses = List.of(0, 1, 2, 3);
        }
        return workOrderRepository.findByStatusIn(statuses, pageable);
    }

    public WorkOrder getById(Long id) {
        return workOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("工单不存在"));
    }

    @Transactional
    public WorkOrder create(WorkOrderCreateRequest request) {
        Equipment equipment = equipmentRepository.findById(request.getEquipmentId())
                .orElseThrow(() -> new RuntimeException("设备不存在"));

        if (equipment.getStatus() == 3) {
            throw new RuntimeException("该设备已报废，无法创建工单");
        }

        WorkOrder wo = new WorkOrder();
        wo.setOrderNo(generateOrderNo());
        wo.setEquipment(equipment);
        wo.setReporter(request.getReporter());
        wo.setFaultDesc(request.getFaultDesc());
        wo.setFaultCategory(request.getFaultCategory());
        wo.setUrgency(request.getUrgency() != null ? request.getUrgency() : 0);
        wo.setPhotos(request.getPhotos());
        wo.setStatus(0); // 待处理
        wo.setCreatedAt(LocalDateTime.now());

        // 更新设备状态为"待维修"
        if (equipment.getStatus() == 0) {
            equipment.setStatus(1);
            equipmentRepository.save(equipment);
        }

        return workOrderRepository.save(wo);
    }

    @Transactional
    public WorkOrder accept(Long id, String engineerName) {
        WorkOrder wo = getById(id);
        if (wo.getStatus() != 0) {
            throw new RuntimeException("该工单已被人接单");
        }
        wo.setRepairEngineer(engineerName);
        wo.setStatus(1); // 处理中
        wo.setUpdatedAt(LocalDateTime.now());

        // 更新设备状态为"维修中"
        Equipment equipment = wo.getEquipment();
        equipment.setStatus(2);
        equipmentRepository.save(equipment);

        return workOrderRepository.save(wo);
    }

    @Transactional
    public WorkOrder cancel(Long id) {
        WorkOrder wo = getById(id);
        if (wo.getStatus() != 0) {
            throw new RuntimeException("只能撤销待处理的工单");
        }
        wo.setStatus(3);
        wo.setUpdatedAt(LocalDateTime.now());

        // 检查该设备是否还有其他待处理/处理中的工单，如果没有则恢复设备状态
        Equipment equipment = wo.getEquipment();
        if (equipment.getStatus() == 1) {
            boolean hasOtherActive = workOrderRepository
                    .findByEquipmentIdOrderByCreatedAtDesc(equipment.getId())
                    .stream()
                    .anyMatch(o -> !o.getId().equals(id) && (o.getStatus() == 0 || o.getStatus() == 1));
            if (!hasOtherActive) {
                equipment.setStatus(0);
                equipmentRepository.save(equipment);
            }
        }

        return workOrderRepository.save(wo);
    }

    @Transactional
    public WorkOrder complete(Long id, WorkOrderCompleteRequest request) {
        WorkOrder wo = getById(id);
        if (wo.getStatus() != 1) {
            throw new RuntimeException("工单状态不正确");
        }
        wo.setDiagnosis(request.getDiagnosis());
        wo.setRepairAction(request.getRepairAction());
        wo.setReplacedParts(request.getReplacedParts());
        wo.setPhotos(request.getPhotos());
        wo.setStatus(2); // 已完成
        wo.setCompletedAt(LocalDateTime.now());
        wo.setUpdatedAt(LocalDateTime.now());

        // 恢复设备状态为"正常"
        Equipment equipment = wo.getEquipment();
        equipment.setStatus(0);
        equipmentRepository.save(equipment);

        WorkOrder saved = workOrderRepository.save(wo);

        // Learn from this repair: create a diagnosis case for knowledge base improvement
        try {
            diagnosisService.createCaseFromWorkOrder(saved);
        } catch (Exception e) {
            // Don't fail the completion if case creation fails
        }

        // Reset degradation state for predictive maintenance simulation
        if (saved.getFaultCategory() != null) {
            machineDataGenerator.resetWear(equipment.getId(), saved.getFaultCategory());
        }

        return saved;
    }

    private String generateOrderNo() {
        return "WO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
