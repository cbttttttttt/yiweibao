package com.yiweibao.dto;

public class FaultTypeStat {
    private String type;
    private long count;

    public FaultTypeStat(String type, long count) {
        this.type = type;
        this.count = count;
    }

    public String getType() { return type; }
    public long getCount() { return count; }
}
