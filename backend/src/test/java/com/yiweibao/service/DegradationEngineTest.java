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
