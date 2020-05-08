package annotations.handlers;

import connections.ConnectionToDB;
import crud.CrudServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RelationsLoaderTest {

    Connection connection;
    CrudServices crudServices;

    @BeforeEach
    void setUp() {
        try {
            ConnectionToDB connectionToDB = ConnectionToDB.getInstance();
            connection = connectionToDB.getConnection();
            crudServices = new CrudServices();
            crudServices.setConnection(connection);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    @Test
    void test(){

    }

}