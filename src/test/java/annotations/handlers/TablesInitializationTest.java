package annotations.handlers;

import annotations.handlers.configuration.Phone;
import connections.ConnectionToDB;
import crud.CrudServices;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TablesInitializationTest {
    private static Connection connection;
    private static Set<DBTable> tables;
    private static CrudServices crudServices;

    @BeforeAll
    void init() {
        ConnectionToDB connectionToDB;
        try {
            connectionToDB = ConnectionToDB.getInstance();
            connection = connectionToDB.getConnection();
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        tables = EntityToTableMapper.getTables();
        crudServices = new CrudServices();
        RelationsWithOneHandler relationsWithOneHandler = new RelationsWithOneHandler();
        relationsWithOneHandler.handle(EntityHandler.getEntitiesSet());
    }

    @Test
    void connectionTest() {
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT email FROM users WHERE id=2;");
            while (rs.next()) {
                String email = rs.getString("email");
                System.out.println("email: " + email);
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
    }

    @Test
    public void tableCreationTest() {
        Phone phone = new Phone();
        phone.setNumber("937-99-92");
        crudServices.setConnection(connection);
        crudServices.create(phone);
//        CrudServices crudServices = new CrudServices();
//        crudServices.initTables(connection);
    }

    @Test
    private void tableRecordCreateTest() {


    }

    @Test
    public void eraseTables() {
        boolean dropTablesAfterTest = true;
        StringBuilder dropTablesQuery = new StringBuilder();
        List<DBTable> relationTables = ManyToManyHandler.getRelationTables();
        for (DBTable table : tables) {
            dropTablesQuery.append("DROP TABLE IF EXISTS ").
                    append(table.getName()).
                    append(" CASCADE;\n");
        }
        for (DBTable table : relationTables) {
            dropTablesQuery.append("DROP TABLE IF EXISTS ").
                    append(table.getName()).
                    append(" CASCADE;\n");
        }
        if (dropTablesAfterTest) {
            try {
                Statement statement = connection.createStatement();
                statement.execute(dropTablesQuery.toString());
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

}
