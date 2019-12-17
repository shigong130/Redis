package com.neu.dao;

import com.neu.pojo.ResortSeason;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResortSeasonDao {

    public static void main(String[] args) {
        ResortSeasonDao dao = new ResortSeasonDao();
        List<Integer> list = dao.getSeasonsByResortId(1);
        System.out.println(list.size());
        for(int i : list){
            System.out.println(i);
        }
    }

    public boolean insertResortSeason(ResortSeason rs){
        Connection conn = ConnectionFactory.getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO ResortSeason VALUES (?, ?)");
            ps.setInt(1, rs.getResort());
            ps.setInt(2, rs.getSeason());
            int i = ps.executeUpdate();

            return i==1;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public List<Integer> getSeasonsByResortId(int resortId) {
        List<Integer> list = new ArrayList<Integer>();
        Connection conn = ConnectionFactory.getConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM ResortSeason where resortId = "+resortId);

            while(rs.next())
            {
                ResortSeason resortSeason = new ResortSeason();
                list.add(rs.getInt("season"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
    }

}
