package com.neu.pojo;

import java.sql.Timestamp;

public class Stat {

    private String url;
    private String action;
    private long count;
    private double mean;
    private long max;
    private Timestamp updatedTime;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public Timestamp getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Timestamp updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public String toString() {
        return "Stat{" +
                "url='" + url + '\'' +
                ", action='" + action + '\'' +
                ", count=" + count +
                ", mean=" + mean +
                ", max=" + max +
                ", updatedTime=" + updatedTime +
                '}';
    }
}
