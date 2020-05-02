package annotations.handlers;

import annotations.handlers.configuration.ExtendedEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RelationsWithOneHandlerTest {
    Reflections reflections;
    Set<Class<?>> entities;

    @BeforeEach
    void setUp() {
        reflections = new Reflections(ExtendedEntity.class);
        EntityHandler.setReflections(reflections);
        entities = EntityHandler.inspectEntities();
    }

    @Test
    void handle() {
        //given
        Set<DBTable> tables = EntityToTableMapper.getTables();
        RelationsWithOneHandler relationsWithOneHandler = new RelationsWithOneHandler();
        //when
        relationsWithOneHandler.handle(entities);

        //then
        for (DBTable dbTable : tables) {
            assertNotNull(dbTable.getForeignKeys());
            for (ForeignKey foreignKey : dbTable.getForeignKeys()) {
                assertNotNull(foreignKey.getMyTableKey());
                assertNotNull(foreignKey.getOtherTableKey());
                assertNotNull(foreignKey.getOtherTable());
                assertNotNull(foreignKey.getRelationType());
                System.out.println(foreignKey.toString());
            }
        }
    }
}