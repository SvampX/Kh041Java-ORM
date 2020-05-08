package crud;

import annotations.*;
import annotations.handlers.*;
import connections.ConnectionToDB;
import exceptions.DataObtainingFailureException;
import exceptions.Messages;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class RelationsLoader {
    Map<ManyToMany, ManyToMany> manyToManyLinkMap = ManyToManyHandler.getFirstManyToManyLinkMap();
    Map<ManyToMany, ManyToMany> crossManyToManyLinkMap = ManyToManyHandler.getSecondManyToManyLinkMap();
    Map<ManyToMany, Field> manyToManyFieldMap = ManyToManyHandler.getManyToManyFieldMap();
    private Connection connection;
    private Set<DBTable> tables;
    private CrudServices crudServices;
    private int numberGenerator = 0;

    public RelationsLoader() {
        init();
    }

    private void init() {
        try {
            ConnectionToDB connectionToDB = ConnectionToDB.getInstance();
            connection = connectionToDB.getConnection();
            tables = EntityToTableMapper.getTables();
            crudServices = new CrudServices();
            crudServices.setConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean create(Object entity) {
        Class<?> clazz = entity.getClass();
        isEntityCheck(clazz);
        DBTable table = getTableByClass(clazz);
        Map<ForeignKey, Field> lazyFields = getLazyFields(table);
        if (lazyFields.size() == 0 && table.getForeignKeys().size() == 0) {
            return crudServices.create(entity);
        }
        if (lazyFields.size() == 0) {
            return createOneToOne(entity);
        }
        return createWithMany(entity, lazyFields);
    }

    private boolean createWithMany(Object entity, Map<ForeignKey, Field> lazyFields) {

        initRelationsWithMany(entity);
        return true;
    }

    private void initRelationsWithMany(Object entity) {
        Class<?> clazz = entity.getClass();
        DBTable table = getTableByClass(clazz);
        Map<ForeignKey, Field> lazyFieldsMap = getLazyFields(table);
        Set<Field> lazyField = new HashSet<>();
        for (Map.Entry<ForeignKey, Field> entry : lazyFieldsMap.entrySet()) {
            lazyField.add(entry.getValue());
        }
        Set<Object> lazyObjects = new HashSet<>();
        for (Field field : lazyField) {
            if (getObjectByField(field, entity) != null) {
                lazyObjects.add(getObjectByField(field, entity));
            }
        }
        System.out.println("lazyField = " + lazyObjects);
        for (Field f : lazyField) {
            if(f.isAnnotationPresent(ManyToMany.class)){
                createManyToMany(entity, f);
            }
        }
    }

    private void createManyToMany(Object entity, Field myCollection) {
        Field otherEntityCollection = getCrossField(myCollection);
        fillLeftManyToMany(entity, myCollection, otherEntityCollection);

    }

    private void fillLeftManyToMany(Object entity, Field myCollection, Field otherEntityCollection) {
        Class<?> clazz = entity.getClass();
        DBTable tableByClass = getTableByClass(clazz);
        Object id = createEntityGetId(tableByClass, entity);
    }

    private Object createEntityGetId(DBTable table, Object entity) {
        List<DBColumn> columnsOrder = new ArrayList<>();
        String createQuery = buildCreateQueryWithReturningId(table, columnsOrder);
        Object id = null;
        try {
            PreparedStatement statement = connection.prepareStatement(createQuery);
            setStatementValues(statement, columnsOrder, entity);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            id = resultSet.getObject(1);
        } catch (SQLException | IllegalAccessException throwables) {
            throwables.printStackTrace();
        }
        return id;
    }

    private Field getCrossField(Field myCollection) {
        ManyToMany manyToMany = myCollection.getAnnotation(ManyToMany.class);
        ManyToMany crossMtm;
        if(crossManyToManyLinkMap.containsKey(manyToMany)){
            crossMtm = crossManyToManyLinkMap.get(manyToMany);
        } else {
            crossMtm = manyToManyLinkMap.get(manyToMany);
        }
        return manyToManyFieldMap.get(crossMtm);
    }

    private boolean createOneToOne(Object entity) {
        Map<Class<?>, Object> classesWithId = new HashMap<>();
        String updateQuery = "";
        Object id = initOneToOneRelations(entity, classesWithId, updateQuery, new Object());

        return true;
    }

    //couldn`t be refactored, i tried)
    private Object initOneToOneRelations(Object entity, Map<Class<?>, Object> classesWithId, String updateQuery, Object myId) {
        Class<?> clazz = entity.getClass();
        DBTable table = getTableByClass(clazz);
        Field[] declaredFields = entity.getClass().getDeclaredFields();
        Set<Field> oneToOneFields = new HashSet<>();
        Set<Field> joinColumn = new HashSet<>();
        getAnnotatedFields(joinColumn, declaredFields, JoinColumn.class);
        findOneToOneFields(table, declaredFields, oneToOneFields);
        Object joinId;
        Object joinObject = null;
        if (joinColumn.size() > 0) {
            joinObject = getObjectByField(joinColumn.iterator().next(), entity);
        }
        if (joinObject != null) {
            if (!classesWithId.containsKey(clazz)) {
                createEntityGetId(table, classesWithId, entity);
                myId = classesWithId.get(clazz);
                updateQuery = buildUpdateWithJoinColumnQuery(table, myId);
            }
            if (!classesWithId.containsKey(joinObject.getClass())) {
                createEntityGetId(getTableByClass(joinObject.getClass()), classesWithId, joinObject);
            }
            joinId = classesWithId.get(joinObject.getClass());
            updateEntityWithJoinColumn(updateQuery, joinId);
        }
        for (Field field : oneToOneFields) {
            Object oneToOneObject = getObjectByField(field, entity);
            if (oneToOneObject != null) {
                if (!classesWithId.containsKey(clazz)) {
                    createEntityGetId(table, classesWithId, entity);
                    initOneToOneRelations(oneToOneObject, classesWithId, updateQuery, myId);
                }
            }
        }
        try {
            myId = classesWithId.get(clazz);
            table.getPrimaryKey().getField().set(entity, myId);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return myId;
    }

    private void initRemainingRelations(Object entity, Map<Class<?>, Object> classesWithId, Class<?> clazz, Set<Field> oneToOneFields) {

    }

    private void updateEntityWithJoinColumn(String updateQuery, Object joinId) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
            preparedStatement.setObject(1, joinId);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Object getObjectByField(Field field, Object entity) {
        Object object = null;
        field.setAccessible(true);
        try {
            object = field.get(entity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return object;
    }


    private void createEntityGetId(DBTable table, Map<Class<?>, Object> classIdLinks, Object entity) {
        List<DBColumn> columnsOrder = new ArrayList<>();
        String createQuery = buildCreateQueryWithReturningId(table, columnsOrder);
        try {
            PreparedStatement statement = connection.prepareStatement(createQuery);
            setStatementValues(statement, columnsOrder, entity);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            Object id = resultSet.getObject(1);
            classIdLinks.put(table.getMyEntityClass(), id);
        } catch (SQLException | IllegalAccessException throwables) {
            throwables.printStackTrace();
        }
    }

    protected void setStatementValues(PreparedStatement preparedStatement, List<DBColumn> columnsOrder, Object entity) throws IllegalAccessException, SQLException {
        for (int i = 0; i < columnsOrder.size(); i++) {
            DBColumn dbc = columnsOrder.get(i);
            Field field = dbc.getField();
            Object value = field.get(entity);
            preparedStatement.setObject(i + 1, value);
        }
    }

    private String buildUpdateWithJoinColumnQuery(DBTable table, Object id) {
        StringBuilder updateJoinColumnQuery = new StringBuilder();
        updateJoinColumnQuery.append("UPDATE ").
                append(table.getName()).
                append("\n SET ").
                append(table.getJoinColumn().getName()).
                append(" = ? ").
                append("\n WHERE ").
                append(table.getPrimaryKey().getName()).
                append(" = ").
                append(id).
                append(" ;");
        return updateJoinColumnQuery.toString();
    }

    private DBColumn findOneToOneFields(DBTable table, Field[] declaredFields, Set<Field> oneToOneFields) {
        DBColumn joinColumn = null;
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(OneToOne.class)) {
                oneToOneFields.add(field);
                if (field.isAnnotationPresent(JoinColumn.class)) {
                    joinColumn = table.getJoinColumn();
                }
            }
        }
        return joinColumn;
    }

    protected String buildCreateQueryWithReturningId(DBTable dbTable, List<DBColumn> columnsOrder) {
        StringBuilder insertQuery = new StringBuilder();
        String insName = "ins" + getNumber(false);
        insertQuery.append("WITH ").
                append(insName).
                append(" AS (").
                append(" INSERT INTO ").
                append(dbTable.getName()).append("(").
                append(prepareColumnsForCreateQuery(dbTable, columnsOrder)).
                append(") VALUES(").
                append("?,".repeat(columnsOrder.size())).
                delete(insertQuery.length() - 1, insertQuery.length()).
                append(")").
                append("\n RETURNING ").
                append(dbTable.getPrimaryKey().getName()).
                append(" ) ").append("SELECT ").
                append(dbTable.getPrimaryKey().getName()).
                append(" FROM ").
                append(insName);
        return insertQuery.toString();
    }

    protected StringBuilder prepareColumnsForCreateQuery(DBTable dbTable, List<DBColumn> columnsOrder) {
        StringBuilder columnNames = new StringBuilder();
        for (DBColumn dbc : dbTable.getColumnSet()) {
            columnNames.append(dbc.getName()).append(", ");
            columnsOrder.add(dbc);
        }
        columnNames.delete(columnNames.length() - 2, columnNames.length());
        return columnNames;
    }


    private Map<ForeignKey, Field> getLazyFields(DBTable dbTable) {
        Map<ForeignKey, Field> lazyFields = new HashMap<>();
        Set<ForeignKey> foreignKeys = dbTable.getForeignKeys();
        for (ForeignKey fk : foreignKeys) {
            if (fk.getRelationType() != RelationType.OneToOne) {
                Field lazyField = getRelationsField(fk, dbTable.getMyEntityClass());
                lazyFields.put(fk, lazyField);
            }
        }
        return lazyFields;
    }

    public Field getRelationsField(ForeignKey fk, Class<?> myEntityClass) {
        Set<Field> fieldSet = new HashSet<>();
        Field[] declaredFields = myEntityClass.getDeclaredFields();
        RelationType relationType = fk.getRelationType();
        Class<? extends Annotation> annotation = getAnnotationByRelationType(relationType);
        getAnnotatedFields(fieldSet, declaredFields, annotation);
        return findRelationField(fk, fieldSet);
    }

    private void getAnnotatedFields(Set<Field> fieldSet, Field[] declaredFields, Class<? extends Annotation> annotation) {
        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(annotation)) {
                fieldSet.add(declaredField);
            }
        }
    }

    private Field findRelationField(ForeignKey fk, Set<Field> fieldSet) {
        Field field = null;
        if (fk.getRelationType() == RelationType.ManyToOne) {
            field = findManyToOneField(fk, fieldSet);
            return field;
        }
        if (fk.getRelationType() == RelationType.OneToMany) {
            field = findOneToManyField(fk, fieldSet);
            return field;
        }
        if (fk.getRelationType() == RelationType.ManyToMany) {
            field = findManyToManyField(fk, fieldSet);
            return field;
        }

        return field;
    }

    private Field findManyToManyField(ForeignKey fk, Set<Field> fieldSet) {
        DBTable relationsTable = fk.getOtherTable();
        String otherTableName = relationsTable.getName();
        Set<ForeignKey> relationsTableKeys = relationsTable.getForeignKeys();
        ManyToMany annotation;
        for (Field field : fieldSet) {
            annotation = field.getAnnotation(ManyToMany.class);
            if (annotation.tableName().equals(otherTableName)) {
                ManyToMany relationsManyToMany;
                if (manyToManyLinkMap.containsKey(annotation)) {
                    relationsManyToMany = manyToManyLinkMap.get(annotation);
                } else {
                    relationsManyToMany = crossManyToManyLinkMap.get(annotation);
                }
                if (manyToManyFieldMap.containsKey(relationsManyToMany)) {
                    return manyToManyFieldMap.get(annotation);
                }
                throw new IllegalStateException("Can not find @ManyToMane related with");
            }
        }
        return null;
    }

    private Field findOneToManyField(ForeignKey fk, Set<Field> fieldSet) {
        OneToMany annotation = null;
        String myEntityRelationsFieldName;
        Set<Field> otherTableFieldSet = new HashSet<>();
        for (Field field : fieldSet) {
            annotation = field.getAnnotation(OneToMany.class);
            myEntityRelationsFieldName = annotation.mappedBy();
            Field[] otherEntityDeclaredFields = fk.getOtherTable().getMyEntityClass().getDeclaredFields();
            getAnnotatedFields(otherTableFieldSet, otherEntityDeclaredFields, ManyToOne.class);
            for (Field fld : otherTableFieldSet) {
                if (fld.getName().equals(myEntityRelationsFieldName)) {

                    isCollectionCheck(field, field.getDeclaringClass());
                    return field;
                }
            }
        }
        throw new IllegalStateException("Can not find @ManyToOne annotated field with name from @OneToMany(mappedBy)");
    }

    private Field findManyToOneField(ForeignKey fk, Set<Field> fieldSet) {
        Class<?> otherEntityClass = fk.getOtherTable().getMyEntityClass();
        for (Field field : fieldSet) {
            if (field.getClass().equals(otherEntityClass)) {
                return field;
            }
        }
        return null;
    }


    private Class<? extends Annotation> getAnnotationByRelationType(RelationType relationType) {
        switch (relationType) {
            case OneToOne:
                return OneToOne.class;
            case OneToMany:
                return OneToMany.class;
            case ManyToOne:
                return ManyToOne.class;
            case ManyToMany:
                return ManyToMany.class;
            default:
                throw new IllegalStateException("Can not find relation field marked with @OneToMany or @ManyToOne");
        }
    }

    public DBTable getTableByClass(Class<?> clazz) {
        for (DBTable dbt : tables) {
            if (dbt.getMyEntityClass().equals(clazz)) {
                return dbt;
            }
        }
        return null;
    }

    private int getNumber(boolean reset) {
        if (reset) {
            numberGenerator = 0;
        }
        numberGenerator++;
        return numberGenerator;
    }

    //TODO Checks section


    private void isCollectionCheck(Field field, Class<?> entityClass) {
        try {
            Object obj = entityClass.getDeclaredConstructor().newInstance();
            field.setAccessible(true);
            if (field.get(obj) instanceof Collection) {
                throw new IllegalArgumentException("@OneTOMany annotated field should be instance of Collection");
            }
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    protected void hasPrimaryKeyCheck(DBTable table) {
        boolean hasPrimaryKey = table.getPrimaryKey() != null;
        if (!hasPrimaryKey) {
            throw new IllegalStateException("For this operation Entity should have @Id annotated field");
        }
    }

    protected void hasCorrectIdTypeCheck(Object id) {
        if (!(id instanceof Integer) && !(id instanceof Long)) {
            throw new IllegalArgumentException("Primary key must be initialized and Integer or Long type");
        }
    }

    protected boolean hasRelationsCheck(Class<?> clazz) {
        DBTable dbTable = getTableByClass(clazz);
        if (dbTable.getForeignKeys().size() > 0) {
            return true;
        }
        return false;
    }

    protected void isEntityCheck(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new DataObtainingFailureException("Current class: " + clazz + Messages.ERR_CANNOT_OBTAIN_ENTITY_CLASS);
        }
        DBTable dbTable = getTableByClass(clazz);
        if (dbTable.getPrimaryKey() == null && dbTable.getColumnSet() == null) {
            throw new IllegalStateException("Entity must have at least one @Column or @Id field");
        }
    }
}
