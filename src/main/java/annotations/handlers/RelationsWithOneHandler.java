package annotations.handlers;

import annotations.JoinColumn;
import annotations.ManyToOne;
import annotations.OneToMany;
import annotations.OneToOne;
import exceptions.DataObtainingFailureException;
import exceptions.Messages;

import java.lang.reflect.Field;
import java.util.Set;

public class RelationsWithOneHandler {

    public static final Class<OneToMany> ONE_TO_MANY_CLASS = OneToMany.class;
    public static final Class<ManyToOne> MANY_TO_ONE_CLASS = ManyToOne.class;
    public static final Class<OneToOne> ONE_TO_ONE_CLASS = OneToOne.class;

    public void handle(Set<Class<?>> entitiesSet) {
        for (Class<?> cl : entitiesSet) {
            for (Field field : cl.getDeclaredFields()) {
                if (field.isAnnotationPresent(ONE_TO_MANY_CLASS)) {
                    handleRelation(cl, field, RelationType.OneToMany);
                } else if (field.isAnnotationPresent(MANY_TO_ONE_CLASS)) {
                    handleRelation(cl, field, RelationType.ManyToOne);
                } else if (field.isAnnotationPresent(ONE_TO_ONE_CLASS)) {
                    handleRelation(cl, field, RelationType.OneToOne);
                }
            }
        }
    }

    private void handleRelation(Class<?> clazz, Field field, RelationType relationType) {
        DBTable dbTable = getDbTableByClass(clazz);

        ForeignKey newForeignKey = generateForeignKey(field, dbTable, relationType);
        dbTable.getForeignKeys().add(newForeignKey);
    }

    private DBTable getDbTableByClass(Class<?> clazz) {
        return EntityToTableMapper.getTables()
                    .stream()
                    .filter(table -> table.getMyEntityClass() == clazz)
                    .findFirst()
                    .orElseThrow(DataObtainingFailureException::new);
    }

    private ForeignKey generateForeignKey(Field field, DBTable currentTable, RelationType relationType) {
        DBTable relationTable = null;
        if (relationType == RelationType.ManyToOne || relationType == RelationType.OneToOne) {
            relationTable = getDbTableByClass(field.getType());
        } else if (relationType == RelationType.OneToMany) {
            relationTable = getRelationTableForOneToMany(field.getAnnotation(ONE_TO_MANY_CLASS).mappedBy());
        }

        DBColumn myTableKey = currentTable.getPrimaryKey();
        assert relationTable != null;
        DBColumn otherTableKey = relationTable.getPrimaryKey();

        if (field.isAnnotationPresent(JoinColumn.class)) {
            setJoinColumnToDBTable(field, currentTable, relationTable);
        }

        return new ForeignKey(myTableKey, otherTableKey, relationTable, relationType, false);
    }

    private DBTable getRelationTableForOneToMany(String mappedBy) {
        DBTable returnTable = null;
        Set<DBTable> dbTables = EntityToTableMapper.getTables();
        for (DBTable dbTable : dbTables) {
            for (Field field : dbTable.getMyEntityClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(MANY_TO_ONE_CLASS) && field.getName().equalsIgnoreCase(mappedBy)) {
                    returnTable = dbTable;
                }
            }
        }

        if (returnTable == null) {
            throw new DataObtainingFailureException(Messages.ERR_CANNOT_OBTAIN_DBTABLE);
        }
        return returnTable;
    }

    private void setJoinColumnToDBTable(Field field, DBTable currentTable, DBTable relationTable) {
        DBColumn joinDBColumn = new DBColumn();
        String columnName = getJoinColumnName(field, relationTable);
        joinDBColumn.setField(relationTable.getPrimaryKey().getField());
        joinDBColumn.setName(columnName);
        joinDBColumn.setType(EntityToTableMapper.getColumnType(field));
        currentTable.setJoinColumn(joinDBColumn);
    }

    private String getJoinColumnName(Field field, DBTable relationTable) {
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        return !joinColumn.name().isEmpty() ? joinColumn.name() : relationTable.getName() + "_id";
    }

}
