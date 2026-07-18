package com.yiweibao.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.yiweibao.dto.EquipmentRequest;
import com.yiweibao.entity.Equipment;
import com.yiweibao.repository.EquipmentRepository;
import com.yiweibao.repository.WorkOrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final WorkOrderRepository workOrderRepository;
    private final String uploadPath;

    public EquipmentService(EquipmentRepository equipmentRepository,
                            WorkOrderRepository workOrderRepository,
                            @Value("${app.upload.path}") String uploadPath) {
        this.equipmentRepository = equipmentRepository;
        this.workOrderRepository = workOrderRepository;
        this.uploadPath = uploadPath;
    }

    public Page<Equipment> list(String keyword, int page, int size) {
        return equipmentRepository.search(
                keyword == null || keyword.isBlank() ? null : keyword,
                PageRequest.of(page, size));
    }

    public Equipment getById(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("设备不存在"));
    }

    public Equipment create(EquipmentRequest request) {
        Equipment e = new Equipment();
        applyRequest(e, request);
        e.setCreatedAt(LocalDateTime.now());
        Equipment saved = equipmentRepository.save(e);
        generateQRCode(saved);
        return saved;
    }

    public Equipment update(Long id, EquipmentRequest request) {
        Equipment e = getById(id);
        applyRequest(e, request);
        e.setUpdatedAt(LocalDateTime.now());
        return equipmentRepository.save(e);
    }

    @Transactional
    public void delete(Long id) {
        Equipment e = getById(id);
        var orders = workOrderRepository.findByEquipmentIdOrderByCreatedAtDesc(id);
        if (orders.stream().anyMatch(wo -> wo.getStatus() != 2)) {
            throw new RuntimeException("该设备存在未完成的工单，无法删除");
        }
        workOrderRepository.deleteAll(orders);
        equipmentRepository.delete(e);
    }

    @Transactional
    public Equipment scrap(Long id) {
        Equipment e = getById(id);
        if (e.getStatus() == 3) {
            throw new RuntimeException("该设备已报废");
        }
        e.setStatus(3);
        e.setUpdatedAt(LocalDateTime.now());
        return equipmentRepository.save(e);
    }

    private void applyRequest(Equipment e, EquipmentRequest req) {
        e.setCode(req.getCode());
        e.setName(req.getName());
        e.setModel(req.getModel());
        e.setSpec(req.getSpec());
        e.setManufacturer(req.getManufacturer());
        e.setLocation(req.getLocation());
        e.setWorkshop(req.getWorkshop());
        e.setManager(req.getManager());
        e.setPurchaseDate(req.getPurchaseDate());
        e.setStartDate(req.getStartDate());
        e.setStatus(req.getStatus() != null ? req.getStatus() : 0);
    }

    private void generateQRCode(Equipment equipment) {
        try {
            String qrDir = uploadPath + "/qrcodes";
            new File(qrDir).mkdirs();
            String filePath = qrDir + "/equipment_" + equipment.getId() + ".png";

            String content = "yiweibao://equipment/" + equipment.getId();

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, 300, 300);
            MatrixToImageWriter.writeToPath(matrix, "PNG", Path.of(filePath));

            equipment.setQrCodePath("/api/files/qrcodes/equipment_" + equipment.getId() + ".png");
            equipmentRepository.save(equipment);
        } catch (Exception e) {
            System.err.println("[EquipmentService] QR code generation failed for equipment " + equipment.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
