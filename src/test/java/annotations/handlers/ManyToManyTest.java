package annotations.handlers;

import annotations.handlers.configuration.ExtendedEntity;
import connections.ConnectionToDB;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import java.sql.SQLException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ManyToManyTest {

    @Test
    void standardTestWithTwoManyToMany() {
        try {
            ConnectionToDB contextInitPoint= ConnectionToDB.getInstance();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        ManyToManyHandler.createJoinTables();

        Set<DBTable> tables = EntityToTableMapper.getTables();
        DBTable joinTable = new DBTable();
        DBTable extendedEntityTable = new DBTable();
        DBTable simpleEntityTable = new DBTable();

        for (DBTable table : tables) {
            switch (table.getName()) {
                case "test":
                    joinTable = table;
                    break;
                case "firstTable":
                    extendedEntityTable = table;
                    break;
                case "secondTable":
                    simpleEntityTable = table;
                    break;
            }
        }
        ForeignKey foreignKeyFromExtendedEntity = getForeignKeyFromOneSizeSet(extendedEntityTable.getForeignKeys());
        ForeignKey foreignKeyFromSimpleEntity = getForeignKeyFromOneSizeSet(simpleEntityTable.getForeignKeys());
        ForeignKey crossForeignKeyForExtendedEntity = findCrossKey(joinTable.getForeignKeys(), foreignKeyFromExtendedEntity);
        ForeignKey crossForeignKeyForSimpleEntity = findCrossKey(joinTable.getForeignKeys(), foreignKeyFromSimpleEntity);

        System.out.println(foreignKeyFromExtendedEntity.getMyTableKey().getName());
        System.out.println(foreignKeyFromSimpleEntity.getMyTableKey().getName());
        System.out.println(crossForeignKeyForExtendedEntity.getMyTableKey().getName());
        System.out.println(crossForeignKeyForSimpleEntity.getMyTableKey().getName());

        assertEquals(foreignKeyFromExtendedEntity.getMyTableKey().getName(),
                crossForeignKeyForExtendedEntity.getOtherTableKey().getName());
        assertEquals(foreignKeyFromSimpleEntity.getMyTableKey().getName(),
                crossForeignKeyForSimpleEntity.getOtherTableKey().getName());
        assertEquals(crossForeignKeyForExtendedEntity.getMyTableKey().getName(), "firstId");
        assertEquals(crossForeignKeyForSimpleEntity.getMyTableKey().getName(), "secondId");

    }

    private ForeignKey getForeignKeyFromOneSizeSet(Set<ForeignKey> set) {
        for (ForeignKey fk : set) {
            return fk;
        }
        return null;
    }

    private ForeignKey findCrossKey(Set<ForeignKey> set, ForeignKey fk) {
        for (ForeignKey foreignKey : set) {
            if (foreignKey.getOtherTableKey() == fk.getMyTableKey())
                return foreignKey;
        }
        return null;
    }
}
