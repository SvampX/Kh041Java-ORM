package crud;


import annotations.Entity;
import annotations.handlers.DBColumn;
import annotations.handlers.DBTable;
import annotations.handlers.EntityToTableMapper;
import annotations.handlers.ForeignKey;
import annotations.handlers.GeneratedValueHandler;
import annotations.handlers.ManyToManyHandler;
import annotations.handlers.RelationType;
import annotations.handlers.Type;
import connections.ConnectionToDB;
import exceptions.DataObtainingFailureException;
import exceptions.Messages;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CrudServices {
    Set<DBTable> tables = EntityToTableMapper.getTables();
    Connection connection;

    //TODO Tables definition section

    private String getTablesDefineQuery() {
        StringBuilder tablesDefineQuery = new StringBuilder();
        for (DBTable dbc : tables) {
            tablesDefineQuery.append(prepareTableQuery(dbc));
        }
        return tablesDefineQuery.toString();
    }

    private StringBuilder prepareTableQuery(DBTable dbTable) {
        boolean hasPrimaryKey = dbTable.getPrimaryKey() != null;
        GeneratedValueHandler generatedValueHandler = new GeneratedValueHandler();
        StringBuilder singleTableQuery = new StringBuilder();
        //TODO checking on "Drop if exists parameter"
        singleTableQuery.append("CREATE TABLE ").
                append(dbTable.getName()).
                append(" (\u0020");
        if (hasPrimaryKey) {
            try {
                singleTableQuery.append(generatedValueHandler.createIdGenerator(dbTable))
                        .append(",\n");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        singleTableQuery.append(getColumnsDefinition(dbTable));
        return singleTableQuery;
    }

    private StringBuilder getColumnsDefinition(DBTable dbTable) {
        StringBuilder columnsDefinition = new StringBuilder();
        for (DBColumn dbc : dbTable.getColumnSet()) {
            columnsDefinition.append(dbc.getName()).
                    append(" ");
            if (dbc.getType().getSqlType().equals(Type.STRING.getSqlType())) {
                columnsDefinition.append("VARCHAR (").
                        append(dbc.getSize()).
                        append("),\n");
            } else {
                columnsDefinition.append(dbc.getType().getSqlType()).
                        append(",\n");
            }
        }
        if (dbTable.getJoinColumn() == null) {
            columnsDefinition.delete(columnsDefinition.length() - 2, columnsDefinition.length());
        } else {
            columnsDefinition.append(getJoinColumnDefinition(dbTable));
        }
        columnsDefinition.append(");\n");
        return columnsDefinition;
    }

    private String getJoinColumnDefinition(DBTable dbTable) {
        DBColumn joinColumn = dbTable.getJoinColumn();

        return joinColumn.getName() + " " + joinColumn.getType().getSqlType();
    }

    public void initTables(Connection connection) {
        String dialect;
        try {
            dialect = ConnectionToDB.getInstance().getDialect();
            Statement statement = connection.createStatement();
            statement.execute(getTablesDefineQuery());
            if (dialect.equalsIgnoreCase("postgresql")) {
                statement.execute(linkSequenceToTable());
            }
            statement.execute(addForeignKeysWithOneRelation());
            statement.execute(getJoinTablesDefineQuery());
            statement.execute(addManyToManyForeignKeys());
        } catch (SQLException sql) {
            sql.printStackTrace();
        }
    }

    private String linkSequenceToTable() {
        Map<String, String> sequenceList = GeneratedValueHandler.sequences;
        StringBuilder linkQuery = new StringBuilder();
        for (DBTable dbTable : tables) {
            linkQuery.append("ALTER SEQUENCE IF EXISTS ")
                    .append(sequenceList.get(dbTable.getName()))
                    .append(" OWNED BY ")
                    .append(dbTable.getName()).append(".").append(dbTable.getPrimaryKey().getName())
                    .append(";\n");
        }
        return linkQuery.toString();
    }

    private String addForeignKeysWithOneRelation() {
        StringBuilder alterForeignKeysQuery = new StringBuilder();
        for (DBTable dbTable : tables) {
            if (dbTable.getJoinColumn() == null) {
                continue;
            }
            alterForeignKeysQuery.append(createAlterForeignKeyQuery(dbTable));
        }
        return alterForeignKeysQuery.toString();
    }

    private String createAlterForeignKeyQuery(DBTable dbTable) {
        StringBuilder query = new StringBuilder();
        for (ForeignKey foreignKey : dbTable.getForeignKeys()) {
            if (foreignKey.getRelationType() == RelationType.ManyToMany) {
                continue;
            }
            query.append("ALTER TABLE ").append(dbTable.getName())
                    .append(" ADD FOREIGN KEY ")
                    .append("(").append(dbTable.getJoinColumn().getName()).append(")")
                    .append(" REFERENCES ").append(foreignKey.getOtherTable().getName())
                    .append("(").append(foreignKey.getOtherTableKey().getName()).append(")")
                    .append(" ON DELETE CASCADE;\n");
        }

        return query.toString();
    }


    private String getJoinTablesDefineQuery() {
        StringBuilder builder = new StringBuilder();
        for (DBTable dbc : ManyToManyHandler.getRelationTables()) {
            builder.append(prepareJoinTableQuery(dbc));
        }
        return builder.toString();
    }


    private String addManyToManyForeignKeys() {
        StringBuilder builder = new StringBuilder();
        for (DBTable dbc : tables) {
            for (ForeignKey fk : dbc.getForeignKeys()) {
                createForeignKey(builder, dbc, fk);
            }
        }
        return builder.toString();

    }

    private void createForeignKey(StringBuilder builder, DBTable dbc, ForeignKey fk) {
        if (fk.isHasRelationsTable()) {
            for (ForeignKey relationTableKey : fk.getOtherTable().getForeignKeys()) {
                if (relationTableKey.getOtherTable() == dbc) {
                    DBTable relationTable = fk.getOtherTable();
                    builder.append("ALTER TABLE ").
                            append(relationTable.getName()).
                            append("\n").
                            append("ADD FOREIGN KEY").
                            append("(").
                            append(relationTableKey.getMyTableKey().getName()).
                            append(")").
                            append(" REFERENCES ").
                            append(dbc.getName()).
                            append("(").
                            append(relationTableKey.getOtherTableKey().getName()).
                            append(");").append("\n");
                }
            }
        }
    }

    private StringBuilder prepareJoinTableQuery(DBTable dbTable) {
        //TODO checking on "Drop if exists parameter"
        StringBuilder singleTableQuery = new StringBuilder();
        singleTableQuery.append("CREATE TABLE ").
                append(dbTable.getName()).
                append(" (\n");
        singleTableQuery.append(getColumnsDefinition(dbTable));
        singleTableQuery.delete(singleTableQuery.length() - 3, singleTableQuery.length());
        singleTableQuery.append(",");
        singleTableQuery.append(getRelationTablePrimaryKey(dbTable.getColumnSet()));
        return singleTableQuery;
    }

    private StringBuilder getRelationTablePrimaryKey(Set<DBColumn> columnSet) {
        StringBuilder builder = new StringBuilder();
        builder.append(" PRIMARY KEY(");
        for (DBColumn column : columnSet) {
            builder.append(column.getName());
            builder.append(", ");
        }
        builder.delete(builder.length() - 2, builder.length());
        builder.append("));");
        return builder;
    }

    //TODO Read processing section

    public Object readById(Object id, Class<?> clazz) {
        isEntityCheck(clazz);
        Object entity = null;
        try {
            entity = clazz.getConstructor().newInstance();
            DBTable table = getTableByClass(clazz);
            assert table != null;
            String preparedQuery = createSelectAllByPrimaryKeyQuery(table);
            PreparedStatement preparedStatement = connection.prepareStatement(preparedQuery);
            hasCorrectIdTypeCheck(id);
            preparedStatement.setObject(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            fillObjectFieldsFromTable(entity, table, rs);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException | SQLException e) {
            e.printStackTrace();
        }
        return entity;
    }

    private String createSelectAllByPrimaryKeyQuery(DBTable dbTable) {
        StringBuilder selectAllById = new StringBuilder();
        selectAllById.append("SELECT * FROM ").
                append(dbTable.getName()).
                append(" WHERE ").
                append(dbTable.getPrimaryKey().getName()).
                append(" = ? ;");
        return selectAllById.toString();
    }

    public Set<Object> readEntityByPartialInitializedInstance(Object entity) {
        Class<?> clazz = entity.getClass();
        isEntityCheck(clazz);
        Set<Object> foundedEntities = new HashSet<>();
        Set<Field> notNullFields = getNotNullFields(entity, clazz);
        DBTable dbTable = getTableByClass(clazz);
        List<DBColumn> readQueryColumns = getReadQueryColumns(notNullFields, dbTable);
        if (readQueryColumns.size() == 0) {
            throw new IllegalArgumentException("Entity \"" + dbTable.getName() +
                    "\" should have at least one initialized field annotated @Column");
        }
        String readQuery = getReadByColumnsValuesQuery(dbTable, readQueryColumns);
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(readQuery);
            setStatementValues(preparedStatement, readQueryColumns, entity);
            ResultSet resultSet = preparedStatement.executeQuery();
            fillEntitiesFromResultSet(dbTable, resultSet, foundedEntities);
        } catch (SQLException | NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return foundedEntities;
    }

    public Set<Object> readAll(Class<?> clazz) {
        isEntityCheck(clazz);
        Set<Object> foundedEntities = new HashSet<>();
        DBTable dbTable = getTableByClass(clazz);
        String readQuery = "SELECT * FROM " + dbTable.getName() + ";";
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(readQuery);
            fillEntitiesFromResultSet(dbTable, resultSet, foundedEntities);
        } catch (SQLException | NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return foundedEntities;
    }


    private Set<Field> getNotNullFields(Object entity, Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields()).
                peek(field -> field.setAccessible(true)).
                filter(field -> {
                    try {
                        return field.get(entity) != null;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return false;
                }).collect(Collectors.toSet());
    }

    private void fillEntitiesFromResultSet(DBTable dbTable, ResultSet resultSet, Set<Object> foundedEntities) throws SQLException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Object entity = null;
        while (!resultSet.isAfterLast()) {
            entity = dbTable.getMyEntityClass().getConstructor().newInstance();
            fillObjectFieldsFromTable(entity, dbTable, resultSet);
            foundedEntities.add(entity);
        }
        foundedEntities.remove(entity);
    }

    private String getReadByColumnsValuesQuery(DBTable dbTable, List<DBColumn> readQueryColumns) {
        StringBuilder readQuery = new StringBuilder();
        readQuery.append("SELECT * FROM ").
                append(dbTable.getName()).append(" WHERE ");
        for (DBColumn dbc : readQueryColumns) {
            readQuery.append(dbc.getName()).append(" = ?, ");
        }
        readQuery.delete(readQuery.length() - 2, readQuery.length());
        readQuery.append(" ;");
        return readQuery.toString();
    }

    private List<DBColumn> getReadQueryColumns(Set<Field> notNullFields, DBTable table) {
        List<DBColumn> columns = new ArrayList<>();
        for (DBColumn dbc : table.getColumnSet()) {
            if (notNullFields.contains(dbc.getField())) {
                columns.add(dbc);
            }
        }
        return columns;
    }

    //TODO Create processing section

    public boolean create(Object entity) {
        Class<?> clazz = entity.getClass();
        isEntityCheck(clazz);
        DBTable dbTable = getTableByClass(clazz);
        List<DBColumn> columnsOrder = new ArrayList<>();
        String insertPreparedQuery = buildCreateQuery(dbTable, columnsOrder);
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(insertPreparedQuery);
            setStatementValues(preparedStatement, columnsOrder, entity);
            return preparedStatement.execute();
        } catch (SQLException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String buildCreateQuery(DBTable dbTable, List<DBColumn> columnsOrder) {
        StringBuilder insertQuery = new StringBuilder();
        insertQuery.append("INSERT INTO ").
                append(dbTable.getName()).append("(").
                append(prepareColumnsForCreateQuery(dbTable, columnsOrder)).
                append(") VALUES(").
                append("?,".repeat(columnsOrder.size())).
                delete(insertQuery.length() - 1, insertQuery.length()).
                append(");");
        return insertQuery.toString();
    }

    private StringBuilder prepareColumnsForCreateQuery(DBTable dbTable, List<DBColumn> columnsOrder) {
        StringBuilder columnNames = new StringBuilder();
        for (DBColumn dbc : dbTable.getColumnSet()) {
            columnNames.append(dbc.getName()).append(", ");
            columnsOrder.add(dbc);
        }
        columnNames.delete(columnNames.length() - 2, columnNames.length());
        return columnNames;
    }

    //TODO Delete processing section

    public boolean delete(Object id, Class<?> clazz) {
        isEntityCheck(clazz);
        DBTable table = getTableByClass(clazz);
        hasPrimaryKeyCheck(table);
        hasCorrectIdTypeCheck(id);
        String deleteQuery = buildDeleteQuery(id, table);
        try {
            Statement statement = connection.createStatement();
            return statement.execute(deleteQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String buildDeleteQuery(Object id, DBTable table) {
        StringBuilder deleteQuery = new StringBuilder();
        deleteQuery.append("DELETE FROM ").
                append(table.getName()).
                append(" WHERE ").
                append(table.getPrimaryKey().getName()).
                append(" = ").
                append(id).
                append(" ;");
        return deleteQuery.toString();
    }

    //TODO Update processing section

    public boolean update(Object entity) {
        Class<?> clazz = entity.getClass();
        isEntityCheck(clazz);
        DBTable dbTable = getTableByClass(clazz);
        List<DBColumn> columnsOrder = new ArrayList<>();
        hasPrimaryKeyCheck(dbTable);
        try {
            Object id = dbTable.getPrimaryKey().getField().get(entity);
            hasCorrectIdTypeCheck(id);
            String updatePreparedQuery = buildUpdateQuery(dbTable, id, columnsOrder);
            PreparedStatement preparedStatement = connection.prepareStatement(updatePreparedQuery);
            setStatementValues(preparedStatement, columnsOrder, entity);
            return preparedStatement.execute();
        } catch (SQLException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String buildUpdateQuery(DBTable dbTable, Object id, List<DBColumn> columnsOrder) {
        StringBuilder updateQuery = new StringBuilder();
        updateQuery.append("UPDATE ").
                append(dbTable.getName()).
                append("\n SET ").
                append(prepareColumnsForUpdateQuery(dbTable, columnsOrder)).
                append("\n WHERE ").
                append(dbTable.getPrimaryKey().getName()).
                append(" = ").append(id).append(";");
        return updateQuery.toString();
    }

    private StringBuilder prepareColumnsForUpdateQuery(DBTable dbTable, List<DBColumn> columnsOrder) {
        StringBuilder columnNames = new StringBuilder();
        for (DBColumn dbc : dbTable.getColumnSet()) {
            columnNames.append(dbc.getName()).append(" = ?, ");
            columnsOrder.add(dbc);
        }
        columnNames.delete(columnNames.length() - 2, columnNames.length());
        return columnNames;
    }

    //TODO multitask methods

    private void setStatementValues(PreparedStatement preparedStatement, List<DBColumn> columnsOrder, Object entity) throws IllegalAccessException, SQLException {
        for (int i = 0; i < columnsOrder.size(); i++) {
            DBColumn dbc = columnsOrder.get(i);
            Field field = dbc.getField();
            Object value = field.get(entity);
            preparedStatement.setObject(i + 1, value);
        }
    }

    private void fillObjectFieldsFromTable(Object entity, DBTable table, ResultSet rs) throws SQLException, IllegalAccessException {
        boolean hasPrimaryKey = table.getPrimaryKey() != null;
        if (rs.next()) {
            if (hasPrimaryKey) {
                fillPrimaryKeyField(entity, table, rs);
            }
            for (DBColumn dbColumn : table.getColumnSet()) {
                fillSimpleField(entity, dbColumn, rs);
            }
        }
    }

    private void fillPrimaryKeyField(Object entity, DBTable table, ResultSet rs) throws IllegalAccessException, SQLException {
        String pkType = table.getPrimaryKey().getField().getType().getSimpleName();
        String pkName = table.getPrimaryKey().getName();
        if (pkType.equals("Integer") || pkType.equals("int")) {
            table.getPrimaryKey().getField().setInt(entity, rs.getInt(pkName));
        } else {
            if (pkType.toLowerCase().equals("long")) {
                table.getPrimaryKey().getField().setLong(entity, rs.getLong(pkName));
            } else {
                throw new IllegalStateException("Primary key should be Integer(int) or Long(long) type");
            }
        }
    }

    private void fillSimpleField(Object entity, DBColumn dbColumn, ResultSet rs) throws IllegalAccessException, SQLException {
        Field entityField = dbColumn.getField();
        String columnName = dbColumn.getName();
        entityField.set(entity, rs.getObject(columnName));
    }

    private DBTable getTableByClass(Class<?> clazz) {
        for (DBTable dbt : tables) {
            if (dbt.getMyEntityClass().equals(clazz)) {
                return dbt;
            }
        }
        return null;
    }

    //TODO Checks section

    private void hasPrimaryKeyCheck(DBTable table) {
        boolean hasPrimaryKey = table.getPrimaryKey() != null;
        if (!hasPrimaryKey) {
            throw new IllegalStateException("For this operation Entity should have @Id annotated field");
        }
    }

    private void hasCorrectIdTypeCheck(Object id) {
        if (!(id instanceof Integer) && !(id instanceof Long)) {
            throw new IllegalArgumentException("Primary key must be initialized and Integer or Long type");
        }
    }

    private void isEntityCheck(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new DataObtainingFailureException("Current class: " + clazz + Messages.ERR_CANNOT_OBTAIN_ENTITY_CLASS);
        }
        DBTable dbTable = getTableByClass(clazz);
        if (dbTable.getPrimaryKey() == null && dbTable.getColumnSet() == null) {
            throw new IllegalStateException("Entity must have at least one @Column or @Id field");
        }
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
