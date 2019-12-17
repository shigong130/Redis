package com.neu.dao;

import com.neu.pojo.Ride;
import com.neu.pojo.Stat;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StatDao {


    public boolean insertStat(Stat s) {
        Connection conn = ConnectionFactory.getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO Stat VALUES (?, ?, ?, ?, ?, ?)");
            ps.setString(1, s.getUrl());
            ps.setString(2, s.getAction());
            ps.setLong(3, s.getCount());
            ps.setDouble(4, s.getMean());
            ps.setLong(5, s.getMax());
            ps.setTimestamp(6, s.getUpdatedTime());

            int i = ps.executeUpdate();

            return i==1;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }


    public List<Stat> getStatByUrlAndAction(String url, String action) {
        List<Stat> list = new ArrayList<Stat>();
        Connection conn = ConnectionFactory.getConnection();

        try {
            Statement stmt = conn.createStatement();
            String template = "SELECT * FROM Stat where url = \"%s\" And action = \"%s\"";
            ResultSet rs = stmt.executeQuery(String.format(template, url, action));

            while(rs.next())
            {
                Stat s = new Stat();
                s.setUrl(rs.getString("url"));
                s.setAction(rs.getString("action"));
                s.setCount(rs.getLong("count"));
                s.setMean(rs.getDouble("mean"));
                s.setMax(rs.getLong("max"));
                s.setUpdatedTime(rs.getTimestamp("time"));

                list.add(s);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
    }


    public static void test1(){
        Stat s = new Stat();
        s.setUrl("Test");
        s.setAction("Test");
        s.setCount(100);
        s.setMean(5.5D);
        s.setMax(10);

        Date date= new Date();
        long time = date. getTime();
        Timestamp ts = new Timestamp(time);
        s.setUpdatedTime(ts);

        new StatDao().insertStat(s);

    }

    public static void test2() {
        List<Stat> list = new StatDao().getStatByUrlAndAction("Test", "Test");
        for(Stat s : list) {
            System.out.println(s.toString());
        }

    }

    public static void main(String[] args){
        test2();
    }
}
