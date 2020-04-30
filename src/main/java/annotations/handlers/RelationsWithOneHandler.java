package annotations.handlers;

import annotations.ManyToOne;
import annotations.OneToMany;
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
                    handleOneToMany(cl, field);
                } else if (field.isAnnotationPresent(ManyToOne.class)) {
                    handleManyToOne(cl, field);
                }
            }
        }
    }

    private void handleOneToMany(Class<?> clazz, Field field) {
        DBTable dbTable = getDbTableByClass(clazz);

        ForeignKey newForeignKey = generateForeignKeyOneToMany(field, dbTable);
        dbTable.getForeignKeys().add(newForeignKey);
    }

    private void handleManyToOne(Class<?> clazz, Field field) {
        DBTable dbTable = getDbTableByClass(clazz);

        ForeignKey newForeignKey = generateForeignKeyManyToOne(field, dbTable);
        dbTable.getForeignKeys().add(newForeignKey);
    }

    private DBTable getDbTableByClass(Class<?> clazz) {
        return EntityToTableMapper.getTables()
                    .stream()
                    .filter(table -> table.getMyEntityClass() == clazz)
                    .findFirst()
                    .orElseThrow(DataObtainingFailureException::new);
    }

    private ForeignKey generateForeignKeyManyToOne(Field field, DBTable currentTable) {
        DBTable relationTable = getDbTableByClass(field.getType());

        DBColumn myTableKey = getMyTableKeyByReflectedKey(currentTable, relationTable, RelationType.OneToMany);
        DBColumn otherTableKey = relationTable.getPrimaryKey();

        return new ForeignKey(myTableKey, otherTableKey, relationTable, RelationType.ManyToOne, false);
    }

    private ForeignKey generateForeignKeyOneToMany(Field field, DBTable currentTable) {
        DBTable relationTable = getRelationTableForOneToMany(field.getAnnotation(OneToMany.class).mappedBy());

        DBColumn myTableKey = getMyTableKeyByReflectedKey(currentTable, relationTable, RelationType.ManyToOne);
        DBColumn otherTableKey = relationTable.getPrimaryKey();

        return new ForeignKey(myTableKey, otherTableKey, relationTable, RelationType.OneToMany, false);
    }

    /**
     * This method searches ForeignKey's object in the relationTable which links to currentTable
     */
    private DBColumn getMyTableKeyByReflectedKey(DBTable currentTable, DBTable relationTable,
                                                 RelationType relationType) {
        Optional<ForeignKey> keyOptional = relationTable.getForeignKeys()
                .stream()
                .filter(fk -> fk.getOtherTable() == currentTable && fk.getRelationType() == relationType)
                .findFirst();

        return keyOptional.map(ForeignKey::getOtherTableKey).orElseThrow(DataObtainingFailureException::new);
    }

    private DBTable getRelationTableForOneToMany(String mappedBy) {
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

//    private DBTable getRelationTableForManyToOne(Class<?> type) {
//    }

}
