package com.yiweibao.dto;

public class FaultTypeAvgTime {
    private String type;
    private long count;
    private double avgHours;

    public FaultTypeAvgTime(String type, long count, double avgHours) {
        this.type = type;
        this.count = count;
        this.avgHours = avgHours;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
    public double getAvgHours() { return avgHours; }
    public void setAvgHours(double avgHours) { this.avgHours = avgHours; }
}
