package connections;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;


public class ConnectionToDB  {

    public Connection getConnection() throws SQLException {
        InputStream input = null;
        try {
            input = new FileInputStream("src/main/resources/config.properties");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Properties prop = new Properties();

        try {
            prop.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // get the property value and print it out
        System.out.println(prop.getProperty("db.url"));
        System.out.println(prop.getProperty("db.user"));
        System.out.println(prop.getProperty("db.password"));
        System.out.println(prop.getProperty("db.driver"));

        try {
            Class.forName(prop.getProperty("db.driver"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return DriverManager.getConnection(prop.getProperty("db.url"), prop.getProperty("db.user"), prop.getProperty("db.password"));

    }








}




