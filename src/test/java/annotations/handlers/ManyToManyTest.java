package annotations.handlers;

import annotations.handlers.configuration.ExtendedEntity;
import exceptions.DBException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ManyToManyTest {

    private Set<DBTable> dbTables;


    @Test
    void standartTest(){

        Reflections reflections = new Reflections(ExtendedEntity.class);
        EntityHandler.setReflections(reflections);

       Set<DBTable> tables = EntityToTableMapper.getTables();
        for (DBTable table: tables) {
            table.getForeignKeys();
        }

        ManyToManyHandler.createTables();

        tables = EntityToTableMapper.getTables();
        for (DBTable table:tables) {
            System.out.println(table.getName());
        }



    }
}
