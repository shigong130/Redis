package com.neu.dao;

import com.neu.pojo.Lift;
import com.neu.pojo.Resort;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ResortDao {

    public static void main(String[] args) {
        ResortDao dao = new ResortDao();
        List<Resort> list = dao.getAllResorts();
        System.out.println(list.size());
        for(Resort r : list){
            System.out.println(r.getId()+" "+r.getName());
        }
    }

    public List<Resort> getAllResorts() {
        List<Resort> list = new ArrayList<Resort>();
        Connection conn = ConnectionFactory.getConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Resort");

            while(rs.next())
            {
                Resort resort = new Resort();
                resort.setId(rs.getInt("id"));
                resort.setName(rs.getString("name"));
                list.add(resort);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
    }
}
