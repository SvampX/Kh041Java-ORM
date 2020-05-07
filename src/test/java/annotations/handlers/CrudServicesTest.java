package annotations.handlers;

import annotations.handlers.configuration.Phone;
import connections.ConnectionToDB;
import crud.CrudServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

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
    void createEntityTest() {
        Phone phone = new Phone();
        phone.setNumber("937-99-92");
        crudServices.create(phone);
    }

    @Test
    void readEntities() {
        Phone phone = (Phone) crudServices.readById(1, Phone.class);
        System.out.println("Phone = " + phone);
        Set<Object> entities = crudServices.readAll(Phone.class);
        System.out.println("\n All entities from phone table");
        entities.forEach(System.out::println);
    }

    @Test
    void updateEntitiesTest() {
        Phone phone = new Phone();
        phone.setId(5);
        phone.setNumber("5555555");
        crudServices.update(phone);
    }

    @Test
    void deleteByIdTest() {
        int id = 3;
        crudServices.delete(id, Phone.class);
    }


    @Test
    void readEntitiesByInitializedFields() {
        Phone phone = new Phone();
        phone.setNumber("937-99-92");
        Set<Object> phoneSet;
        phoneSet = crudServices.readEntityByPartialInitializedInstance(phone);
        phoneSet.forEach(phon -> {
            System.out.println("id = " + ((Phone) phon).getId() + "   " + ((Phone) phon).getNumber());
        });
    }


}