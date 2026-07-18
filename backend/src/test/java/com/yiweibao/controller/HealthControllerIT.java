package com.yiweibao.controller;

import com.yiweibao.entity.Equipment;
import com.yiweibao.entity.MachineData;
import com.yiweibao.repository.EquipmentRepository;
import com.yiweibao.repository.MachineDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for HealthController REST endpoints.
 *
 * Requires a running MySQL database (configured in application.yml).
 * If MySQL is unavailable, annotate this class with @Disabled and skip.
 *
 * Security is bypassed via addFilters = false — these tests verify
 * controller + service + repository wiring, not authentication.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@Disabled("Requires running MySQL database — start MySQL and remove this annotation to run")
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
