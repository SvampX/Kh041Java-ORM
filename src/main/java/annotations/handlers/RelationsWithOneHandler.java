package annotations.handlers;

import annotations.Entity;
import annotations.ManyToOne;
import annotations.OneToMany;
import annotations.Table;
import exceptions.DataObtainingFailureException;
import exceptions.Messages;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;

public class RelationsWithOneHandler {

    public void handle(Set<Class<?>> entitiesSet) {
        for (Class<?> cl : entitiesSet) {
            for (Field field : cl.getDeclaredFields()) {
                if (field.isAnnotationPresent(OneToMany.class)) {
                    handleOneToMany(cl, field);}
            }
        }
    }
    
    private void handleOneToMany(Class<?> clazz, Field field) {
        String dbName = clazz.getAnnotation(Table.class).name();
        if (dbName.isEmpty()) {
            dbName = clazz.getAnnotation(Entity.class).name();
        }
        
        DBTable dbTable = getTable(dbName);
        ForeignKey newForeignKey = generateForeignKey(field, dbTable);
        dbTable.getForeignKeys().add(newForeignKey);
    }

    private ForeignKey generateForeignKey(Field field, DBTable currentTable) {
        DBTable relationTable = getRelationTable(field.getAnnotation(OneToMany.class).mappedBy());

        DBColumn myTableKey = getMyTableKeyByReflectedKey(currentTable, relationTable);
        DBColumn otherTableKey = relationTable.getPrimaryKey();

        return new ForeignKey(myTableKey, otherTableKey, relationTable, RelationType.OneToMany, false);
    }

    private DBColumn getMyTableKeyByReflectedKey(DBTable currentTable, DBTable relationTable) {

        Optional<ForeignKey> keyOptional = relationTable.getForeignKeys()
                .stream()
                .filter(fk -> fk.getOtherTable() == currentTable && fk.getRelationType() == RelationType.ManyToOne)
                .findFirst();

        return keyOptional.map(ForeignKey::getOtherTableKey).orElseThrow(DataObtainingFailureException::new);
    }

    private DBTable getRelationTable(String mappedBy) {
        DBTable returnTable = null;
        Set<DBTable> dbTables = EntityToTableMapper.getTables();
        for (DBTable dbTable : dbTables) {
            for (Field field : dbTable.getMyEntityClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(ManyToOne.class) && field.getName().equalsIgnoreCase(mappedBy)) {
                    returnTable = dbTable;
                }
            }
        }

        if (returnTable == null) {
            throw new DataObtainingFailureException(Messages.ERR_CANNOT_OBTAIN_DBTABLE);
        }
        return returnTable;
    }

    private DBTable getTable(String dbName) {
        Set<DBTable> dbTables = EntityToTableMapper.getTables();

        return dbTables.stream()
                .filter(dbTable -> dbTable.getName().equalsIgnoreCase(dbName))
                .findFirst()
                .orElseThrow(DataObtainingFailureException::new);
    }

}
