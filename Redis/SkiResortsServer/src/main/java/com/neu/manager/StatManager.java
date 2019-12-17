package com.neu.manager;

import com.neu.dao.StatDao;
import com.neu.pojo.Stat;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class StatManager {
    private StatDao statDao;

    public StatManager() {
        statDao = new StatDao();
    }

    public void addStat(String url, String action, long count, double mean, long max){
        Stat s = new Stat();
        s.setUrl(url);
        s.setAction(action);
        s.setCount(count);
        s.setMean(mean);
        s.setMax(max);

        Date date= new Date();
        long time = date. getTime();
        Timestamp ts = new Timestamp(time);
        s.setUpdatedTime(ts);

        statDao.insertStat(s);
    }

    public Stat queryStat(String url, String action, long count, double mean, long max) {

        if (!("skiers".equals(url) || "resorts".equals(url)) ||
                !("get".equals(action) || "post".equals(action))) {
            return null;
        }

        List<Stat> list = statDao.getStatByUrlAndAction(url, action);

        long tempMax = 0;
        double tempTotal = 0;
        long tempCount = 0;
        for(Stat s : list){
            tempCount += s.getCount();
            tempTotal += s.getCount() * s.getMean();
            tempMax = Math.max(tempMax, s.getMax());
        }

        tempCount+=count;
        tempTotal+=count*mean;
        tempMax=Math.max(tempMax, max);


        Stat stat = new Stat();
        stat.setMean(tempTotal/tempCount);
        stat.setMax(tempMax);

        return stat;
    }

    public static void main(String[] args) {
        StatManager sm = new StatManager();
        Stat stat = sm.queryStat("Test", "Test", 300, 30, 9);
        System.out.println(stat.getMax()+" "+stat.getMean());
    }


}
