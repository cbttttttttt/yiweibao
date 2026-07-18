package com.yiweibao.service;

import com.yiweibao.dto.MachineDataVO;
import com.yiweibao.repository.MachineDataRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class MachineDataService {

    private final MachineDataRepository machineDataRepository;

    public MachineDataService(MachineDataRepository machineDataRepository) {
        this.machineDataRepository = machineDataRepository;
    }

    public List<MachineDataVO> getRealtimeAll() {
        return machineDataRepository.findLatestForAllEquipment()
            .stream()
            .map(MachineDataVO::from)
            .sorted(Comparator.comparing(MachineDataVO::getWorkshop, Comparator.nullsLast(String::compareTo))
                .thenComparing(MachineDataVO::getEquipmentName))
            .toList();
    }

    public MachineDataVO getLatestByEquipment(Long equipmentId) {
        var md = machineDataRepository.findLatestByEquipment(equipmentId);
        return md != null ? MachineDataVO.from(md) : null;
    }

    public List<MachineDataVO> getHistoryByEquipment(Long equipmentId, int minutes) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusMinutes(minutes);
        return machineDataRepository.findByEquipmentAndTimeRange(equipmentId, start, end)
            .stream()
            .map(MachineDataVO::from)
            .toList();
    }
}
