package annotations.handlers;

import annotations.JoinColumn;
import annotations.ManyToOne;
import annotations.OneToOne;
import annotations.handlers.configuration.ExtendedEntity;
import exceptions.DataObtainingFailureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import java.lang.reflect.Field;
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

    @Test
    void handleSetJoinColumn() {
        //given
        Set<DBTable> tables = EntityToTableMapper.getTables();
        RelationsWithOneHandler relationsWithOneHandler = new RelationsWithOneHandler();
        //when
        relationsWithOneHandler.handle(entities);

        //then
        for (DBTable dbTable : tables) {
            Field [] dbEntitiesFields = dbTable.getMyEntityClass().getDeclaredFields();
            for (Field field : dbEntitiesFields) {
                if (field.isAnnotationPresent(ManyToOne.class) ||
                        (field.isAnnotationPresent(OneToOne.class) && field.isAnnotationPresent(JoinColumn.class))) {
                    assertNotNull(dbTable.getJoinColumn());
                    System.out.println("table: " + dbTable.getName() + " joinColumn name: " +
                            dbTable.getJoinColumn().getName());
                    DBTable relationTable = getDbTableByClass(field.getType());
                    assertSame(dbTable.getJoinColumn().getField(), relationTable.getPrimaryKey().getField());
                }
            }
        }
    }

    private DBTable getDbTableByClass(Class<?> clazz) {
        return EntityToTableMapper.getTables()
                .stream()
                .filter(table -> table.getMyEntityClass() == clazz)
                .findFirst()
                .orElseThrow(DataObtainingFailureException::new);
    }
}