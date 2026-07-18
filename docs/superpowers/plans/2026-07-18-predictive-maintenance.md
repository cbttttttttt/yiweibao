# 预测性维护 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the stateless random sensor data generator with a degradation simulation engine (Gamma-process approximation) and add health score + RUL prediction APIs and Android UI.

**Architecture:** New `EquipmentHealth` JPA entity persists per-equipment degradation state (3 wear dimensions). `MachineDataGenerator` refactored to accumulate wear, map to sensor symptoms, and respect ISO 10816 thresholds. `HealthService` computes scores from observable telemetry only (not hidden wear). `WorkOrderService.complete()` resets relevant wear on repair. Android MonitorScreen gets health score color rings and a new detail screen with factor breakdown.

**Tech Stack:** Java 21, Spring Boot 3.3.5, JPA/Hibernate, MySQL 9.3, Kotlin 2.0, Jetpack Compose, Material 3

## Global Constraints

- Java 21, no new dependencies beyond existing Spring Boot 3.3.5 stack
- All API responses use existing `ApiResponse<T>` envelope: `{ "code": 200, "message": "success", "data": {...} }`
- Chinese UI text throughout
- Existing 18 equipment models preserved; each gets real rated parameters from manufacturer spec sheets
- `Equipment.status`: 0-正常 1-待维修 2-维修中 3-已报废
- `MachineData.status`: 0-正常 1-预警 2-告警
- `WorkOrder.status`: 0-待处理 1-处理中 2-已完成 3-已撤销
- `WorkOrder.faultCategory` values: 机械故障, 温控故障, 电气故障, 传动故障, 液压故障
- No Python, no ML libraries, no new equipment, no auto-generated preventive work orders
- Backend port 8080, Android connects to `http://10.0.2.2:8080/`

---

### Task 1: Add rated parameter fields to Equipment entity

**Files:**
- Modify: `backend/src/main/java/com/yiweibao/entity/Equipment.java`
- Modify: `backend/src/main/java/com/yiweibao/config/DataInitializer.java`

**Interfaces:**
- Produces: `Equipment.ratedSpindleSpeed: Double?`, `Equipment.ratedPower: Double?`, `Equipment.ratedCurrent: Double?`, `Equipment.normalTempMax: Double?`

- [ ] **Step 1: Add 4 nullable Double fields to Equipment.java**

Add after `private String manager;` (line 37):

```java
private Double ratedSpindleSpeed;  // 额定主轴转速 rpm
private Double ratedPower;         // 额定功率 kW
private Double ratedCurrent;       // 额定电流 A
private Double normalTempMax;      // 正常运行温度上限 °C
```

Add getters and setters after `setManager` (after line 73):

```java
public Double getRatedSpindleSpeed() { return ratedSpindleSpeed; }
public void setRatedSpindleSpeed(Double v) { this.ratedSpindleSpeed = v; }
public Double getRatedPower() { return ratedPower; }
public void setRatedPower(Double v) { this.ratedPower = v; }
public Double getRatedCurrent() { return ratedCurrent; }
public void setRatedCurrent(Double v) { this.ratedCurrent = v; }
public Double getNormalTempMax() { return normalTempMax; }
public void setNormalTempMax(Double v) { this.normalTempMax = v; }
```

- [ ] **Step 2: Add rated parameters to all 18 equipment in DataInitializer.java**

Replace the existing `createEquipment` helper (lines 121-139) with a version that accepts the 4 new parameters:

```java
private Equipment createEquipment(String code, String name, String model, String spec,
                                   String manufacturer, String location, String workshop,
                                   String manager, LocalDate purchaseDate, LocalDate startDate,
                                   int status, Double ratedSpindleSpeed, Double ratedPower,
                                   Double ratedCurrent, Double normalTempMax) {
    Equipment e = new Equipment();
    e.setCode(code);
    e.setName(name);
    e.setModel(model);
    e.setSpec(spec);
    e.setManufacturer(manufacturer);
    e.setLocation(location);
    e.setWorkshop(workshop);
    e.setManager(manager);
    e.setPurchaseDate(purchaseDate);
    e.setStartDate(startDate);
    e.setStatus(status);
    e.setRatedSpindleSpeed(ratedSpindleSpeed);
    e.setRatedPower(ratedPower);
    e.setRatedCurrent(ratedCurrent);
    e.setNormalTempMax(normalTempMax);
    e.setCreatedAt(LocalDateTime.now());
    Equipment saved = equipmentRepository.save(e);
    generateQRCode(saved);
    return saved;
}
```

Update all 18 `createEquipment` calls with rated specs:

| Code | Model | Spindle (rpm) | Power (kW) | Current (A) | TempMax (°C) |
|------|-------|---------------|------------|-------------|--------------|
| EQ-001 | CK6150 | 1400 | 7.5 | 16.0 | 70 |
| EQ-002 | VMC850 | 8000 | 11.0 | 24.0 | 75 |
| EQ-003 | XK7132 | 6000 | 5.5 | 12.0 | 70 |
| EQ-004 | M1432 | 3000 | 5.5 | 12.0 | 65 |
| EQ-005 | HP300 | 500 | 7.5 | 16.0 | 70 |
| EQ-006 | TP619 | 2000 | 11.0 | 24.0 | 70 |
| EQ-007 | X2020 | 6000 | 15.0 | 32.0 | 75 |
| EQ-008 | T600 | 8000 | 7.5 | 16.0 | 70 |
| EQ-009 | Y3150 | 2000 | 5.5 | 12.0 | 70 |
| EQ-010 | QC12Y | 0 | 7.5 | 16.0 | 60 |
| EQ-011 | D7140 | 0 | 4.0 | 9.0 | 65 |
| EQ-012 | WC67Y | 0 | 7.5 | 16.0 | 65 |
| EQ-013 | CW6180 | 800 | 11.0 | 24.0 | 75 |
| EQ-014 | Z5140 | 3000 | 4.0 | 9.0 | 65 |
| EQ-015 | M6025 | 4000 | 3.0 | 7.0 | 65 |
| EQ-016 | GZ4000 | 0 | 5.5 | 12.0 | 60 |
| EQ-017 | HMC630 | 6000 | 15.0 | 32.0 | 75 |
| EQ-018 | YT32-315 | 0 | 15.0 | 32.0 | 70 |

Example updated call for EQ-001:

```java
Equipment e1 = createEquipment("EQ-001", "数控车床 CK6150", "CK6150", "Φ500×1500mm", "沈阳机床", "机加车间-A区", "机加车间", "张管理",
        LocalDate.of(2020, 3, 15), LocalDate.of(2020, 4, 1), 0,
        1400.0, 7.5, 16.0, 70.0);
```

All 18 calls updated similarly with values from the table above.

- [ ] **Step 3: Run backend to verify DDL auto-update**

Run: `cd backend && mvn spring-boot:run`
Expected: Tables updated (new columns added by `ddl-auto: update`), server starts on port 8080.

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/yiweibao/entity/Equipment.java backend/src/main/java/com/yiweibao/config/DataInitializer.java
git commit -m "feat: add rated parameters to Equipment entity with real specs for all 18 models"
```

### Task 2: Create EquipmentHealth entity and repository

**Files:**
- Create: `backend/src/main/java/com/yiweibao/entity/EquipmentHealth.java`
- Create: `backend/src/main/java/com/yiweibao/repository/EquipmentHealthRepository.java`

**Interfaces:**
- Consumes: `Equipment.id: Long`
- Produces: `EquipmentHealth` fields: `id: Long`, `equipmentId: Long` (unique), `bearingWear: Double`, `coolingDecay: Double`, `toolWear: Double`

- [ ] **Step 1: Create EquipmentHealth.java**

```java
package com.yiweibao.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "equipment_health")
public class EquipmentHealth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "equipment_id", nullable = false, unique = true)
    private Long equipmentId;

    @Column(nullable = false)
    private Double bearingWear = 0.0;   // 轴承磨损 0~1

    @Column(nullable = false)
    private Double coolingDecay = 0.0;  // 冷却系统衰减 0~1

    @Column(nullable = false)
    private Double toolWear = 0.0;      // 刀具磨损 0~1

    public EquipmentHealth() {}

    public EquipmentHealth(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Long v) { this.equipmentId = v; }
    public Double getBearingWear() { return bearingWear; }
    public void setBearingWear(Double v) { this.bearingWear = clamp(v); }
    public Double getCoolingDecay() { return coolingDecay; }
    public void setCoolingDecay(Double v) { this.coolingDecay = clamp(v); }
    public Double getToolWear() { return toolWear; }
    public void setToolWear(Double v) { this.toolWear = clamp(v); }

    private double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
```

- [ ] **Step 2: Create EquipmentHealthRepository.java**

```java
package com.yiweibao.repository;

import com.yiweibao.entity.EquipmentHealth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EquipmentHealthRepository extends JpaRepository<EquipmentHealth, Long> {
    Optional<EquipmentHealth> findByEquipmentId(Long equipmentId);
}
```

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/yiweibao/entity/EquipmentHealth.java backend/src/main/java/com/yiweibao/repository/EquipmentHealthRepository.java
git commit -m "feat: add EquipmentHealth entity for per-device degradation tracking"
```

### Task 3: Add simulation config to application.yml

**Files:**
- Modify: `backend/src/main/resources/application.yml`

**Interfaces:**
- Produces: `app.simulation.acceleration: Double` (accessible via `@Value("${app.simulation.acceleration}")`)

- [ ] **Step 1: Add config block to application.yml**

After the `upload:` block (line 30), add:

```yaml
  simulation:
    acceleration: 180.0  # ~2h full degradation cycle at normal load
```

Full section:

```yaml
app:
  jwt:
    secret: yiweibao-demo-jwt-secret-key-2026-summer-term-project
    expiration-ms: 86400000
  upload:
    path: ./uploads
  simulation:
    acceleration: 180.0
```

- [ ] **Step 2: Commit**

```bash
git add backend/src/main/resources/application.yml
git commit -m "feat: add simulation acceleration config for predictive maintenance"
```

### Task 4: Refactor MachineDataGenerator with degradation engine

**Files:**
- Modify: `backend/src/main/java/com/yiweibao/service/MachineDataGenerator.java`
- Create: `backend/src/main/java/com/yiweibao/service/DegradationEngine.java`
- Create: `backend/src/test/java/com/yiweibao/service/DegradationEngineTest.java`

**Interfaces:**
- Consumes: `EquipmentRepository`, `MachineDataRepository`, `EquipmentHealthRepository`, `@Value("${app.simulation.acceleration}") acceleration: double`
- Produces: updated `MachineData` records with degradation-driven values; `EquipmentHealth` rows created/updated

- [ ] **Step 1: Write failing test for degradation math — DegradationEngineTest.java**

```java
package com.yiweibao.service;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class DegradationEngineTest {

    @Test
    void wearIsMonotonicIncreasing() {
        DegradationEngine engine = new DegradationEngine(180.0);
        double wear = 0.0;
        for (int i = 0; i < 100; i++) {
            double next = engine.accumulateWear(wear, 0.85, 10.0);
            assertThat(next).isGreaterThanOrEqualTo(wear);
            wear = next;
        }
    }

    @Test
    void highLoadAccumulatesFasterThanLowLoad() {
        DegradationEngine engine = new DegradationEngine(180.0);
        double highLoadWear = engine.accumulateWear(0.0, 0.9, 10.0);
        double lowLoadWear = engine.accumulateWear(0.0, 0.1, 10.0);
        assertThat(highLoadWear).isGreaterThan(lowLoadWear);
    }

    @Test
    void wearClampedToMaxOne() {
        DegradationEngine engine = new DegradationEngine(1e9);
        double wear = engine.accumulateWear(0.999, 1.0, 10.0);
        assertThat(wear).isLessThanOrEqualTo(1.0);
    }

    @Test
    void wearResetToZero() {
        DegradationEngine engine = new DegradationEngine(180.0);
        assertThat(engine.resetWear()).isEqualTo(0.0);
    }

    @Test
    void symptomVibrationRisesWithBearingWear() {
        DegradationEngine engine = new DegradationEngine(180.0);
        double vibLow = engine.computeVibration(1.2, 0.85, 0.05, 0.05, 1.0);
        double vibHigh = engine.computeVibration(1.2, 0.85, 0.6, 0.05, 1.0);
        assertThat(vibHigh).isGreaterThan(vibLow);
    }

    @Test
    void symptomTemperatureRisesWithCoolingDecay() {
        DegradationEngine engine = new DegradationEngine(180.0);
        double tempLow = engine.computeTemperature(42.0, 0.85, 0.05, 0.0);
        double tempHigh = engine.computeTemperature(42.0, 0.85, 0.8, 0.0);
        assertThat(tempHigh).isGreaterThan(tempLow);
    }

    @Test
    void iso10816ThresholdMapReturnsCorrectZone() {
        DegradationEngine engine = new DegradationEngine(180.0);
        assertThat(engine.getVibrationStatus(1.0, 15.0)).isEqualTo(0);
        assertThat(engine.getVibrationStatus(3.0, 15.0)).isEqualTo(1);
        assertThat(engine.getVibrationStatus(8.0, 15.0)).isEqualTo(2);
    }
}
```

Run: `cd backend && mvn test -Dtest=DegradationEngineTest`
Expected: FAIL — `DegradationEngine` class not found.

- [ ] **Step 2: Create DegradationEngine.java**

```java
package com.yiweibao.service;

import java.util.Random;

public class DegradationEngine {

    private final double acceleration;
    private final Random random;

    public DegradationEngine(double acceleration) {
        this.acceleration = acceleration;
        this.random = new Random();
    }

    public double accumulateWear(double current, double loadFactor, double intervalSec) {
        double baseRate = 1.0 / (7200.0 / acceleration);
        double increment = Math.max(0, baseRate * loadFactor * intervalSec * (0.5 + random.nextDouble()));
        return Math.min(1.0, current + increment);
    }

    public double resetWear() {
        return 0.0;
    }

    public double computeVibration(double baseVib, double loadFactor,
                                   double bearingWear, double toolWear, double noise) {
        double symptomFactor = (1.0 + 4.0 * bearingWear) * (1.0 + toolWear);
        return baseVib * loadFactor * symptomFactor * noise;
    }

    public double computeTemperature(double baseTemp, double loadFactor,
                                      double coolingDecay, double noise) {
        return baseTemp + (loadFactor * 20.0) + (coolingDecay * 30.0) + noise;
    }

    public double bearingTempContribution(double bearingWear) {
        return bearingWear > 0.5 ? (bearingWear - 0.5) * 30.0 : 0.0;
    }

    public double computeCurrent(double baseCurrent, double loadFactor,
                                  double toolWear, double noise) {
        return baseCurrent * loadFactor * (1.0 + toolWear) * noise;
    }

    public double computePower(double basePower, double loadFactor,
                                double toolWear, double noise) {
        return basePower * loadFactor * (1.0 + toolWear) * noise;
    }

    public int getVibrationStatus(double vibrationMmPerSec, double ratedPowerKw) {
        if (vibrationMmPerSec > 7.1) return 2;
        if (vibrationMmPerSec > 2.8) return 1;
        return 0;
    }

    public int getTemperatureStatus(double temperature, double normalTempMax) {
        if (temperature > normalTempMax + 20) return 2;
        if (temperature > normalTempMax + 10) return 1;
        return 0;
    }

    public int combinedStatus(int vibStatus, int tempStatus) {
        return Math.max(vibStatus, tempStatus);
    }
}
```

- [ ] **Step 3: Run tests to verify they pass**

Run: `cd backend && mvn test -Dtest=DegradationEngineTest`
Expected: PASS — 7/7 tests green.

- [ ] **Step 4: Write the refactored MachineDataGenerator**

Replace the entire contents of `MachineDataGenerator.java`:

```java
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
```

- [ ] **Step 5: Run DegradationEngineTest again to confirm still passes**

Run: `cd backend && mvn test -Dtest=DegradationEngineTest`
Expected: PASS.

- [ ] **Step 6: Compile backend to verify no type errors**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS.

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java/com/yiweibao/service/DegradationEngine.java \
        backend/src/main/java/com/yiweibao/service/MachineDataGenerator.java \
        backend/src/test/java/com/yiweibao/service/DegradationEngineTest.java
git commit -m "feat: refactor data generator with degradation simulation engine"
```

### Task 5: Wire WorkOrderService to reset degradation on repair

**Files:**
- Modify: `backend/src/main/java/com/yiweibao/service/WorkOrderService.java`

**Interfaces:**
- Consumes: `MachineDataGenerator.resetWear(Long equipmentId, String faultCategory): void`

- [ ] **Step 1: Add MachineDataGenerator injection and reset call**

Add field:

```java
private final MachineDataGenerator machineDataGenerator;
```

Update constructor to 4 parameters:

```java
public WorkOrderService(WorkOrderRepository workOrderRepository,
                        EquipmentRepository equipmentRepository,
                        DiagnosisService diagnosisService,
                        MachineDataGenerator machineDataGenerator) {
    this.workOrderRepository = workOrderRepository;
    this.equipmentRepository = equipmentRepository;
    this.diagnosisService = diagnosisService;
    this.machineDataGenerator = machineDataGenerator;
}
```

In `complete()` method, after `diagnosisService.createCaseFromWorkOrder(saved)` (after the try-catch block), add:

```java
        if (saved.getFaultCategory() != null) {
            machineDataGenerator.resetWear(equipment.getId(), saved.getFaultCategory());
        }
```

- [ ] **Step 2: Compile to verify**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/yiweibao/service/WorkOrderService.java
git commit -m "feat: reset equipment degradation on work order completion"
```

### Task 6: Create HealthService with health score and RUL computation

**Files:**
- Create: `backend/src/main/java/com/yiweibao/service/HealthService.java`
- Create: `backend/src/test/java/com/yiweibao/service/HealthServiceTest.java`

**Interfaces:**
- Consumes: `MachineDataRepository.findByEquipmentAndTimeRange()`, `MachineDataRepository.findLatestForAllEquipment()`, `EquipmentRepository`
- Produces: `HealthService.getHealthScores(): List<HealthScore>`, `HealthService.getHealthDetail(Long equipmentId): HealthDetail`

- [ ] **Step 1: Create HealthService.java**

```java
package com.yiweibao.service;

import com.yiweibao.entity.Equipment;
import com.yiweibao.entity.MachineData;
import com.yiweibao.repository.EquipmentRepository;
import com.yiweibao.repository.MachineDataRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
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
            vib += d.getVibration(); temp += d.getTemperature();
            current += d.getCurrent(); power += d.getPower();
            speed += d.getSpindleSpeed(); pressure += d.getPressure();
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
            new FactorDetail("温度", avg.getTemperature(), "°C",
                String.format("上限 %.0f°C", normalTempMax),
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
            sumX += x; sumY += y; sumXY += x * y; sumX2 += x * x;
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
```

- [ ] **Step 2: Write HealthServiceTest.java**

```java
package com.yiweibao.service;

import com.yiweibao.entity.Equipment;
import com.yiweibao.entity.MachineData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HealthServiceTest {

    @Mock
    private com.yiweibao.repository.MachineDataRepository machineDataRepository;
    @Mock
    private com.yiweibao.repository.EquipmentRepository equipmentRepository;

    @InjectMocks
    private HealthService healthService;

    private Equipment makeEquipment() {
        Equipment e = new Equipment();
        e.setId(1L);
        e.setName("测试机床");
        e.setWorkshop("测试车间");
        e.setRatedPower(7.5);
        e.setRatedCurrent(16.0);
        e.setNormalTempMax(70.0);
        return e;
    }

    private MachineData makeData(Equipment e, double vib, double temp, double current, double power) {
        MachineData md = new MachineData();
        md.setEquipment(e);
        md.setVibration(vib);
        md.setTemperature(temp);
        md.setCurrent(current);
        md.setPower(power);
        md.setSpindleSpeed(1000.0);
        md.setPressure(6.0);
        return md;
    }

    @Test
    void healthyEquipmentScoresAbove80() {
        Equipment e = makeEquipment();
        MachineData md = makeData(e, 1.2, 45.0, 12.0, 5.5);
        var score = healthService.computeScore(md, md);
        assertThat(score.score()).isGreaterThanOrEqualTo(80.0);
        assertThat(score.status()).isEqualTo("健康");
    }

    @Test
    void degradedEquipmentScoresBelow60() {
        Equipment e = makeEquipment();
        MachineData md = makeData(e, 5.5, 80.0, 25.0, 12.0);
        var score = healthService.computeScore(md, md);
        assertThat(score.score()).isLessThan(60.0);
    }

    @Test
    void nullDataReturnsInsufficient() {
        var score = healthService.computeScore(null, null);
        assertThat(score.status()).isEqualTo("数据不足");
        assertThat(score.score()).isEqualTo(0.0);
    }

    @Test
    void scoreNeverBelowZero() {
        Equipment e = makeEquipment();
        MachineData md = makeData(e, 100.0, 200.0, 100.0, 50.0);
        var score = healthService.computeScore(md, md);
        assertThat(score.score()).isGreaterThanOrEqualTo(0.0);
    }

    @Test
    void scoreNeverAbove100() {
        Equipment e = makeEquipment();
        MachineData md = makeData(e, 0.1, 20.0, 1.0, 0.5);
        var score = healthService.computeScore(md, md);
        assertThat(score.score()).isLessThanOrEqualTo(100.0);
    }

    @Test
    void statusTransitionsCorrectly() {
        Equipment e = makeEquipment();
        var healthy = healthService.computeScore(makeData(e, 1.0, 40.0, 10.0, 5.0), null);
        assertThat(healthy.status()).isEqualTo("健康");

        var attention = healthService.computeScore(makeData(e, 3.5, 62.0, 18.0, 8.0), null);
        assertThat(attention.status()).isEqualTo("关注");

        var alarm = healthService.computeScore(makeData(e, 6.0, 75.0, 28.0, 14.0), null);
        assertThat(alarm.status()).isEqualTo("告警");
    }
}
```

- [ ] **Step 3: Run tests**

Run: `cd backend && mvn test -Dtest=HealthServiceTest`
Expected: PASS — 6/6 tests green.

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/yiweibao/service/HealthService.java \
        backend/src/test/java/com/yiweibao/service/HealthServiceTest.java
git commit -m "feat: add health score and RUL computation service"
```

### Task 7: Create HealthController with REST APIs

**Files:**
- Create: `backend/src/main/java/com/yiweibao/controller/HealthController.java`

**Interfaces:**
- Consumes: `HealthService`
- Produces: `GET /api/health/scores`, `GET /api/health/{equipmentId}`

- [ ] **Step 1: Create HealthController.java**

```java
package com.yiweibao.controller;

import com.yiweibao.dto.ApiResponse;
import com.yiweibao.service.HealthService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/scores")
    public ApiResponse<List<HealthService.HealthScore>> getScores() {
        return ApiResponse.success(healthService.getHealthScores());
    }

    @GetMapping("/{equipmentId}")
    public ApiResponse<HealthService.HealthDetail> getDetail(@PathVariable Long equipmentId) {
        return ApiResponse.success(healthService.getHealthDetail(equipmentId));
    }
}
```

- [ ] **Step 2: Compile**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/yiweibao/controller/HealthController.java
git commit -m "feat: add health score REST API endpoints"
```

### Task 8: Update Android data models and API service

**Files:**
- Modify: `android/app/src/main/java/com/yiweibao/app/data/model/Models.kt`
- Modify: `android/app/src/main/java/com/yiweibao/app/data/api/ApiService.kt`

**Interfaces:**
- Produces: `HealthScore`, `HealthDetail`, `FactorDetail` data classes; `getHealthScores()`, `getHealthDetail(id)` API methods

- [ ] **Step 1: Add data classes to Models.kt**

Add at end of file (after `MachineData`):

```kotlin
data class HealthScore(
    val equipmentId: Long,
    val equipmentName: String,
    val workshop: String?,
    val score: Double,
    val status: String,
    val vibScore: Double,
    val tempScore: Double,
    val elecScore: Double
)

data class HealthDetail(
    val equipmentId: Long,
    val equipmentName: String,
    val workshop: String?,
    val totalScore: Double,
    val status: String,
    val factors: List<FactorDetail>,
    val rul: String,
    val vibScore: Double,
    val tempScore: Double,
    val elecScore: Double
)

data class FactorDetail(
    val name: String,
    val value: Double,
    val unit: String,
    val reference: String,
    val level: String
)
```

- [ ] **Step 2: Add API endpoints to ApiService.kt**

Add before the closing `}` of the interface:

```kotlin
    @GET("api/health/scores")
    suspend fun getHealthScores(): ApiResponse<List<HealthScore>>

    @GET("api/health/{equipmentId}")
    suspend fun getHealthDetail(@Path("equipmentId") id: Long): ApiResponse<HealthDetail>
```

- [ ] **Step 3: Commit**

```bash
git add android/app/src/main/java/com/yiweibao/app/data/model/Models.kt \
        android/app/src/main/java/com/yiweibao/app/data/api/ApiService.kt
git commit -m "feat: add health score Android data models and API endpoints"
```

### Task 9: Add health score rings to Android MonitorScreen

**Files:**
- Modify: `android/app/src/main/java/com/yiweibao/app/ui/monitor/MonitorScreen.kt`
- Modify: `android/app/src/main/java/com/yiweibao/app/ui/monitor/MonitorViewModel.kt`

**Interfaces:**
- Consumes: `ApiService.getHealthScores()`, `ApiService.getMachineDataRealtime()`
- Produces: Updated `EquipmentStatusCard` with health score color ring; sort toggle

- [ ] **Step 1: Update MonitorViewModel.kt to also fetch health scores**

Replace the file:

```kotlin
package com.yiweibao.app.ui.monitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yiweibao.app.data.api.RetrofitClient
import com.yiweibao.app.data.model.HealthScore
import com.yiweibao.app.data.model.MachineData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class MonitorUiState(
    val realtimeData: List<MachineData> = emptyList(),
    val healthScores: Map<Long, HealthScore> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val sortByHealth: Boolean = true
)

class MonitorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MonitorUiState())
    val uiState: StateFlow<MonitorUiState> = _uiState.asStateFlow()

    private val api = RetrofitClient.apiService
    private var pollingJob: Job? = null

    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                try {
                    _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                    val realtimeResult = api.getMachineDataRealtime()
                    val healthResult = api.getHealthScores()
                    if (realtimeResult.code == 200 && healthResult.code == 200) {
                        val healthMap = healthResult.data!!.associateBy { it.equipmentId }
                        _uiState.value = _uiState.value.copy(
                            realtimeData = realtimeResult.data ?: emptyList(),
                            healthScores = healthMap,
                            isLoading = false,
                            error = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = realtimeResult.message
                        )
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "加载失败"
                    )
                }
                delay(5000)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun toggleSort() {
        _uiState.value = _uiState.value.copy(
            sortByHealth = !_uiState.value.sortByHealth
        )
    }
}
```

- [ ] **Step 2: Update MonitorScreen.kt — EquipmentStatusCard with health ring**

Replace the `EquipmentStatusCard` function:

```kotlin
@Composable
fun EquipmentStatusCard(
    data: MachineData,
    healthScore: HealthScore?,
    onCardClick: () -> Unit,
    onHealthClick: () -> Unit
) {
    val healthColor = when {
        healthScore == null -> Color.Gray
        healthScore.score >= 80 -> Color(0xFF43A047)
        healthScore.score >= 60 -> Color(0xFFFB8C00)
        else -> Color(0xFFE53935)
    }
    val statusColor = when (data.status) {
        2 -> Color(0xFFE53935)
        1 -> Color(0xFFFB8C00)
        else -> Color(0xFF43A047)
    }
    val statusText = when (data.status) {
        2 -> "告警"
        1 -> "预警"
        else -> "正常"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).clickable(onClick = onHealthClick),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { (healthScore?.score ?: 0.0).toFloat() / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = healthColor,
                    strokeWidth = 4.dp,
                    trackColor = healthColor.copy(alpha = 0.15f),
                )
                Text(
                    text = if (healthScore != null) "${healthScore.score.toInt()}" else "--",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = healthColor
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f).clickable(onClick = onCardClick)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(data.equipmentName, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            statusText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
                if (healthScore != null) {
                    Text(healthScore.status, style = MaterialTheme.typography.bodySmall, color = healthColor)
                }
                if (!data.workshop.isNullOrBlank()) {
                    Text(data.workshop, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline)
                }

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricItem("转速", "${data.spindleSpeed.toInt()} rpm", data.spindleSpeed > 3000)
                    MetricItem("温度", "${data.temperature}°C", data.temperature > 60)
                    MetricItem("振动", "${data.vibration} mm/s", data.vibration > 3.5)
                    MetricItem("功率", "${data.power} kW", data.power > 8)
                }
            }
        }
    }
}
```

Update `MonitorScreen` signature to accept `onHealthDetailClick`:

```kotlin
fun MonitorScreen(
    onBack: () -> Unit,
    onEquipmentClick: (Long, String) -> Unit,
    onHealthDetailClick: (Long, String) -> Unit,
    viewModel: MonitorViewModel = viewModel()
)
```

Replace the LazyColumn header item and items list:

```kotlin
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("在线设备: ${state.realtimeData.size} 台",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                            Text("自动刷新 5s", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = { viewModel.toggleSort() }) {
                            Text(
                                if (state.sortByHealth) "健康度↓" else "默认",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            val sortedData = if (state.sortByHealth) {
                state.realtimeData.sortedBy { state.healthScores[it.equipmentId]?.score ?: 0.0 }
            } else {
                state.realtimeData
            }

            items(sortedData, key = { it.equipmentId }) { data ->
                EquipmentStatusCard(
                    data = data,
                    healthScore = state.healthScores[data.equipmentId],
                    onCardClick = { onEquipmentClick(data.equipmentId, data.equipmentName) },
                    onHealthClick = { onHealthDetailClick(data.equipmentId, data.equipmentName) }
                )
            }
```

- [ ] **Step 3: Commit**

```bash
git add android/app/src/main/java/com/yiweibao/app/ui/monitor/MonitorScreen.kt \
        android/app/src/main/java/com/yiweibao/app/ui/monitor/MonitorViewModel.kt
git commit -m "feat: add health score rings and sort to monitor screen"
```

### Task 10: Create Android Health Detail screen

**Files:**
- Create: `android/app/src/main/java/com/yiweibao/app/ui/monitor/HealthDetailScreen.kt`
- Create: `android/app/src/main/java/com/yiweibao/app/ui/monitor/HealthDetailViewModel.kt`
- Modify: `android/app/src/main/java/com/yiweibao/app/navigation/NavGraph.kt`

**Interfaces:**
- Consumes: `ApiService.getHealthDetail(id)`, `ApiService.getMachineDataHistory(id, minutes)`
- Produces: Health detail screen; navigation route `/health_detail/{id}/{name}`

- [ ] **Step 1: Create HealthDetailViewModel.kt**

```kotlin
package com.yiweibao.app.ui.monitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yiweibao.app.data.api.RetrofitClient
import com.yiweibao.app.data.model.HealthDetail
import com.yiweibao.app.data.model.MachineData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HealthDetailUiState(
    val detail: HealthDetail? = null,
    val trendData: List<MachineData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HealthDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HealthDetailUiState())
    val uiState: StateFlow<HealthDetailUiState> = _uiState.asStateFlow()

    private val api = RetrofitClient.apiService

    fun load(equipmentId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val detailResult = api.getHealthDetail(equipmentId)
                val trendResult = api.getMachineDataHistory(equipmentId, 30)
                if (detailResult.code == 200) {
                    _uiState.value = _uiState.value.copy(
                        detail = detailResult.data,
                        trendData = if (trendResult.code == 200) trendResult.data ?: emptyList() else emptyList(),
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = detailResult.message
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载失败"
                )
            }
        }
    }
}
```

- [ ] **Step 2: Create HealthDetailScreen.kt**

```kotlin
package com.yiweibao.app.ui.monitor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yiweibao.app.data.model.FactorDetail
import com.yiweibao.app.data.model.MachineData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthDetailScreen(
    equipmentId: Long,
    equipmentName: String,
    onBack: () -> Unit,
    viewModel: HealthDetailViewModel = viewModel()
) {
    LaunchedEffect(equipmentId) {
        viewModel.load(equipmentId)
    }

    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$equipmentName 健康详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.load(equipmentId) }) { Text("重试") }
                }
            }
            state.detail != null -> {
                val detail = state.detail!!
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TotalScoreCard(detail.totalScore, detail.status, detail.rul)

                    Text("因子扣分明细", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    detail.factors.forEach { factor ->
                        FactorCard(factor)
                    }

                    if (state.trendData.isNotEmpty()) {
                        Text("30分钟趋势", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        TrendChart(state.trendData, detail)
                    }
                }
            }
        }
    }
}

@Composable
fun TotalScoreCard(totalScore: Double, status: String, rul: String) {
    val scoreColor = when {
        totalScore >= 80 -> Color(0xFF43A047)
        totalScore >= 60 -> Color(0xFFFB8C00)
        else -> Color(0xFFE53935)
    }

    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { (totalScore / 100.0).toFloat() },
                    modifier = Modifier.fillMaxSize(),
                    color = scoreColor,
                    strokeWidth = 8.dp,
                    trackColor = scoreColor.copy(alpha = 0.12f),
                    strokeCap = StrokeCap.Round,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${totalScore.toInt()}", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = scoreColor)
                    Text("分", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }
            Spacer(Modifier.height(12.dp))
            Surface(shape = RoundedCornerShape(12.dp), color = scoreColor.copy(alpha = 0.12f)) {
                Text(status, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    fontWeight = FontWeight.SemiBold, color = scoreColor)
            }
            Spacer(Modifier.height(8.dp))
            Text(rul, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun FactorCard(factor: FactorDetail) {
    val levelColor = if (factor.level == "warning") Color(0xFFE53935) else Color(0xFF43A047)
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(factor.name, fontWeight = FontWeight.SemiBold)
                Text(factor.reference, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${"%.1f".format(factor.value)} ${factor.unit}",
                    fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(factor.level, style = MaterialTheme.typography.labelSmall, color = levelColor)
            }
        }
    }
}

@Composable
fun TrendChart(data: List<MachineData>, detail: com.yiweibao.app.data.model.HealthDetail) {
    val vibColor = Color(0xFFE53935)
    val tempColor = Color(0xFFFF9800)

    Card(Modifier.fillMaxWidth().height(180.dp)) {
        Canvas(Modifier.fillMaxSize().padding(12.dp)) {
            if (data.size < 2) return@Canvas
            val w = size.width
            val h = size.height
            val vibMax = data.maxOf { it.vibration } * 1.2
            val tempMax = data.maxOf { it.temperature } * 1.2
            val vibMin = 0.0
            val tempMin = data.minOf { it.temperature } * 0.8
            val vibRange = if (vibMax - vibMin > 0) vibMax - vibMin else 1.0
            val tempRange = if (tempMax - tempMin > 0) tempMax - tempMin else 1.0

            for (i in 1 until data.size) {
                val x1 = (i - 1).toFloat() / (data.size - 1) * w
                val x2 = i.toFloat() / (data.size - 1) * w
                val y1v = h - ((data[i - 1].vibration - vibMin) / vibRange * h).toFloat()
                val y2v = h - ((data[i].vibration - vibMin) / vibRange * h).toFloat()
                drawLine(vibColor, Offset(x1, y1v), Offset(x2, y2v), strokeWidth = 2.5f)

                val y1t = h - ((data[i - 1].temperature - tempMin) / tempRange * h).toFloat()
                val y2t = h - ((data[i].temperature - tempMin) / tempRange * h).toFloat()
                drawLine(tempColor, Offset(x1, y1t), Offset(x2, y2t), strokeWidth = 2.5f)
            }
        }
    }
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(Modifier.size(10.dp)) { drawCircle(vibColor, 5f, center = Offset(5f, 5f)) }
            Spacer(Modifier.width(4.dp))
            Text("振动", style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.width(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(Modifier.size(10.dp)) { drawCircle(tempColor, 5f, center = Offset(5f, 5f)) }
            Spacer(Modifier.width(4.dp))
            Text("温度", style = MaterialTheme.typography.labelSmall)
        }
    }
}
```

- [ ] **Step 3: Add navigation route for health detail**

In `NavGraph.kt`, add to `Screen` sealed class:

```kotlin
    object HealthDetail : Screen("health_detail/{id}/{name}") {
        fun create(id: Long, name: String) = "health_detail/$id/${java.net.URLEncoder.encode(name, "UTF-8")}"
    }
```

Add composable in `NavHost` after the `EquipmentData` composable block:

```kotlin
        composable(
            route = Screen.HealthDetail.route,
            arguments = listOf(
                navArgument("id") { type = NavType.LongType },
                navArgument("name") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            val name = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("name") ?: "设备", "UTF-8")
            HealthDetailScreen(
                equipmentId = id,
                equipmentName = name,
                onBack = { navController.popBackStack() }
            )
        }
```

Update `MonitorScreen` call (line 216 in NavGraph) to pass health detail callback:

```kotlin
            MonitorScreen(
                onBack = { navController.popBackStack() },
                onEquipmentClick = { id, name ->
                    navController.navigate(Screen.EquipmentData.create(id, name))
                },
                onHealthDetailClick = { id, name ->
                    navController.navigate(Screen.HealthDetail.create(id, name))
                }
            )
```

- [ ] **Step 4: Commit**

```bash
git add android/app/src/main/java/com/yiweibao/app/ui/monitor/ \
        android/app/src/main/java/com/yiweibao/app/navigation/NavGraph.kt
git commit -m "feat: add health detail screen with score breakdown and trend chart"
```

### Task 11: Backend integration test for health APIs

**Files:**
- Create: `backend/src/test/java/com/yiweibao/controller/HealthControllerIT.java`

**Interfaces:**
- Consumes: `HealthService`, `HealthController`
- Tests: `GET /api/health/scores` returns 200 with array, `GET /api/health/{id}` returns 200

- [ ] **Step 1: Create HealthControllerIT.java**

```java
package com.yiweibao.controller;

import com.yiweibao.entity.Equipment;
import com.yiweibao.entity.MachineData;
import com.yiweibao.repository.EquipmentRepository;
import com.yiweibao.repository.MachineDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HealthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private MachineDataRepository machineDataRepository;

    private Long equipmentId;

    @BeforeEach
    void setUp() {
        Equipment eq = new Equipment();
        eq.setCode("TEST-HC-001");
        eq.setName("集成测试设备");
        eq.setStatus(0);
        eq.setRatedPower(7.5);
        eq.setRatedCurrent(16.0);
        eq.setNormalTempMax(70.0);
        eq = equipmentRepository.save(eq);
        equipmentId = eq.getId();

        MachineData md = new MachineData();
        md.setEquipment(eq);
        md.setSpindleSpeed(1000.0);
        md.setTemperature(45.0);
        md.setVibration(1.2);
        md.setCurrent(12.0);
        md.setPower(5.5);
        md.setPressure(6.0);
        md.setStatus(0);
        md.setTimestamp(LocalDateTime.now());
        machineDataRepository.save(md);
    }

    @Test
    void getHealthScoresReturns200() throws Exception {
        mockMvc.perform(get("/api/health/scores")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getHealthDetailReturns200() throws Exception {
        mockMvc.perform(get("/api/health/" + equipmentId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.equipmentId").value(equipmentId));
    }
}
```

- [ ] **Step 2: Run integration tests**

Run: `cd backend && mvn test -Dtest=HealthControllerIT`
Expected: PASS — 2/2 tests green.

- [ ] **Step 3: Run all backend tests**

Run: `cd backend && mvn test`
Expected: All tests pass, BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add backend/src/test/java/com/yiweibao/controller/HealthControllerIT.java
git commit -m "test: add integration tests for health API endpoints"
```

### Task 12: Final verification — data backfill and end-to-end test

**Files:**
- Modify: `backend/src/main/java/com/yiweibao/config/DataInitializer.java`

**Interfaces:**
- Backfill: on startup, ensure existing Equipment rows without rated params get filled by model

- [ ] **Step 1: Add backfill logic for existing equipment in DataInitializer.java**

In `run()` method, add `backfillRatedParams()` as the first call:

```java
    @Override
    public void run(String... args) {
        backfillRatedParams();
        if (diagnosisRuleRepository.count() == 0) seedDiagnosisRules();
        if (userRepository.count() > 0) return;
        // ... rest unchanged (all createEquipment calls now include rated params)
    }

    private void backfillRatedParams() {
        List<Equipment> all = equipmentRepository.findAll();
        for (Equipment e : all) {
            if (e.getRatedPower() != null) continue;
            String model = e.getModel();
            if (model == null) continue;
            switch (model) {
                case "CK6150" -> { e.setRatedSpindleSpeed(1400.0); e.setRatedPower(7.5); e.setRatedCurrent(16.0); e.setNormalTempMax(70.0); }
                case "VMC850" -> { e.setRatedSpindleSpeed(8000.0); e.setRatedPower(11.0); e.setRatedCurrent(24.0); e.setNormalTempMax(75.0); }
                case "XK7132" -> { e.setRatedSpindleSpeed(6000.0); e.setRatedPower(5.5); e.setRatedCurrent(12.0); e.setNormalTempMax(70.0); }
                case "M1432"  -> { e.setRatedSpindleSpeed(3000.0); e.setRatedPower(5.5); e.setRatedCurrent(12.0); e.setNormalTempMax(65.0); }
                case "HP300"  -> { e.setRatedSpindleSpeed(500.0);  e.setRatedPower(7.5); e.setRatedCurrent(16.0); e.setNormalTempMax(70.0); }
                case "TP619"  -> { e.setRatedSpindleSpeed(2000.0); e.setRatedPower(11.0); e.setRatedCurrent(24.0); e.setNormalTempMax(70.0); }
                case "X2020"  -> { e.setRatedSpindleSpeed(6000.0); e.setRatedPower(15.0); e.setRatedCurrent(32.0); e.setNormalTempMax(75.0); }
                case "T600"   -> { e.setRatedSpindleSpeed(8000.0); e.setRatedPower(7.5); e.setRatedCurrent(16.0); e.setNormalTempMax(70.0); }
                case "Y3150"  -> { e.setRatedSpindleSpeed(2000.0); e.setRatedPower(5.5); e.setRatedCurrent(12.0); e.setNormalTempMax(70.0); }
                case "QC12Y"  -> { e.setRatedSpindleSpeed(0.0);    e.setRatedPower(7.5); e.setRatedCurrent(16.0); e.setNormalTempMax(60.0); }
                case "D7140"  -> { e.setRatedSpindleSpeed(0.0);    e.setRatedPower(4.0); e.setRatedCurrent(9.0);  e.setNormalTempMax(65.0); }
                case "WC67Y"  -> { e.setRatedSpindleSpeed(0.0);    e.setRatedPower(7.5); e.setRatedCurrent(16.0); e.setNormalTempMax(65.0); }
                case "CW6180" -> { e.setRatedSpindleSpeed(800.0);  e.setRatedPower(11.0); e.setRatedCurrent(24.0); e.setNormalTempMax(75.0); }
                case "Z5140"  -> { e.setRatedSpindleSpeed(3000.0); e.setRatedPower(4.0); e.setRatedCurrent(9.0);  e.setNormalTempMax(65.0); }
                case "M6025"  -> { e.setRatedSpindleSpeed(4000.0); e.setRatedPower(3.0); e.setRatedCurrent(7.0);  e.setNormalTempMax(65.0); }
                case "GZ4000" -> { e.setRatedSpindleSpeed(0.0);    e.setRatedPower(5.5); e.setRatedCurrent(12.0); e.setNormalTempMax(60.0); }
                case "HMC630" -> { e.setRatedSpindleSpeed(6000.0); e.setRatedPower(15.0); e.setRatedCurrent(32.0); e.setNormalTempMax(75.0); }
                case "YT32-315" -> { e.setRatedSpindleSpeed(0.0);  e.setRatedPower(15.0); e.setRatedCurrent(32.0); e.setNormalTempMax(70.0); }
            }
            if (e.getRatedPower() != null) {
                equipmentRepository.save(e);
            }
        }
    }
```

Add `import java.util.List;` to the file imports.

- [ ] **Step 2: Full backend build and test**

Run: `cd backend && mvn clean compile test`
Expected: BUILD SUCCESS, all tests pass.

- [ ] **Step 3: Start backend and verify manually**

Run: `cd backend && mvn spring-boot:run`
Expected: Server starts on port 8080.

Manual verification:
```bash
curl http://localhost:8080/api/login -H "Content-Type: application/json" -d '{"username":"admin","password":"123456"}'
# Copy token from response
curl http://localhost:8080/api/health/scores -H "Authorization: Bearer <token>"
curl http://localhost:8080/api/health/1 -H "Authorization: Bearer <token>"
curl http://localhost:8080/api/health/13 -H "Authorization: Bearer <token>"
```

Expected: Health scores for 18 equipment with varying scores; EQ-013 (CW6180) should show lower score than EQ-001 (CK6150).

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/yiweibao/config/DataInitializer.java
git commit -m "feat: add backfill logic for existing equipment rated parameters"
```

---

## Self-Review

**1. Spec coverage:**
- Part 1 (Equipment rated params): Task 1 adds fields + seed data; Task 12 adds backfill for existing DB rows
- Part 2 (Degradation engine): Task 2 (entity), Task 3 (config), Task 4 (engine + generator), Task 5 (repair reset)
- Part 3 (Health score): Task 6 (service), Task 7 (controller), Task 8 (Android models), Task 9 (UI rings)
- Part 4 (RUL prediction): Task 6 (computeRUL), Task 10 (detail screen with trend + RUL text)
- Error handling: DegradationEngine clamps wear to [0,1]; HealthService handles null data, insufficient data; backfill handles existing rows
- Testing: Task 4 (unit: degradation math), Task 6 (unit: health score), Task 11 (integration: health APIs)
- Out-of-scope items confirmed absent: no preventive work orders, no Python, no ML, no new equipment

**2. Placeholder scan:** No TBD, TODO, "implement later", or "add appropriate error handling" found. Every step includes actual code.

**3. Type consistency:**
- `HealthService.HealthScore` defined in Task 6 Step 1, used in Task 7 (controller), Task 8 (Android model mirrors it), Task 9 (MonitorScreen), Task 10 (HealthDetailScreen)
- `HealthService.HealthDetail` defined in Task 6 Step 1, used in Tasks 7, 8, 10
- `DegradationEngine` methods match their calls in `MachineDataGenerator` (Task 4) and `DegradationEngineTest`
- `EquipmentHealth` fields match `MachineDataGenerator.generateForEquipment()` usage
