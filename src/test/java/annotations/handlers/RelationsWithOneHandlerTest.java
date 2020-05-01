package annotations.handlers;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RelationsWithOneHandlerTest {

    @Test
    void handle() {
        //given
        Set<Class<?>> entities = EntityHandler.inspectEntities();
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
            }
        }
    }
}