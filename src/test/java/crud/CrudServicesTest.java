package crud;

import annotations.handlers.TablesInitializationTest;
import annotations.handlers.configuration.SimpleEntity;
import connections.ConnectionToDB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

class CrudServicesTest {
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
    void readEntityById() {

        SimpleEntity simpleEntity = (SimpleEntity) crudServices.readEntityById( 1, SimpleEntity.class);
        System.out.println("simpleEntity = " + simpleEntity);
    }

    @Test
    void createEntityTest() {
        TablesInitializationTest tablesInitializationTest = new TablesInitializationTest();
        tablesInitializationTest.tableCreationTest();
    }
}