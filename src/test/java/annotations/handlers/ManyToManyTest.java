package annotations.handlers;

import annotations.handlers.configuration.ExtendedEntity;
import exceptions.DBException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ManyToManyTest {

    private Set<DBTable> dbTables;


    @Test
    void standartTestWithTwoMtm(){

        Reflections reflections = new Reflections(ExtendedEntity.class);
        EntityHandler.setReflections(reflections);

       //Set<DBTable> tables = EntityToTableMapper.getTables();
        ManyToManyHandler.createJoinTables();


        Set<DBTable> tables = EntityToTableMapper.getTables();
        DBTable joinTable = new DBTable();
        DBTable extendedEntityTable = new DBTable();
        DBTable simpleEntityTable = new DBTable();

        for (DBTable table:tables) {
            switch(table.getName()) {
                case "test":
                    joinTable = table;

                case "1stTable":
                    extendedEntityTable = table;

                case "2ndTable":
                    simpleEntityTable = table;
            }

        }
        ForeignKey fkFromExt = getForeignKeyFromOneSizeSet(extendedEntityTable.getForeignKeys());
        ForeignKey fkFromSimple = getForeignKeyFromOneSizeSet(simpleEntityTable.getForeignKeys());
        ForeignKey crossKeyForExt = findCrossKey(joinTable.getForeignKeys(), fkFromExt);
        ForeignKey crossKeyForSimple = findCrossKey(joinTable.getForeignKeys(), fkFromSimple);

        assertEquals(fkFromExt.getMyTableKey().getName(), crossKeyForExt.getOtherTableKey().getName());
        assertEquals(fkFromSimple.getMyTableKey().getName(), crossKeyForSimple.getOtherTableKey().getName());



    }
    private ForeignKey getForeignKeyFromOneSizeSet(Set<ForeignKey> set){
        List<ForeignKey> list = new ArrayList<>();
        for(ForeignKey fk : set){
            list.add(fk);
        }
    //    if(list.size() != 1){
    //        throw new IllegalArgumentException();
    //    }
        return list.get(0);

    }
    private ForeignKey findCrossKey(Set<ForeignKey> set, ForeignKey fk){
        for(ForeignKey foreignKey : set){
            if(foreignKey.getOtherTableKey() == fk.getMyTableKey())
             return foreignKey;
        }
        return null;
    }
}
