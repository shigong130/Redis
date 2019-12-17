package com.neu.dao;

import com.neu.Util.TestUtil;
import com.neu.pojo.Ride;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;

import java.sql.SQLException;

public class RedisHelper {

    /*---------------------------Redis Post Operation----------------------*/

    private static String generateRedisDbKey(String skierId, String resortId){
        return String.format("@%s#%s", skierId, resortId);
    }

    private static String generateRedisDbValue(String seasonId, String dayId, String time, String liftId) {
        String vertical = String.valueOf(Integer.valueOf(liftId)*10);
        return String.format("%s,%s,%s,%s,%s", seasonId, dayId, time, liftId, vertical);
    }

    /**
     * Save a SkiRecord to Redis database
     * @param skiRecord
     */
    public static void saveSkiRecordToRedisDb(Ride skiRecord){
        String skierId = String.valueOf(skiRecord.getSkierId());
        String resortId = String.valueOf(skiRecord.getResortId());

        String seasonId = String.valueOf(skiRecord.getSeason());
        String dayId = String.valueOf(skiRecord.getDay());
        String time = String.valueOf(skiRecord.getTime());
        String liftId = String.valueOf(skiRecord.getLiftId());

        if(TestUtil.doUseRedisCache) {
            CacheHelper.deleteRecordFromRedisCache(skierId, resortId, seasonId, dayId);
        }

        String dbQueryKey = generateRedisDbKey(skierId, resortId);
        String dbQueryValue = generateRedisDbValue(seasonId, dayId, time, liftId);
        System.out.println("save a record to redis: "+dbQueryKey + "  " + dbQueryValue);
        redisSetList(dbQueryKey, dbQueryValue);
    }

    /*---------------------------Redis Get Operation----------------------*/

    /**
     * Get total vertical from db table
     * @param skierId
     * @param resortId
     * @param seasonId
     * @return
     */
    public static int getTotalVerticalFromRedis(String skierId, String resortId, String seasonId){
        String dbKey = generateRedisDbKey(String.valueOf(skierId), resortId);

        RList<String> list = redisGetList(dbKey);
        if(list.isEmpty()) return 0;

        int vertical = 0;

        for(String s : list) {
            String[] seg = s.split(",");

            if(seasonId==null || seasonId.length()==0){
                vertical+=Integer.valueOf(seg[4]);
            }else {
                if(seasonId.equals(seg[0])){
                    vertical+=Integer.valueOf(seg[4]);
                }
            }
        }

        return vertical;
    }

    private static int getVerticalFromRedisDbWithRideInfo(String skierId, String resortId, String seasonId, String dayId){
        String dbKey = generateRedisDbKey(String.valueOf(skierId), resortId);

        RList<String> list = redisGetList(dbKey);
        if(list.isEmpty()) return 0;

        int vertical = 0;

        for(String s : list) {
            String[] seg = s.split(",");
            if(seg[0].equals(seasonId) && seg[1].equals(dayId)){
                vertical+=Integer.valueOf(seg[4]);
            }
        }

        return vertical;
    }

    /**
     * Handle the get request to get vertical with a combination of skierId and dayId
     * @param skierId
     * @param dayId
     * @return integer of vertical
     * @throws SQLException
     */
    public static int getVerticalFromRedis(String skierId, String resortId, String seasonId, String dayId) throws SQLException {
        if(TestUtil.doUseRedisCache) {
            int value = CacheHelper.getVerticalFromRedisCache(skierId, resortId, seasonId, dayId);
            if(value!=0) return value;
        }

        //Read db if no value from cache
        int value = getVerticalFromRedisDbWithRideInfo(skierId, resortId, seasonId, dayId);

        if(TestUtil.doUseRedisCache) {
            CacheHelper.writeVertialToRedisCache(skierId, resortId, seasonId, dayId, value);
        }

        return value;
    }

    public static class CacheHelper {
        public static String generateKeyOfRedisCache(String skierId, String resortId, String seasonId, String dayId) {

            return "R" + String.valueOf(skierId)+"#" + resortId+"#" + seasonId + "#" + dayId;
        }

        public static int getVerticalFromRedisCache(String skierId, String resortId, String seasonId, String dayId) {
            String key = generateKeyOfRedisCache(skierId, resortId, seasonId, dayId);
            int vertical = redisGet(key);
            if (vertical == 0)
                return 0;
            else
                return vertical;
        }

        public static void writeVertialToRedisCache(String skierId, String resortId, String seasonId, String dayId, int vertical) {
            String key = generateKeyOfRedisCache(skierId, resortId, seasonId, dayId);
            redisSet(key, vertical);
        }

        public static void deleteRecordFromRedisCache(String skierId, String resortId, String seasonId, String dayId) {
            String key = generateKeyOfRedisCache(skierId, resortId, seasonId, dayId);
            redisDelete(key);
        }
    }

    public static int redisGet(String key){
        RBucket<Integer> bucket = RedisRedissonUtil.getRedisClient().getBucket(key);
        if(!bucket.isExists()) return 0;
        return bucket.get();
    }

    public static void redisSet(String key, int value){
        RBucket<Integer> bucket = RedisRedissonUtil.getRedisClient().getBucket(key);
        bucket.set(value);
    }

    public static void redisDelete(String key){
        RBucket<Integer> bucket = RedisRedissonUtil.getRedisClient().getBucket(key);
        bucket.delete();
    }

    public static void redisSetList(String key, String value) {
        RList <String> list = RedisRedissonUtil.getRedisClient().getList(key);
        list.add(value);
    }

    public static RList<String> redisGetList(String key) {
        RList<String> list = RedisRedissonUtil.getRedisClient().getList(key);
        return list;
    }

}
