package com.yiweibao.service;

import com.yiweibao.entity.Equipment;
import com.yiweibao.entity.MachineData;
import com.yiweibao.repository.EquipmentRepository;
import com.yiweibao.repository.MachineDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
public class MachineDataGenerator {

    private static final Logger log = LoggerFactory.getLogger(MachineDataGenerator.class);
    private final EquipmentRepository equipmentRepository;
    private final MachineDataRepository machineDataRepository;
    private final Random random = new Random();

    public MachineDataGenerator(EquipmentRepository equipmentRepository,
                                MachineDataRepository machineDataRepository) {
        this.equipmentRepository = equipmentRepository;
        this.machineDataRepository = machineDataRepository;
    }

    @Scheduled(fixedRate = 10000)
    public void generate() {
        List<Equipment> equipmentList = equipmentRepository.findByStatusIn(List.of(0, 1, 2));
        if (equipmentList.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();
        List<MachineData> batch = equipmentList.stream()
            .map(eq -> generateForEquipment(eq, now))
            .toList();

        machineDataRepository.saveAll(batch);
        log.debug("Generated {} machine data records", batch.size());
    }

    private MachineData generateForEquipment(Equipment eq, LocalDateTime timestamp) {
        String name = eq.getName();
        boolean isCncLathe = name.contains("CNC") || name.contains("数控");
        boolean isGrinder = name.contains("磨") || name.contains("加工中心");

        double baseSpindle = isCncLathe ? 3500.0 : isGrinder ? 2800.0 : 1500.0;
        double baseTemp    = 42.0;
        double baseVib     = isGrinder ? 1.8 : 1.2;
        double baseCurrent = isCncLathe ? 18.0 : isGrinder ? 22.0 : 12.0;
        double basePower   = isCncLathe ? 7.5 : isGrinder ? 11.0 : 5.5;
        double basePress   = 6.0;

        int hour = timestamp.getHour();

        double loadFactor;
        if (hour >= 8 && hour < 13) {
            loadFactor = 0.85 + random.nextDouble() * 0.15;
        } else if (hour >= 13 && hour < 17) {
            loadFactor = 0.80 + random.nextDouble() * 0.20;
        } else if (hour >= 17 && hour < 21) {
            loadFactor = 0.40 + random.nextDouble() * 0.30;
        } else {
            loadFactor = 0.05 + random.nextDouble() * 0.10;
        }

        double spindleSpeed = baseSpindle * loadFactor * (0.95 + random.nextDouble() * 0.10);
        double temperature  = baseTemp + (loadFactor * 18.0) + random.nextGaussian() * 2.0;
        double vibration    = baseVib * loadFactor * (0.80 + random.nextDouble() * 0.40);
        double current      = baseCurrent * loadFactor * (0.90 + random.nextDouble() * 0.20);
        double power        = basePower * loadFactor * (0.90 + random.nextDouble() * 0.20);
        double pressure     = basePress * loadFactor * (0.90 + random.nextDouble() * 0.20);

        if (random.nextDouble() < 0.03) {
            vibration *= 2.5 + random.nextDouble() * 1.5;
            if (random.nextDouble() < 0.5) temperature += 10 + random.nextDouble() * 15;
        }

        int status = 0;
        if (temperature > 70 || vibration > 5.0) status = 2;
        else if (temperature > 60 || vibration > 3.5) status = 1;

        MachineData md = new MachineData();
        md.setEquipment(eq);
        md.setSpindleSpeed(Math.round(spindleSpeed * 10.0) / 10.0);
        md.setTemperature(Math.round(temperature * 10.0) / 10.0);
        md.setVibration(Math.round(vibration * 100.0) / 100.0);
        md.setCurrent(Math.round(current * 10.0) / 10.0);
        md.setPower(Math.round(power * 10.0) / 10.0);
        md.setPressure(Math.round(pressure * 10.0) / 10.0);
        md.setStatus(status);
        md.setTimestamp(timestamp);
        return md;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanup() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        machineDataRepository.deleteByTimestampBefore(threshold);
        log.info("Cleaned up machine data older than: {}", threshold);
    }
}
