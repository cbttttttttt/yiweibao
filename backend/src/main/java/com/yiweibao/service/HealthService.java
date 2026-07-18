package com.yiweibao.service;

import com.yiweibao.entity.Equipment;
import com.yiweibao.entity.MachineData;
import com.yiweibao.repository.EquipmentRepository;
import com.yiweibao.repository.MachineDataRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

@Service
public class HealthService {

    private final MachineDataRepository machineDataRepository;
    private final EquipmentRepository equipmentRepository;

    public HealthService(MachineDataRepository machineDataRepository,
                         EquipmentRepository equipmentRepository) {
        this.machineDataRepository = machineDataRepository;
        this.equipmentRepository = equipmentRepository;
    }

    public List<HealthScore> getHealthScores() {
        List<MachineData> latestList = machineDataRepository.findLatestForAllEquipment();
        return latestList.stream()
            .map(md -> computeScore(md, avgRecentData(md.getEquipment().getId(), 10)))
            .sorted(Comparator.comparingDouble(HealthScore::score))
            .collect(Collectors.toList());
    }

    public HealthDetail getHealthDetail(Long equipmentId) {
        Equipment eq = equipmentRepository.findById(equipmentId)
            .orElseThrow(() -> new RuntimeException("设备不存在"));

        List<MachineData> recent10Min = machineDataRepository.findByEquipmentAndTimeRange(
            equipmentId, LocalDateTime.now().minusMinutes(10), LocalDateTime.now());

        if (recent10Min.isEmpty()) {
            return HealthDetail.insufficient(eq);
        }

        MachineData avg = average(recent10Min);
        HealthScore score = computeScore(avg, avg);
        List<FactorDetail> factors = computeFactors(avg, eq);
        String rul = computeRUL(equipmentId);

        return new HealthDetail(
            eq.getId(), eq.getName(), eq.getWorkshop(),
            score.score(), score.status(),
            factors, rul,
            score.vibScore(), score.tempScore(), score.elecScore()
        );
    }

    private MachineData avgRecentData(Long equipmentId, int minutes) {
        List<MachineData> data = machineDataRepository.findByEquipmentAndTimeRange(
            equipmentId, LocalDateTime.now().minusMinutes(minutes), LocalDateTime.now());
        return data.isEmpty() ? null : average(data);
    }

    private MachineData average(List<MachineData> list) {
        int n = list.size();
        double vib = 0, temp = 0, current = 0, power = 0, speed = 0, pressure = 0;
        for (MachineData d : list) {
            vib += d.getVibration();
            temp += d.getTemperature();
            current += d.getCurrent();
            power += d.getPower();
            speed += d.getSpindleSpeed();
            pressure += d.getPressure();
        }
        MachineData avg = new MachineData();
        avg.setVibration(vib / n);
        avg.setTemperature(temp / n);
        avg.setCurrent(current / n);
        avg.setPower(power / n);
        avg.setSpindleSpeed(speed / n);
        avg.setPressure(pressure / n);
        avg.setEquipment(list.get(0).getEquipment());
        return avg;
    }

    HealthScore computeScore(MachineData avg, MachineData recentAvg) {
        if (avg == null) return new HealthScore(0L, "", "", 0.0, "数据不足", 0, 0, 0);

        Equipment eq = avg.getEquipment();
        double ratedPower = eq.getRatedPower() != null ? eq.getRatedPower() : 5.5;
        double normalTempMax = eq.getNormalTempMax() != null ? eq.getNormalTempMax() : 70.0;
        double ratedCurrent = eq.getRatedCurrent() != null ? eq.getRatedCurrent() : 12.0;
        double baseVib = ratedPower > 10 ? 2.0 : ratedPower > 5 ? 1.5 : 1.0;

        double vibScore = Math.max(0, 100 - (avg.getVibration() / baseVib - 1.0) * 200);
        vibScore = Math.min(100, vibScore);

        double tempScore = Math.max(0, 100 - (avg.getTemperature() / normalTempMax - 1.0) * 300);
        tempScore = Math.min(100, tempScore);

        double currentDev = Math.max(0, (avg.getCurrent() / ratedCurrent - 1.0) * 200);
        double powerDev = Math.max(0, (avg.getPower() / ratedPower - 1.0) * 200);
        double elecScore = Math.max(0, 100 - (currentDev * 0.5 + powerDev * 0.5));
        elecScore = Math.min(100, elecScore);

        double total = vibScore * 0.40 + tempScore * 0.30 + elecScore * 0.30;
        total = Math.round(total * 10.0) / 10.0;

        String status = total >= 80 ? "健康" : total >= 60 ? "关注" : "告警";

        return new HealthScore(eq.getId(), eq.getName(), eq.getWorkshop(),
            total, status,
            Math.round(vibScore * 10.0) / 10.0,
            Math.round(tempScore * 10.0) / 10.0,
            Math.round(elecScore * 10.0) / 10.0);
    }

    private List<FactorDetail> computeFactors(MachineData avg, Equipment eq) {
        double ratedPower = eq.getRatedPower() != null ? eq.getRatedPower() : 5.5;
        double normalTempMax = eq.getNormalTempMax() != null ? eq.getNormalTempMax() : 70.0;
        double ratedCurrent = eq.getRatedCurrent() != null ? eq.getRatedCurrent() : 12.0;
        double baseVib = ratedPower > 10 ? 2.0 : ratedPower > 5 ? 1.5 : 1.0;

        return List.of(
            new FactorDetail("振动", avg.getVibration(), "mm/s",
                String.format("基准 %.1f mm/s", baseVib),
                avg.getVibration() > baseVib * 2.0 ? "warning" : "normal"),
            new FactorDetail("温度", avg.getTemperature(), "℃",
                String.format("上限 %.0f℃", normalTempMax),
                avg.getTemperature() > normalTempMax ? "warning" : "normal"),
            new FactorDetail("电流", avg.getCurrent(), "A",
                String.format("额定 %.1f A", ratedCurrent),
                avg.getCurrent() > ratedCurrent * 1.3 ? "warning" : "normal"),
            new FactorDetail("功率", avg.getPower(), "kW",
                String.format("额定 %.1f kW", ratedPower),
                avg.getPower() > ratedPower * 1.2 ? "warning" : "normal")
        );
    }

    String computeRUL(Long equipmentId) {
        List<MachineData> data = machineDataRepository.findByEquipmentAndTimeRange(
            equipmentId, LocalDateTime.now().minusMinutes(30), LocalDateTime.now());

        if (data.size() < 30) return "数据积累中（需至少30分钟数据）";

        double vibSlope = slope(data, MachineData::getVibration);
        double tempSlope = slope(data, MachineData::getTemperature);
        double currentSlope = slope(data, MachineData::getCurrent);

        double dominantSlope = Math.max(vibSlope, Math.max(tempSlope, currentSlope));

        if (dominantSlope <= 0.001) return "暂无退化趋势";

        MachineData latest = data.get(data.size() - 1);
        double normalTempMax = latest.getEquipment().getNormalTempMax() != null
            ? latest.getEquipment().getNormalTempMax() : 70.0;

        double hours;
        if (vibSlope >= tempSlope && vibSlope >= currentSlope && vibSlope > 0.001) {
            double headroom = 7.1 - latest.getVibration();
            hours = headroom / vibSlope / 360.0;
        } else if (tempSlope >= currentSlope && tempSlope > 0.001) {
            double headroom = (normalTempMax + 20) - latest.getTemperature();
            hours = headroom / tempSlope / 360.0;
        } else {
            hours = 48.0;
        }

        if (hours <= 0) return "已超过告警阈值";
        if (hours < 1) return String.format("预计 %.0f 分钟后达到告警阈值", hours * 60);
        if (hours < 24) return String.format("预计 %.1f 小时后达到告警阈值", hours);
        return String.format("预计 %.1f 天后达到告警阈值", hours / 24);
    }

    private double slope(List<MachineData> data, ToDoubleFunction<MachineData> field) {
        int n = data.size();
        if (n < 2) return 0;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            double x = i;
            double y = field.applyAsDouble(data.get(i));
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }
        double denom = n * sumX2 - sumX * sumX;
        if (Math.abs(denom) < 1e-9) return 0;
        return (n * sumXY - sumX * sumY) / denom;
    }

    public record HealthScore(Long equipmentId, String equipmentName, String workshop,
                               double score, String status,
                               double vibScore, double tempScore, double elecScore) {}

    public record HealthDetail(Long equipmentId, String equipmentName, String workshop,
                                double totalScore, String status,
                                List<FactorDetail> factors, String rul,
                                double vibScore, double tempScore, double elecScore) {
        public static HealthDetail insufficient(Equipment eq) {
            return new HealthDetail(eq.getId(), eq.getName(), eq.getWorkshop(),
                0, "数据不足", List.of(), "数据积累中（需至少10分钟运行数据）", 0, 0, 0);
        }
    }

    public record FactorDetail(String name, double value, String unit,
                                String reference, String level) {}
}
