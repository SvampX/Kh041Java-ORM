package annotations.handlers;

import annotations.handlers.configuration.ExtendedEntity;
import connections.ConnectionToDB;
import exceptions.DBException;
import exceptions.Messages;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GeneratedValueHandlerTest {
    public final static String CLEAN_QUERY = "DROP SEQUENCE IF EXISTS ";

    Reflections reflections;
    Set<Class<?>> entities;

    @BeforeEach
    void setUp() {
        reflections = new Reflections(ExtendedEntity.class);
        EntityHandler.setReflections(reflections);
        entities = EntityHandler.inspectEntities();
    }

    /**
     * Test works for existing database - testdb
     * Before test needs to set properties in resources/config.properties :
     * db.user=testuser
     * db.password=123
     * db.url=jdbc:postgresql://localhost/testdb
     */
    @Test
    void createIdGenerator() throws SQLException {
        //given
        Set<DBTable> tables = EntityToTableMapper.getTables();
        GeneratedValueHandler generatedValueHandler = new GeneratedValueHandler();

        //when, then
        for (DBTable dbTable : tables) {
            String idScript = generatedValueHandler.createIdGenerator(dbTable);
            assertNotNull(idScript);
            System.out.println(idScript);
        }
    }

    @AfterEach
    void cleanUp() throws DBException {
        dropSequence("table_name_id_seq");
        dropSequence("user_gen_seq");
    }

    private void dropSequence (String sequence) throws DBException {
        Connection connection;
        Statement statement;
        try {
            connection = ConnectionToDB.getInstance().getConnection();
            statement = connection.createStatement();
            statement.execute(CLEAN_QUERY + sequence);
        } catch (SQLException e) {
            throw new DBException(Messages.ERR_CANNOT_DELETE_SEQUENCE, e);
        }
    }
}