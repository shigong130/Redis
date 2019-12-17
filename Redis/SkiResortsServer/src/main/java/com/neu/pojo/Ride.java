package com.neu.pojo;

/**
 * This class is the pojo class which maps the relational table in DB.
 */
public class Ride {

    int skierId;
    int resortId;
    int season;
    int day;
    int liftId;
    int time;

    public Ride(int skierId, int resortId, int season, int day, int liftId, int time){
        this.skierId=skierId;
        this.resortId=resortId;
        this.season=season;
        this.day=day;
        this.liftId=liftId;
        this.time=time;
    }

    public Ride(){}


    public int getSkierId() {
        return skierId;
    }

    public void setSkierId(int skierId) {
        this.skierId = skierId;
    }

    public int getResortId() {
        return resortId;
    }

    public void setResortId(int resortId) {
        this.resortId = resortId;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getLiftId() {
        return liftId;
    }

    public void setLiftId(int liftId) {
        this.liftId = liftId;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Ride{" +
                "skierId=" + skierId +
                ", resortId=" + resortId +
                ", season=" + season +
                ", day=" + day +
                ", liftId=" + liftId +
                ", time=" + time +
                '}';
    }
}
