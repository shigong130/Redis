package com.neu.manager;

import com.neu.Util.TestUtil;
import com.neu.dao.LiftDao;
import com.neu.dao.RedisHelper;
import com.neu.dao.RideDao;
import com.neu.pojo.Lift;
import com.neu.pojo.ResortSeason;
import com.neu.pojo.Ride;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SkierManager {
    private static final int THRESHOLD = 1000;

    private static SkierManager skierManager = null;


    RideDao rideDao = new RideDao();
    LiftDao liftDao = new LiftDao();
    //Lazy initialization;
    Map<Integer, Integer> verticalMap;
    BlockingQueue<Ride> queue;

    public SkierManager(BlockingQueue<Ride> queue) {
        rideDao = new RideDao();
        liftDao = new LiftDao();
        this.queue = queue;

    }


    public boolean addRide(int skierId, int resortId, int season, int day, int liftId, int time){
        if(TestUtil.doUseRedisCache) {
            RedisHelper.CacheHelper.deleteRecordFromRedisCache(String.valueOf(skierId), String.valueOf(resortId),
                    String.valueOf(season), String.valueOf(day));
        }

        if(TestUtil.doUseRedisDb){
            Ride ride = new Ride(skierId, resortId, season, day, liftId, time);
            RedisHelper.saveSkiRecordToRedisDb(ride);
            return true;
        }else{
            return addRideToRds(skierId, resortId, season, day, liftId, time);
        }
    }


    public boolean addRideToRds(int skierId, int resortId, int season, int day, int liftId, int time) {
        Ride r = new Ride();
        r.setTime(time);
        r.setLiftId(liftId);
        r.setDay(day);
        r.setSeason(season);
        r.setResortId(resortId);
        r.setSkierId(skierId);

        boolean result = rideDao.insertRide(r);
        return result;
    }


    public String getVerticalBySkier(int skierId, int resortId) {
        return getVerticalBySkier(skierId, resortId, -1);
    }


    public String getVerticalBySkier(int skierId, int resortId, int season) {
        getVerticalMap();
        int result = 0;
        List<Ride> rideList = rideDao.getRidesBySkierId(skierId, resortId);

        for(Ride r : rideList){
            if(season!=-1 && r.getSeason()!=season){
                continue;
            }

            int liftId = r.getLiftId();
            if(verticalMap.containsKey(liftId)) {
                result += verticalMap.get(liftId);
            }
        }

        if(season!=-1) {
            String jsonTemplate = "{\n" +
                    "  \"resorts\": [\n" +
                    "    {\n" +
                    "      \"seasonID\": \"%d\",\n" +
                    "      \"totalVert\": %d\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            return String.format(jsonTemplate, season, result);
        } else {
            String jsonTemplate = "{\n" +
                    "  \"resorts\": [\n" +
                    "    {\n" +
                    "      \"totalVert\": %d\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            return String.format(jsonTemplate, result);
        }

    }


    public int getVerticalFromMysql(String skierId, String resortId, String seasonId, String dayId) throws SQLException {
        if(TestUtil.doUseRedisCache) {
            int value = RedisHelper.CacheHelper.getVerticalFromRedisCache(skierId, resortId, seasonId, dayId);
            if(value!=0) return value;
        }

        //Read db if no value from cache
        String v = getVerticalBySkierAndDay(Integer.valueOf(skierId), Integer.valueOf(resortId),
                Integer.valueOf(seasonId), Integer.valueOf(dayId));

        if(TestUtil.doUseRedisCache) {
            RedisHelper.CacheHelper.writeVertialToRedisCache(skierId, resortId, seasonId, dayId, Integer.valueOf(v));
        }

        return Integer.valueOf(v);
    }


    public String getVerticalBySkierAndDay(int skierId, int resortId, int season, int day) {
        //getVerticalMap();
        int result = 0;
        List<Ride> rideList = rideDao.getRidesBySkierTrip(skierId, resortId, season, day);

        for(Ride r : rideList){
            int liftId = r.getLiftId();

            result+=(liftId*10);
        }

        return new Integer(result).toString();
    }


    private void getVerticalMap(){
        if (verticalMap != null) return;

        verticalMap = new HashMap<Integer, Integer>();

        List<Lift> liftList = liftDao.getAllLifts();
        if(liftList==null || liftList.size()==0) return;

        for(Lift lift : liftList){
            verticalMap.put(lift.getId(), lift.getVertical());
        }
    }

    public static void main(String[] args){
    }
}
