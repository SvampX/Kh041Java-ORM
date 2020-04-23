package annotations.handlers;

import annotations.Column;
import annotations.Entity;
import annotations.Id;
import annotations.Table;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class EntityHandler {
    private static final Reflections reflections = new Reflections("EntityHandler");

    static Set<Class<?>> inspectEntities() {
        Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class, true);
        return entities;
    }

    //Could be used in CRUD-services
    private static Set<Field> getAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annotation) {
        Set<Field> annotatedFields = Arrays.stream(clazz.getDeclaredFields()).
                filter(field -> field.isAnnotationPresent(annotation)).
                peek(field -> field.setAccessible(true)).
                collect(Collectors.toSet());
        return annotatedFields;
    }

    //Could be used in CRUD-services
    private static Set<Method> getAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotation) {
        Set<Method> annotatedMethods = Arrays.stream(clazz.getDeclaredMethods()).
                filter(method -> method.isAnnotationPresent(annotation)).
                peek(method -> method.setAccessible(true)).
                collect(Collectors.toSet());
        return annotatedMethods;
    }

    static Map<String, Class<?>> getEntitiesWithTableNames(Set<Class<?>> entities) {
        Map<String, Class<?>> entityTableRelation = new HashMap<>();
        for (Class<?> clazz : entities) {
            if (clazz.isAnnotationPresent(Table.class)) {
                entityTableRelation.put(clazz.getAnnotation(Table.class).tableName(), clazz);
            } else {
                entityTableRelation.put(clazz.getAnnotation(Entity.class).name(), clazz);
            }
        }
        return entityTableRelation;
    }

    //Could be used in CRUD-services
    static Set<DBColumn> getColumns(Class<?> entity) {
        Set<DBColumn> dbColumns = new HashSet<>();
        if(entity == null) {
            return dbColumns;
        }
        Set<Field> fields = getAnnotatedFields(entity, Column.class);
        fields.addAll(getAnnotatedFields(entity, Id.class));
        Set<Method> methods = getAnnotatedMethods(entity, Column.class);
        methods.addAll(getAnnotatedMethods(entity, Id.class));
        dbColumns = collectDBColumnsFromFields(fields);
        dbColumns.addAll(collectDBColumnsFromMethods(methods));
        return dbColumns;
    }

    private static Set<DBColumn> collectDBColumnsFromFields(Set<Field> fieldSet){
        Set<DBColumn> dbColumns = new HashSet<>();
        for (Field f:fieldSet) {
            DBColumn dbColumn = new DBColumn();
            //Could be changed on ternary operator
            if (f.isAnnotationPresent(Column.class)){
                dbColumn.setName(f.getAnnotation(Column.class).columnName());
            } else {
                dbColumn.setName(f.getAnnotation(Id.class).name());
            }
            dbColumn.setField(f);
            dbColumns.add(dbColumn);
        }
        return dbColumns;
    }

    private static Set<DBColumn> collectDBColumnsFromMethods(Set<Method> methodSet){
        Set<DBColumn> dbColumns = new HashSet<>();
        for (Method m: methodSet) {
            DBColumn dbColumn = new DBColumn();
            //Could be changed on ternary operator
            if (m.isAnnotationPresent(Column.class)){
                dbColumn.setName(m.getAnnotation(Column.class).columnName());
            } else {
                dbColumn.setName(m.getAnnotation(Id.class).name());
            }
            dbColumn.setMethod(m);
            dbColumns.add(dbColumn);
        }
        return dbColumns;
    }

}
