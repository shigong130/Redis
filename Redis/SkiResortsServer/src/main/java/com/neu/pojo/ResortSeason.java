package com.neu.pojo;

public class ResortSeason {
    private int resort;
    private int season;

    public int getResort() {
        return resort;
    }

    public void setResort(int resort) {
        this.resort = resort;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    @Override
    public String toString() {
        return "ResortSeason{" +
                "resort=" + resort +
                ", season=" + season +
                '}';
    }
}
