package annotations.handlers;

import annotations.handlers.configuration.ExtendedEntity;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import java.util.Set;

class EntityToTableParserTest {

    @Test
    void defaultAnnotationNames() {
        Reflections reflections = new Reflections(ExtendedEntity.class);
        EntityHandler.setReflections(reflections);
        Set<DBTable> dbTables = EntityToTableParser.parse();
        System.out.println("dbTables.size() = " + dbTables.size());
        for (DBTable dbt:dbTables) {
            System.out.println("dbt.getName() = " + dbt.getName());
            System.out.println("dbt.getPrimaryKey().getName() = " + dbt.getPrimaryKey().getName());
            System.out.println("dbt.getMyEntityClass().getName() = " + dbt.getMyEntityClass().getName());
            System.out.println("dbt.getColumnSet().size() = " + dbt.getColumnSet().size());
        }
        System.out.println("dbTables = " + dbTables);
     }
}