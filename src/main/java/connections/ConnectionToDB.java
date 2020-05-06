package connections;

import annotations.handlers.EntityHandler;
import org.reflections.Reflections;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class ConnectionToDB {

    private List<Connection> availableConList = new ArrayList<>();
    private List<Connection> unvailableConList = new ArrayList<>();
    private int defaultConNumber = 10;
    String driver = "";

    private static ConnectionToDB instance;

    public static synchronized ConnectionToDB getInstance() throws SQLException {
        if (instance == null) {
            instance = new ConnectionToDB();
        }
        return instance;
    }

    private ConnectionToDB() throws SQLException {
        getUsersMetadata();
        for (int i = 0; i < defaultConNumber; i++)
            availableConList.add(createConnection());
    }

    public static void getUsersMetadata() {
        StackTraceElement e[] = Thread.currentThread().getStackTrace();
        String callingClassName = e[4].getClassName();
        System.out.println(callingClassName);
        Reflections reflections;
        try {
            reflections = new Reflections(Class.forName(callingClassName));
            EntityHandler.setReflections(reflections);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    private Connection createConnection() throws SQLException {
        InputStream input = null;
        final URL resource = this.getClass().getClassLoader().getResource("test.properties");
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
        return DriverManager.getConnection(prop.getProperty("db.url"), prop.getProperty("db.user"),
                prop.getProperty("db.password"));
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
        if (availableConList.size() > 0) {
            con = availableConList.get(0);
            unvailableConList.add(con);
            availableConList.remove(con);
        } else {
            con = getConnection();
            unvailableConList.add(con);
        }
        return con;
    }

    public void releaseConnection(Connection con) {
        availableConList.add(con);
        unvailableConList.remove(con);
    }
    public String getDialect(){
        return driver.split("\\.")[1];
    }
}




