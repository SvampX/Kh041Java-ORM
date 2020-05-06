package crud;


import annotations.Column;
import annotations.Entity;
import annotations.GeneratedValue;
import annotations.Table;
import annotations.handlers.*;
import exceptions.DBException;
import exceptions.DataObtainingFailureException;
import exceptions.Messages;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
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
                append(" (\n");
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
        try {
            Statement statement = connection.createStatement();
            statement.execute(getTablesDefineQuery());
//            statement.execute(linkSequenceToTable());
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
        }
        return null;
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
                    .append("(").append(foreignKey.getOtherTableKey().getName()).append(")").append(";\n");
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
                    builder.append("ALTER TABLE " + relationTable.getName() + "\n" +
                            "ADD FOREIGN KEY" + "(" + relationTableKey.getMyTableKey().getName() + ")" + " REFERENCES " +
                            dbc.getName() + "(" + relationTableKey.getOtherTableKey().getName() + ");" + "\n");
                }
            }
        }
    }

    private StringBuilder prepareJoinTableQuery(DBTable dbTable) {
        StringBuilder singleTableQuery = new StringBuilder();
        //TODO checking on "Drop if exists parameter"
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
            if (id instanceof Integer) {
                preparedStatement.setInt(1, (Integer) id);
            } else {
                preparedStatement.setLong(1, (Long) id);
            }
            ResultSet rs = preparedStatement.executeQuery();
            fillObjectSimpleFields(entity, table, rs);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException | SQLException e) {
            e.printStackTrace();
        }
        return entity;
    }

    //TODO Create processing section

    public boolean create(Object entity) {
        Class<?> clazz = entity.getClass();
        isEntityCheck(clazz);
        DBTable dbTable = getTableByClass(clazz);
        List<DBColumn> columnsOrder = new ArrayList<>();
        String insertPreparedQuery = createInsertQuery(dbTable, entity, columnsOrder);
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(insertPreparedQuery);
            setCreateStatementValues(preparedStatement, columnsOrder, entity);
            return preparedStatement.execute();
        } catch (SQLException | IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void setCreateStatementValues(PreparedStatement preparedStatement, List<DBColumn> columnsOrder, Object entity) throws IllegalAccessException, SQLException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        for (int i = 0; i < columnsOrder.size(); i++) {
            DBColumn dbc = columnsOrder.get(i);
            setPreparedStatementValueFromDBColumn(preparedStatement, entity, dbc, i + 1);

        }
    }

    private void setPreparedStatementValueFromDBColumn(PreparedStatement preparedStatement, Object entity, DBColumn dbc, int position) throws IllegalAccessException, SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        Field field = dbc.getField();
        Object value = field.get(entity);
        String fieldType = field.getType().getSimpleName();
        switch (fieldType.toLowerCase()) {
            case "string":
                preparedStatement.setString(position, (String) value);
                break;
            case "integer":
            case "int":
                preparedStatement.setInt(position, (Integer) value);
                break;
            case "short":
                preparedStatement.setShort(position, (Short) value);
                break;
            case "float":
                preparedStatement.setFloat(position, (Float) value);
                break;
            case "double":
                preparedStatement.setDouble(position, (Double) value);
                break;
            case "bigdecimal":
                preparedStatement.setBigDecimal(position, (BigDecimal) value);
                break;
            case "char":
            case "character":
                preparedStatement.setString(position, String.valueOf(value));
                break;
            case "boolean":
                preparedStatement.setBoolean(position, (Boolean) value);
                break;
            default:
                preparedStatement.setObject(position, value);
        }
    }

    private String createInsertQuery(DBTable dbTable, Object entity, List<DBColumn> columnsOrder) {
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

    private void fillObjectSimpleFields(Object entity, DBTable table, ResultSet rs) throws SQLException, IllegalAccessException {
        boolean hasPrimaryKey = table.getPrimaryKey() != null;
        DBColumn currentColumn;
        int i = 0;
        while (rs.next()) {
            if (hasPrimaryKey) {
                fillPrimaryKeyField(entity, table, rs);
                hasPrimaryKey = false;
                i++;
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
        String fieldType = entityField.getType().getSimpleName();
        String columnName = dbColumn.getName();
        switch (fieldType.toLowerCase()) {
            case "string":
                entityField.set(entity, rs.getString(columnName));
                break;
            case "integer":
            case "int":
                entityField.set(entity, rs.getInt(columnName));
                break;
            case "short":
                entityField.set(entity, rs.getShort(columnName));
                break;
            case "float":
                entityField.set(entity, rs.getFloat(columnName));
                break;
            case "double":
                entityField.set(entity, rs.getDouble(columnName));
                break;
            case "bigdecimal":
                entityField.set(entity, rs.getBigDecimal(columnName));
                break;
            case "char":
            case "character":
                entityField.set(entity, rs.getString(columnName).charAt(0));
                break;
            case "boolean":
                entityField.set(entity, rs.getBoolean(columnName));
                break;
            default:
                entityField.set(entity, rs.getObject(columnName));
        }
    }

    public Object readEntityByPartialInitializedInstance(Object object) throws IllegalAccessException, DBException {
        Class<?> clazz = object.getClass();
        isEntityCheck(clazz);
        Set<Field> notNullFields = Arrays.stream(clazz.getDeclaredFields()).
                filter(field -> {
                    try {
                        return field.get(new Object()) == null;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return false;
                }).collect(Collectors.toSet());

        DBTable table = getTableByClass(clazz);
        Map<String, Object> columns = new HashMap<>();
        for (Field f : clazz.getDeclaredFields()) {
            if (f.isAnnotationPresent(Column.class)) {
                f.setAccessible(true);
                columns.put(getColumnName(f), f.get(object));
            }
        }

        //TODO

        return object;
    }

    private String createSelectAllByPrimaryKeyQuery(DBTable dbTable) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * FROM ").
                append(dbTable.getName()).
                append(" WHERE ").
                append(dbTable.getPrimaryKey().getName()).
                append(" = ? ;");
        return builder.toString();
    }

    private String getColumnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        String name = field.getName();
        if (!column.name().isEmpty()) {
            name = column.name();
        }
        return name;
    }

    private String createSelectQuery(Map<String, Object> columns, String table) {
        StringBuilder query = new StringBuilder();
        query.append("select id from ").append(table).append(" where ");
        for (Map.Entry<String, Object> entry : columns.entrySet()) {
            if (entry.getValue() != null) {
                String value = entry.getValue().toString();
                if (entry.getValue() instanceof String || entry.getValue() instanceof Character) {
                    value = "'" + entry.getValue() + "'";
                }
                query.append(entry.getKey()).append("=").append(value).append(" and ");
            }
        }
        query.delete(query.length() - 5, query.length());
        query.append(';');
        return query.toString();
    }

    private String getTableNameByClass(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            Entity entity = clazz.getAnnotation(Entity.class);
            return entity.name();
        }

        return table.name();
    }

//    private String getSelectByEntityObjectPreparedQuery(Object object) {
//        Class clazz = object.getClass();
//        String preparedQuery;
//        StringBuilder builder = new StringBuilder();
//        DBTable dbTable = getTableByClass(clazz);
//        if (dbTable == null) {
//            throw new DataObtainingFailureException(clazz.getSimpleName() + " is not Entity annotated class");
//        }
//        preparedQuery = parseSelectTableToPreparedQuery(dbTable, object);
//
//        return builder.toString();
//    }

//    private String parseSelectTableToPreparedQuery(DBTable dbTable, Object object) {
//        ExistingQuery existingQuery = new ExistingQuery();
//        String query;
//        StringBuilder builder = new StringBuilder();
//        DBColumn primaryKey = dbTable.getPrimaryKey();
//        Set<DBColumn> columns = dbTable.getColumnSet();
//        try {
//            if (primaryKey.getField().get(new Object()) != null) {
//                existingQuery.addColumn(primaryKey);
//                query = createSelectAllByPrimaryKeyQuery(dbTable);
//                existingQuery.setQuery(query);
//                queryWithTypesList.put(object.getClass(), existingQuery);
//                return query;
//            }
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//        builder.append("SELECT * FROM ").
//                append(dbTable.getName()).
//                append(" WHERE ");
//        for (DBColumn column : columns) {
//            try {
//                if (column.getField().get(new Object()) != null) {
//                    queryColumns.add(column);
//                    builder.append(column.getName()).
//                            append(" = ? and ");
//                }
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        }
//        if (queryColumns.size() < 1) {
//            throw new IllegalStateException("Entity instance should have " +
//                    "at least 1 initialized field annotated with @Column");
//        }
//        builder.delete(builder.length() - 4, builder.length());
//        builder.append(';');
//        query = builder.toString();
//        queryWithTypesList.put(query, queryColumns);
//        return query;
//    }

    private DBTable getTableByClass(Class clazz) {
        for (DBTable dbt : tables) {
            if (dbt.getMyEntityClass().equals(clazz)) {
                return dbt;
            }
        }
        return null;
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
