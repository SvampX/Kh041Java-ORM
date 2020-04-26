package connections;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class ConnectionToDB {

    private List<Connection> availableConList = new ArrayList<>();
    private List<Connection> unvailableConList = new ArrayList<>();
    private int defaultConNumber = 10;

    public ConnectionToDB() throws SQLException {
        for (int i = 0; i < defaultConNumber; i++)
            availableConList.add(createConnection());
    }

    private Connection createConnection() throws SQLException {
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

    public Connection getConnection() {
        Connection con = availableConList.get(0);
        unvailableConList.add(con);
        availableConList.remove(con);
        return con;
    }

    public void releaseConnection(Connection con) {
        availableConList.add(con);
        unvailableConList.remove(con);
    }
}




