package com.neu.dao;

import com.neu.pojo.Lift;
import com.neu.pojo.Ride;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class RideDao {

    public List<Ride> getRidesBySkierTrip(int skierId, int resortId, int season, int day) {
        List<Ride> list = new ArrayList<Ride>();
        Connection conn = ConnectionFactory.getConnection();

        try {
            Statement stmt = conn.createStatement();
            String template = "SELECT * FROM Ride where skierId = %d And resortId = %d And season = %d And day = %d";
            ResultSet rs = stmt.executeQuery(String.format(template, skierId, resortId, season, day));

            while(rs.next())
            {
                Ride ride = new Ride();
                ride.setSkierId(rs.getInt("skierId"));
                ride.setDay(rs.getInt("day"));
                ride.setLiftId(rs.getInt("liftId"));
                ride.setResortId(rs.getInt("resortId"));
                ride.setSeason(rs.getInt("season"));
                ride.setTime(rs.getInt("time"));

                list.add(ride);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
    }

    public boolean insertRide(Ride r) {
        Connection conn = ConnectionFactory.getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO Ride VALUES (?, ?, ?, ?, ?, ?)");
            ps.setInt(1, r.getSkierId());
            ps.setInt(2, r.getResortId());
            ps.setInt(3, r.getSeason());
            ps.setInt(4, r.getDay());
            ps.setInt(5, r.getLiftId());
            ps.setInt(6, r.getTime());

            int i = ps.executeUpdate();

            return i==1;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }


    public boolean batchInsertRide(BlockingQueue<Ride> list) {
        for(Ride r : list){
            this.insertRide(r);
        }

        return true;
    }

    public boolean batchInsertRide2(BlockingQueue<Ride> list) {
        Connection conn = ConnectionFactory.getConnection();
        String sqlTemplate = "INSERT INTO Ride (skierId, resortId, season, day, liftId, time) " +
                " VALUES (%d, %d, %d, %d, %d, %d)";
        try {
            Statement stmt = conn.createStatement();
            for(Ride r : list) {
                String sql = String.format(sqlTemplate, r.getSkierId(),
                        r.getResortId(), r.getSeason(), r.getDay(), r.getLiftId(), r.getTime());
                stmt.addBatch(sql);
                System.out.println("sql : "+sql);
            }
            int[] count = stmt.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public List<Ride> getRidesBySkierId(int skierId, int resortId) {
        List<Ride> list = new ArrayList<Ride>();
        Connection conn = ConnectionFactory.getConnection();

        try {
            Statement stmt = conn.createStatement();
            String template = "SELECT * FROM Ride where skierId = %d and resortId = %d";
            ResultSet rs = stmt.executeQuery(String.format(template, skierId, resortId));

            while(rs.next())
            {
                Ride ride = new Ride();
                ride.setSkierId(rs.getInt("skierId"));
                ride.setDay(rs.getInt("day"));
                ride.setLiftId(rs.getInt("liftId"));
                ride.setResortId(rs.getInt("resortId"));
                ride.setSeason(rs.getInt("season"));
                ride.setTime(rs.getInt("time"));

                list.add(ride);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
    }

    public static void main(String[] args) {

        RideDao dao = new RideDao();
        /*
        List<Ride> list = dao.getRidesBySkierTrip(1, 1, 2018, 1);
        for(Ride ride : list){
            System.out.println(ride);
        }

        System.out.println();
    */
//        List<Ride> list2 = dao.getRidesBySkierId(1, 1);
//        for(Ride ride : list2){
//            System.out.println(ride);
//        }


        ArrayBlockingQueue<Ride> list = new ArrayBlockingQueue<Ride>(10000);
        Ride ride = new Ride();
        ride.setSeason(2019);
        ride.setDay(200);
        ride.setSkierId(300);
        ride.setResortId(2);
        ride.setLiftId(3);
        ride.setTime(111);

        list.add(ride);
        dao.batchInsertRide(list);

        //System.out.println(dao.insertRide(ride));


        /*
        List list2 = dao.getRidesBySkierId(300);
        for(Ride rr : list2){
            System.out.println(rr);
        }
        */




    }

}
