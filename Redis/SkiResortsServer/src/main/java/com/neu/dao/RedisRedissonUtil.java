package com.neu.dao;

import com.neu.Util.TestUtil;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class RedisRedissonUtil {

    private static String localUrl =         "redis://localhost:6379";
    private static String redisSingleUrl =   "redis://skiresortcache.gytc6f.0001.use1.cache.amazonaws.com:6379";
    private static String redisClusterUrl =  "redis://skiresortcachecluster.gytc6f.clustercfg.use1.cache.amazonaws.com:6379";
    private static String redisClusterNode1 = "redis://skiresortcachecluster-0001-001.gytc6f.0001.use1.cache.amazonaws.com:6379";
    private static String redisClusterNode2 = "redis://skiresortcachecluster-0002-001.gytc6f.0001.use1.cache.amazonaws.com:6379";
    private static String redisClusterNode3 = "redis://skiresortcachecluster-0003-001.gytc6f.0001.use1.cache.amazonaws.com:6379";
    private static String redisClusterNode4 = "redis://skiresortcachecluster-0004-001.gytc6f.0001.use1.cache.amazonaws.com:6379";
    private static String redisClusterNode5 = "redis://skiresortcachecluster-0005-001.gytc6f.0001.use1.cache.amazonaws.com:6379";
    private static String redisClusterNode6 = "redis://skiresortcachecluster-0006-001.gytc6f.0001.use1.cache.amazonaws.com:6379";
    private static String redisClusterNode7 = "redis://skiresortcachecluster-0007-001.gytc6f.0001.use1.cache.amazonaws.com:6379";
    private static String redisClusterNode8 = "redis://skiresortcachecluster-0008-001.gytc6f.0001.use1.cache.amazonaws.com:6379";


    private static String westRedisSingleUrl = "redis://singleredis.ubxxvc.0001.usw2.cache.amazonaws.com:6379";

    private static String redisWestClusterUrl =   "redis://clusterredis.ubxxvc.clustercfg.usw2.cache.amazonaws.com:6379";
    private static String redisWestClusterNode1 = "redis://clusterredis-0001-001.ubxxvc.0001.usw2.cache.amazonaws.com:6379";
    private static String redisWestClusterNode2 = "redis://clusterredis-0002-001.ubxxvc.0001.usw2.cache.amazonaws.com:6379";
    private static String redisWestClusterNode3 = "redis://clusterredis-0003-001.ubxxvc.0001.usw2.cache.amazonaws.com:6379";


    private static RedissonClient localClient;
    private static RedissonClient singleClient;
    private static RedissonClient clusterClient;


    public static RedissonClient getRedisClient() {
        if(TestUtil.redisEndpoint ==0 ) {
            if(localClient == null) {
                initiateLocalRedis();
            }
            return localClient;
        }else if (TestUtil.redisEndpoint ==1){
            if(singleClient == null){
                initiateSingleRedis();
            }
            return singleClient;
        }else{
            if(clusterClient == null) {
                initiateClusterRedis();
            }
            return clusterClient;
        }
    }

    private static void initiateLocalRedis() {
        localClient = getRedisClient(localUrl);
    }

    private static void initiateSingleRedis() {
        singleClient = getRedisClient(westRedisSingleUrl);
    }

    private static void initiateClusterRedis() {
        clusterClient = getRedisClusterClient();
    }

    public static RedissonClient getRedisClient(String url) {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(url);

        RedissonClient client;
        try {
            client = Redisson.create(config);
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return client;
    }

    public static RedissonClient getRedisClusterClient() {
        Config config = new Config();
//        config.useClusterServers()
//                .setScanInterval(2000) // cluster state scan interval in milliseconds
//                // use "rediss://" for SSL connection
//                .addNodeAddress(redisClusterUrl)
//                .addNodeAddress(redisClusterNode1)
//                .addNodeAddress(redisClusterNode2)
//                .addNodeAddress(redisClusterNode3)
//                .addNodeAddress(redisClusterNode4)
//                .addNodeAddress(redisClusterNode5)
//                .addNodeAddress(redisClusterNode6)
//                .addNodeAddress(redisClusterNode7)
//                .addNodeAddress(redisClusterNode8);


        config.useClusterServers()
                .setScanInterval(2000) // cluster state scan interval in milliseconds
                // use "rediss://" for SSL connection
                .addNodeAddress(redisWestClusterUrl)
                .addNodeAddress(redisWestClusterNode1)
                .addNodeAddress(redisWestClusterNode2)
                .addNodeAddress(redisWestClusterNode3);

        RedissonClient client;
        try {
            client = Redisson.create(config);
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return client;
    }

    public static void main(String[] args) {
        RedissonClient client = getRedisClient(localUrl);
        System.out.println(client);
    }
}
