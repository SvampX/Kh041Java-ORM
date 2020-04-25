package annotations.handlers;

import annotations.handlers.configuration.ExtendedEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.reflections.Reflections;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EntityToTableParserTest {
    private Set<DBTable> dbTables;
    private Set<String> extendedColumnsNames;
    private Set<Type> extendedColumnsTypes;

    @BeforeAll
    void initContext() {
        Reflections reflections = new Reflections(ExtendedEntity.class);
        EntityHandler.setReflections(reflections);
        dbTables = EntityToTableMapper.parse();
        extendedColumnsNames = Set.of("name_id","user_name", "real_name", "java_object");
        extendedColumnsTypes = Set.of(Type.STRING, Type.INTEGER, Type.OTHER);
    }

    @Test
    void defaultAnnotationNames() {
        DBTable simpleTable = new DBTable();
        for (DBTable dbt : dbTables) {
            if (dbt.getName().equals("")) {
                simpleTable = dbt;
                break;
            }
        }
        assertEquals("", simpleTable.getName());
        Set<String> columnNames = simpleTable.getColumnSet().stream().map(DBColumn::getName).collect(Collectors.toSet());
        columnNames.forEach(columnName -> assertEquals("", columnName));
    }

    @Test
    void entityParsingTest(){
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
        extendedTable.getColumnSet().stream().map(DBColumn::getType).forEach(type -> assertTrue(extendedColumnsTypes.contains(type)));
        assertEquals("name_id", primaryKey.getName());
        assertEquals("INTEGER", Type.INTEGER.getSqlType());
        assertEquals(ExtendedEntity.class, extendedTable.getMyEntityClass());
        System.out.println("extendedTable = " + extendedTable);
    }
}