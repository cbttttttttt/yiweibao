package com.yiweibao.service;

import com.yiweibao.entity.Equipment;
import com.yiweibao.entity.EquipmentHealth;
import com.yiweibao.entity.MachineData;
import com.yiweibao.repository.EquipmentHealthRepository;
import com.yiweibao.repository.EquipmentRepository;
import com.yiweibao.repository.MachineDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class MachineDataGenerator {

    private static final Logger log = LoggerFactory.getLogger(MachineDataGenerator.class);
    private final EquipmentRepository equipmentRepository;
    private final MachineDataRepository machineDataRepository;
    private final EquipmentHealthRepository healthRepository;
    private final DegradationEngine engine;

    public MachineDataGenerator(EquipmentRepository equipmentRepository,
                                 MachineDataRepository machineDataRepository,
                                 EquipmentHealthRepository healthRepository,
                                 @Value("${app.simulation.acceleration}") double acceleration) {
        this.equipmentRepository = equipmentRepository;
        this.machineDataRepository = machineDataRepository;
        this.healthRepository = healthRepository;
        this.engine = new DegradationEngine(acceleration);
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
        EquipmentHealth health = healthRepository.findByEquipmentId(eq.getId())
            .orElseGet(() -> {
                EquipmentHealth h = new EquipmentHealth(eq.getId());
                h.setBearingWear(initialWear(eq.getId(), 0));
                h.setCoolingDecay(initialWear(eq.getId(), 1));
                h.setToolWear(initialWear(eq.getId(), 2));
                return healthRepository.save(h);
            });

        int hour = timestamp.getHour();
        double loadFactor = loadFactorForHour(hour);

        health.setBearingWear(engine.accumulateWear(health.getBearingWear(), loadFactor, 10.0));
        health.setCoolingDecay(engine.accumulateWear(health.getCoolingDecay(), loadFactor, 10.0));
        health.setToolWear(engine.accumulateWear(health.getToolWear(), loadFactor, 10.0));
        healthRepository.save(health);

        double baseSpindle = eq.getRatedSpindleSpeed() != null && eq.getRatedSpindleSpeed() > 0
            ? eq.getRatedSpindleSpeed() : defaultSpindle(eq.getName());
        double baseTemp = 42.0;
        double baseVib = estimateBaseVibration(eq.getRatedPower());
        double baseCurrent = eq.getRatedCurrent() != null ? eq.getRatedCurrent() : 12.0;
        double basePower = eq.getRatedPower() != null ? eq.getRatedPower() : 5.5;
        double basePress = 6.0;
        double normalTempMax = eq.getNormalTempMax() != null ? eq.getNormalTempMax() : 70.0;
        double noiseVib = 0.80 + Math.random() * 0.40;
        double noiseOther = 0.95 + Math.random() * 0.10;

        double spindleSpeed = baseSpindle * loadFactor * noiseOther;
        double vibration = engine.computeVibration(baseVib, loadFactor,
            health.getBearingWear(), health.getToolWear(), noiseVib);
        double tempNoise = (Math.random() - 0.5) * 4.0;
        double temperature = engine.computeTemperature(baseTemp, loadFactor,
            health.getCoolingDecay(), tempNoise)
            + engine.bearingTempContribution(health.getBearingWear());
        double current = engine.computeCurrent(baseCurrent, loadFactor,
            health.getToolWear(), noiseOther);
        double power = engine.computePower(basePower, loadFactor,
            health.getToolWear(), noiseOther);
        double pressure = basePress * loadFactor * noiseOther;

        int vibStatus = engine.getVibrationStatus(vibration, basePower);
        int tempStatus = engine.getTemperatureStatus(temperature, normalTempMax);
        int status = engine.combinedStatus(vibStatus, tempStatus);

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

    private double loadFactorForHour(int hour) {
        if (hour >= 8 && hour < 13) return 0.85 + Math.random() * 0.15;
        else if (hour >= 13 && hour < 17) return 0.80 + Math.random() * 0.20;
        else if (hour >= 17 && hour < 21) return 0.40 + Math.random() * 0.30;
        else return 0.05 + Math.random() * 0.10;
    }

    private double initialWear(Long equipmentId, int dimension) {
        if (equipmentId == 13L && dimension == 0) return 0.55 + Math.random() * 0.15;
        if (equipmentId == 4L && dimension == 0) return 0.45 + Math.random() * 0.15;
        if (equipmentId == 13L && dimension == 1) return 0.35 + Math.random() * 0.10;
        return 0.02 + Math.random() * 0.08;
    }

    private double defaultSpindle(String name) {
        if (name.contains("CNC") || name.contains("数控") || name.contains("加工中心")) return 3500.0;
        if (name.contains("磨")) return 2800.0;
        return 1500.0;
    }

    private double estimateBaseVibration(Double ratedPower) {
        double kw = ratedPower != null ? ratedPower : 5.5;
        if (kw > 10.0) return 2.0;
        if (kw > 5.0) return 1.5;
        return 1.0;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanup() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        machineDataRepository.deleteByTimestampBefore(threshold);
        log.info("Cleaned up machine data older than: {}", threshold);
    }

    public void resetWear(Long equipmentId, String faultCategory) {
        healthRepository.findByEquipmentId(equipmentId).ifPresent(health -> {
            switch (faultCategory) {
                case "机械故障" -> health.setBearingWear(engine.resetWear());
                case "温控故障" -> health.setCoolingDecay(engine.resetWear());
                case "电气故障", "传动故障" -> health.setToolWear(engine.resetWear());
                case "液压故障" -> {}
                default -> {}
            }
            healthRepository.save(health);
        });
    }
}
