package annotations.handlers;

import annotations.Column;
import annotations.Entity;
import annotations.Id;
import annotations.Table;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class EntityToTableMapper {
    //TODO move to session factory
    static {
        EntityHandler.inspectEntities();
    }
    public static Set<Class<?>> entitiesSet = EntityHandler.getEntitiesSet();
    private static Set<DBTable> tables;
    private static Set<Field> getAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annotation) {
        Set<Field> annotatedFields = new HashSet<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(annotation)) {
                field.setAccessible(true);
                annotatedFields.add(field);
            }
        }
        return annotatedFields;
    }

    public static Set<DBTable> getTables() {
        if(tables == null){
            tables = mapTablesFromEntities();
        }
        return tables;
    }

    private static Set<DBTable> mapTablesFromEntities() {
        Set<DBTable> tables = new HashSet<>();
        Map<Class<?>, String> entitiesWithTableName = getEntitiesWithTableNames(entitiesSet);
        for (Class<?> clazz : entitiesSet) {
            DBTable table = new DBTable();
            table.setName(entitiesWithTableName.get(clazz));
            table.setPrimaryKey(getPrimaryKey(clazz));
            table.setColumnSet(getColumns(clazz));
            table.setMyEntityClass(clazz);
            tables.add(table);
        }
        return tables;
    }

    private static Map<Class<?>, String> getEntitiesWithTableNames(Set<Class<?>> entities) {
        Map<Class<?>, String> entityTableRelation = new HashMap<>();
        for (Class<?> clazz : entities) {
            if (clazz.isAnnotationPresent(Table.class)) {
                entityTableRelation.put(clazz, clazz.getAnnotation(Table.class).name());
            } else {
                entityTableRelation.put(clazz, clazz.getAnnotation(Entity.class).name());
            }
        }
        return entityTableRelation;
    }

    private static Set<DBColumn> getColumns(Class<?> entity) {
        Set<DBColumn> dbColumns;
        Set<Field> fields = getAnnotatedFields(entity, Column.class);
        dbColumns = collectDBColumnsFromFields(fields);
        return dbColumns;
    }

    private static DBColumn getPrimaryKey(Class<?> entity) {
        DBColumn pk = new DBColumn();
        Set<Field> primaryFields = getAnnotatedFields(entity, Id.class);
        if (primaryFields.size() > 1)
            throw new IllegalStateException("In Entity couldn't be more than 1 Primary Key");
        pk.setField(primaryFields.iterator().next());
        pk.setName(primaryFields.iterator().next().getAnnotation(Column.class).name());
        return pk;
    }

    private static Set<DBColumn> collectDBColumnsFromFields(Set<Field> fieldSet) {
        Set<DBColumn> dbColumns = new HashSet<>();
        for (Field f : fieldSet) {
            DBColumn dbColumn = new DBColumn();
            if (f.isAnnotationPresent(Column.class)) {
                dbColumn.setName(f.getAnnotation(Column.class).name());
            }
            dbColumn.setField(f);
            dbColumn.setType(getColumnType(f));
            dbColumns.add(dbColumn);
        }
        return dbColumns;
    }

    protected static Type getColumnType(Field field) {
        String fieldType = field.getType().getSimpleName();
        switch (fieldType.toLowerCase()) {
            case "string":
                return Type.STRING;
            case "integer":
            case "int" :
                return Type.INTEGER;
            case "short" :
                return Type.SHORT;
            case "float" :
                return Type.FLOAT;
            case  "double" :
                return Type.DOUBLE;
            case "bigdecimal" :
                return Type.BIGDECIMAL;
            case "char":
            case "character" :
                return Type.CHARACTER;
            case "boolean" :
                return Type.BOOLEAN;
            default:
                return Type.OTHER;
        }
    }
}
