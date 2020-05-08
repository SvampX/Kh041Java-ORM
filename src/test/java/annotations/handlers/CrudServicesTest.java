package annotations.handlers;

import annotations.handlers.configuration.*;
import connections.ConnectionToDB;
import crud.CrudServices;
import crud.RelationsLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

class CrudServicesTest {

    Connection connection;
    CrudServices crudServices;
    RelationsLoader relationsLoader;

    @BeforeEach
    void setUp() {
        try {
            ConnectionToDB connectionToDB = ConnectionToDB.getInstance();
            connection = connectionToDB.getConnection();
            crudServices = new CrudServices();
            crudServices.setConnection(connection);
            relationsLoader = new RelationsLoader();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Test
    void createEntityTest() {
        User testUser = new User();
        testUser.setFirstName("Alex");
        testUser.setLastName("Lens");
        testUser.setAge(25);
        Phone testPhone = new Phone();
        testPhone.setNumber("45646464");
        testPhone.setUser(testUser);
        testUser.setPhone(testPhone);
        Car car = new Car("LADA", "1531654");
        car.setUser(testUser);
        testUser.setCar(car);
        Phone phone = new Phone();
        phone.setNumber("12-12-121");
        User user = new User();
        user.setAge(12);
        user.setFirstName("Bodya");
        user.setLastName("Angazalka");
        user.setPhone(phone);
        phone.setUser(user);
        relationsLoader.create(testPhone);
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
        phone.setId(2);
        phone.setNumber("546846");
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

    @Test
    void test(){
        RelationsLoader relationsLoader = new RelationsLoader();
        ExtendedEntity extendedEntity = new ExtendedEntity();
        DBTable table = relationsLoader.getTableByClass(extendedEntity.getClass());
        ForeignKey foreignKey = table.getForeignKeys().iterator().next();
        Field field = relationsLoader.getRelationsField(foreignKey, extendedEntity.getClass());
        System.out.println("field.getDeclaringClass() = " + field.getDeclaringClass());
    }

}