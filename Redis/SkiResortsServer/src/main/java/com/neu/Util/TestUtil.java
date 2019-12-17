package com.neu.Util;

public class TestUtil {
    public static boolean doUseRedisDb = false;//'write-heavy' use redis as a in memory database
    public static boolean doUseRedisCache = false;// 'get-heavy' use redis as a cache
    public static int redisEndpoint = 1;// 0- local , 1-single redis(cluster mode disabled, 2-redis cluster(cluster mode enabled))

}
    /*
    public static boolean doUseRedisDb = true;
    public static boolean doUseRedisCache = false;
    public static int redisEndpoint = 2;
    */

    //Test1: No cache   (1)db->mysql  (2)redis
    //Test2: db--> mysql  (1)no cache  (2)with cache
    //Test3: No cache, Db-rdis (1)endpoint1(single),  (2)endpoint2(cluster)
