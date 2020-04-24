package annotations.handlers;

import annotations.Column;
import annotations.Entity;
import annotations.Id;
import annotations.Table;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



public class EntityToTableParser {
    static{
        EntityHandler.inspectEntities();
    }
    public static Set<Class<?>> entitiesSet = EntityHandler.getEntitiesSet();

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

    private static Set<Method> getAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotation) {
        Set<Method> annotatedMethods = new HashSet<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                method.setAccessible(true);
                annotatedMethods.add(method);
            }
        }
        return annotatedMethods;
    }

    public static Set<DBTable> parse() {
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
        Set<Method> methods = getAnnotatedMethods(entity, Column.class);
        dbColumns = collectDBColumnsFromFields(fields);
        dbColumns.addAll(collectDBColumnsFromMethods(methods));
        removeDuplicates(dbColumns);
        return dbColumns;
    }

    private static DBColumn getPrimaryKey(Class<?> entity) {
        DBColumn pk = new DBColumn();
        Set<Field> primaryFields = getAnnotatedFields(entity, Id.class);
        Set<Method> primaryMethods = getAnnotatedMethods(entity, Id.class);
        if (primaryFields.size() > 1 || primaryMethods.size() > 1)
            throw new IllegalStateException("In Entity couldn't be more than 1 Primary Key");
        if (primaryFields.size() == 0 && primaryMethods.size() == 0)
            throw new IllegalStateException("In Entity Primary Key couldn't be absent");
        if (primaryFields.size() == primaryMethods.size()) {
            if (primaryFields.iterator().next().getName().equals(primaryMethods.iterator().next().getName())) {
                pk.setField(primaryFields.iterator().next());
                pk.setName(primaryFields.iterator().next().getAnnotation(Id.class).name());
                return pk;
            }
            throw new IllegalStateException("In Entity couldn't be more than 1 Primary Key");
        }
        if (primaryFields.size() == 0) {
            pk.setMethod(primaryMethods.iterator().next());
            pk.setName(primaryMethods.iterator().next().getAnnotation(Id.class).name());
            return pk;
        }
        pk.setField(primaryFields.iterator().next());
        return pk;
    }

    //Removes all duplicated columns(leaves field-based) if both Field and Setter are annotated with @Column or @ Id
    private static void removeDuplicates(Set<DBColumn> extendedSet) {
        int n = extendedSet.size();
        DBColumn tmp;
        for (int i = 0; i < n; i++) {
            tmp = extendedSet.iterator().next();
            for (DBColumn dbc : extendedSet) {
                if (!dbc.equals(tmp)) {
                    if (dbc.getName().equals(tmp.getName())) {
                        if (dbc.getField() == null) {
                            extendedSet.remove(dbc);
                        } else {
                            extendedSet.remove(tmp);
                        }
                        n--;
                        break;
                    }
                }
            }
        }
    }

    private static Set<DBColumn> collectDBColumnsFromFields(Set<Field> fieldSet) {
        Set<DBColumn> dbColumns = new HashSet<>();
        for (Field f : fieldSet) {
            DBColumn dbColumn = new DBColumn();
            if (f.isAnnotationPresent(Column.class)) {
                dbColumn.setName(f.getAnnotation(Column.class).name());
            } else {
                dbColumn.setName(f.getAnnotation(Id.class).name());
            }
            dbColumn.setField(f);
            dbColumns.add(dbColumn);
        }
        return dbColumns;
    }

    private static Set<DBColumn> collectDBColumnsFromMethods(Set<Method> methodSet) {
        Set<DBColumn> dbColumns = new HashSet<>();
        for (Method m : methodSet) {
            DBColumn dbColumn = new DBColumn();
            if (m.isAnnotationPresent(Column.class)) {
                dbColumn.setName(m.getAnnotation(Column.class).name());
            } else {
                dbColumn.setName(m.getAnnotation(Id.class).name());
            }
            dbColumn.setMethod(m);
            dbColumns.add(dbColumn);
        }
        return dbColumns;
    }
}
