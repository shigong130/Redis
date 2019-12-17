package com.neu.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class ConnectionFactory {

//    private static final String URL = "jdbc:mysql://database-west.cp9pmeuloqsu.us-east-1.rds.amazonaws.com/SkiResorts?"
//            + "user=root&password=12345678&serverTimezone=UTC";

    private static final String URL = "jdbc:mysql://database.cjlihoxcuuo7.us-west-2.rds.amazonaws.com/SkiResorts?"
            + "user=root&password=12345678&serverTimezone=UTC";


    private static Connection connection = null;
    public static Connection getConnection() {
        if(connection!=null) return connection;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager
                    .getConnection(URL);

            // Statements allow to issue SQL queries to the database
            Statement statement = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return connection;
    }
}
