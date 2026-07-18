package com.yiweibao.service;

import com.yiweibao.entity.Equipment;
import com.yiweibao.entity.MachineData;
import com.yiweibao.repository.EquipmentRepository;
import com.yiweibao.repository.MachineDataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class HealthServiceTest {

    @Mock
    private MachineDataRepository machineDataRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

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
        var score = healthService.computeScore(md);
        assertThat(score.score()).isGreaterThanOrEqualTo(80.0);
        assertThat(score.status()).isEqualTo("健康");
    }

    @Test
    void degradedEquipmentScoresBelow60() {
        Equipment e = makeEquipment();
        MachineData md = makeData(e, 5.5, 80.0, 25.0, 12.0);
        var score = healthService.computeScore(md);
        assertThat(score.score()).isLessThan(60.0);
    }

    @Test
    void nullDataReturnsInsufficient() {
        var score = healthService.computeScore(null);
        assertThat(score.status()).isEqualTo("数据不足");
        assertThat(score.score()).isEqualTo(0.0);
    }

    @Test
    void scoreNeverBelowZero() {
        Equipment e = makeEquipment();
        MachineData md = makeData(e, 100.0, 200.0, 100.0, 50.0);
        var score = healthService.computeScore(md);
        assertThat(score.score()).isGreaterThanOrEqualTo(0.0);
    }

    @Test
    void scoreNeverAbove100() {
        Equipment e = makeEquipment();
        MachineData md = makeData(e, 0.1, 20.0, 1.0, 0.5);
        var score = healthService.computeScore(md);
        assertThat(score.score()).isLessThanOrEqualTo(100.0);
    }

    @Test
    void statusTransitionsCorrectly() {
        Equipment e = makeEquipment();
        var healthy = healthService.computeScore(makeData(e, 1.0, 40.0, 10.0, 5.0));
        assertThat(healthy.status()).isEqualTo("健康");

        var attention = healthService.computeScore(makeData(e, 2.5, 55.0, 14.0, 6.5));
        assertThat(attention.status()).isEqualTo("关注");

        var alarm = healthService.computeScore(makeData(e, 6.0, 75.0, 28.0, 14.0));
        assertThat(alarm.status()).isEqualTo("告警");
    }
}
