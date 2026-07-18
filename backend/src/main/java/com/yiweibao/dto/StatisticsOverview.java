package com.yiweibao.dto;

public class StatisticsOverview {
    private long totalFaults;
    private long thisMonthFaults;
    private long pendingOrders;
    private double avgRepairHours;

    public StatisticsOverview(long totalFaults, long thisMonthFaults, long pendingOrders, double avgRepairHours) {
        this.totalFaults = totalFaults;
        this.thisMonthFaults = thisMonthFaults;
        this.pendingOrders = pendingOrders;
        this.avgRepairHours = avgRepairHours;
    }

    public long getTotalFaults() { return totalFaults; }
    public long getThisMonthFaults() { return thisMonthFaults; }
    public long getPendingOrders() { return pendingOrders; }
    public double getAvgRepairHours() { return avgRepairHours; }
}
