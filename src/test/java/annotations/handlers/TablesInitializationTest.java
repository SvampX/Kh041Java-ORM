package annotations.handlers;

import annotations.handlers.configuration.ExtendedEntity;
import connections.ConnectionToDB;
import crud.CrudServices;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.reflections.Reflections;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TablesInitializationTest {
    private static Connection connection;
    private static DBTable tableToCreate;

    @BeforeAll
    void init() {
        tableToCreate = new DBTable();
        Reflections reflections = new Reflections(ExtendedEntity.class);
        EntityHandler.setReflections(reflections);
        EntityToTableMapper.getTables().
                stream().
                filter(dbTable -> dbTable.getName().equals("1stTable")).
                peek(dbTable -> tableToCreate.setPrimaryKey(dbTable.getPrimaryKey())).
                map(DBTable::getColumnSet).
                forEach(tableToCreate::setColumnSet);
        tableToCreate.setName("create_table_test");
        ConnectionToDB connectionToDB;
        try {
            connectionToDB = ConnectionToDB.getInstance();
            connection = connectionToDB.getConnection();
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
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
    void TableCreationTest() {
        CrudServices crudServices = new CrudServices();
        crudServices.initTables(connection);

        String dropTestTables = "DROP TABLE IF EXISTS test_users;\n" +
                "DROP TABLE IF EXISTS firstTable;\n" +
                "DROP TABLE IF EXISTS phones;\n" +
                "DROP TABLE IF EXISTS addresses; \n" +
                "DROP TABLE IF EXISTS secondTable; ";
        try {
            Statement statement = connection.createStatement();
            //statement.execute(dropTestTables);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
