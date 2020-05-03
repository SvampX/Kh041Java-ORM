package annotations.handlers;

import annotations.handlers.configuration.ExtendedEntity;
import connections.ConnectionToDB;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.reflections.Reflections;

import java.sql.SQLException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EntityToTableMapperTest {
    private Set<DBTable> dbTables;
    private Set<String> extendedColumnsNames;
    private Set<Type> extendedColumnsTypes;
    private Set<String> simpleColumnsNames;

    @BeforeAll
    void initContext() {
        try {
            ConnectionToDB contextInitPoint= new ConnectionToDB();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        Reflections reflections = new Reflections(ExtendedEntity.class);
        EntityHandler.setReflections(reflections);
        dbTables = EntityToTableMapper.getTables();
        extendedColumnsNames = Set.of("name_id", "user_name", "real_name", "java_object");
        extendedColumnsTypes = Set.of(Type.STRING, Type.INTEGER, Type.OTHER);
        simpleColumnsNames = Set.of("id", "userName");
    }

    @Test
    void defaultAnnotationNames() {
        DBTable simpleTable = new DBTable();
        for (DBTable dbt : dbTables) {
            if (dbt.getName().equals("SimpleEntity")) {
                simpleTable = dbt;
                break;
            }
        }
        assertEquals("SimpleEntity", simpleTable.getName());
        Set<String> columnNames = simpleTable.getColumnSet().stream().map(DBColumn::getName).collect(Collectors.toSet());
        columnNames.forEach(columnName -> assertTrue(simpleColumnsNames.contains(columnName)));
    }

    @Test
    void entityParsingTest() {
        DBTable extendedTable = new DBTable();
        for (DBTable dbt : dbTables) {
            if (dbt.getName().equals("table_name")) {
                extendedTable = dbt;
                break;
            }
        }
        DBColumn primaryKey = extendedTable.getPrimaryKey();
        Set<String> columnNames = extendedTable.getColumnSet().stream().map(DBColumn::getName).collect(Collectors.toSet());
        columnNames.forEach(column -> assertTrue(extendedColumnsNames.contains(column)));
        extendedTable.
                getColumnSet().stream().
                map(DBColumn::getType).
                forEach(type -> assertTrue(extendedColumnsTypes.contains(type)));
        assertEquals("name_id", primaryKey.getName());
        assertEquals("INTEGER", Type.INTEGER.getSqlType());
        assertEquals(ExtendedEntity.class, extendedTable.getMyEntityClass());
        System.out.println("extendedTable = " + extendedTable);
    }

    @ParameterizedTest
    @MethodSource(value = "prepareData")
    public void test(String value, boolean isResultEnable) {

        System.out.println(value);
    }

    private Stream<Arguments> prepareData() {
        return Stream.of(Arguments.arguments("FIRST_TEST ", true),
                Arguments.arguments("Second_test ", false));
    }
}