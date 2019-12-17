package com.neu.dao;

import com.neu.pojo.Lift;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LiftDao {

    public static void main(String[] args){
        LiftDao dao = new LiftDao();
        List<Lift> list = dao.getAllLifts();
        System.out.println(list.size());
        System.out.println(list.get(0).getId()+" "+list.get(0).getVertical());

    }

    public List<Lift> getAllLifts() {
        List<Lift> list = new ArrayList<Lift>();
        Connection conn = ConnectionFactory.getConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Lift");

            while(rs.next())
            {
                Lift lift = new Lift();
                lift.setId(rs.getInt("id"));
                lift.setVertical(rs.getInt("vertical"));
                list.add(lift);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
    }
}
