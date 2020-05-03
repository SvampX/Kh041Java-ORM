package connections;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class ConnectionToDB {

    private List<Connection> availableConList = new ArrayList<>();
    private List<Connection> unvailableConList = new ArrayList<>();
    private int defaultConNumber = 10;
    String driver = "";

    public ConnectionToDB() throws SQLException {
        for (int i = 0; i < defaultConNumber; i++)
            availableConList.add(createConnection());
    }

    private Connection createConnection() throws SQLException {
        InputStream input = null;
        final URL resource = this.getClass().getClassLoader().getResource("config.properties");
        try {
            input = new FileInputStream(resource.getPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Properties prop = getProperties(input);

        try {
            Class.forName(prop.getProperty("db.driver"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        driver = prop.getProperty("db.driver");
        return DriverManager.getConnection(prop.getProperty("db.url"), prop.getProperty("db.user"), prop.getProperty("db.password"));
    }

    private Properties getProperties(InputStream input) {
        Properties prop = new Properties();

        try {
            prop.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }

    public Connection getConnection() {
        Connection con;
        if(availableConList.size()>0) {
            con = availableConList.get(0);
            unvailableConList.add(con);
            availableConList.remove(con);
            return con;
        }else{
            con = getConnection();
            unvailableConList.add(con);
            return con;
        }
    }
    public void releaseConnection(Connection con) {
        availableConList.add(con);
        unvailableConList.remove(con);
    }
    public String getDriver(){
       return driver.split("\\.")[1];
    }
}



