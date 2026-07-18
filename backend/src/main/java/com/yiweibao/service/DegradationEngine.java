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
