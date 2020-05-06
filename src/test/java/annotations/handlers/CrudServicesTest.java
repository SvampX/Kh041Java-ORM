package crud;

import annotations.handlers.DBTable;
import annotations.handlers.EntityToTableMapper;
import annotations.handlers.configuration.Phone;
import annotations.handlers.configuration.SimpleEntity;
import connections.ConnectionToDB;
import crud.CrudServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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
    void initTables() {
        crudServices.initTables(connection);
    }

    @Test
    void readEntityById() {
        Set<DBTable> tables = EntityToTableMapper.getTables();
        SimpleEntity simpleEntity = (SimpleEntity) crudServices.readById( 1, SimpleEntity.class);
        System.out.println("simpleEntity = " + simpleEntity);
    }

    @Test
    void createEntityTest() {
        Phone phone = new Phone();
        phone.setNumber("937-99-92");
        crudServices.create(phone);
    }
}