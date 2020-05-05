package crud;


import annotations.handlers.*;
import annotations.Column;
import annotations.Entity;
import annotations.Table;
import annotations.handlers.DBColumn;
import annotations.handlers.DBTable;
import annotations.handlers.EntityToTableMapper;
import annotations.handlers.ForeignKey;
import annotations.handlers.RelationType;
import annotations.handlers.Type;
import exceptions.DBException;
import exceptions.DataObtainingFailureException;
import exceptions.Messages;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

public class CrudServices {
    Set<DBTable> tables = EntityToTableMapper.getTables();
    Map<Class, ExistingQuery> queryWithTypesList = new HashMap<>();

    //Tables definition section

    private String getTablesDefineQuery() {
        StringBuilder builder = new StringBuilder();
        for (DBTable dbc : tables) {
            builder.append(prepareTableQuery(dbc));
        }
        return builder.toString();
    }

    private StringBuilder prepareTableQuery(DBTable dbTable) {
        boolean hasPrimaryKey = dbTable.getPrimaryKey() != null;
        StringBuilder builder = new StringBuilder();
        //TODO checking on "Drop if exists parameter"
        builder.append("CREATE TABLE ").
                append(dbTable.getName()).
                append(" (\n");
        if (hasPrimaryKey) {
            builder.append(dbTable.getPrimaryKey().getName()).
                    append(" SERIAL4 PRIMARY KEY,\n");
        }
        builder.append(getColumnsDefinition(dbTable.getColumnSet()));
        return builder;
    }

    private StringBuilder getColumnsDefinition(Set<DBColumn> dbColumns) {
        StringBuilder builder = new StringBuilder();
        for (DBColumn dbc : dbColumns) {
            builder.append(dbc.getName()).
                    append(" ");
            if (dbc.getType().getSqlType().equals(Type.STRING.getSqlType())) {
                builder.append("VARCHAR (").
                        append(dbc.getSize()).
                        append("),\n");
            } else {
                builder.append(dbc.getType().getSqlType()).
                        append(",\n");
            }
        }
        builder.delete(builder.length() - 2, builder.length());
        builder.append(");\n");
        return builder;
    }

    public void initTables(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            statement.execute(getTablesDefineQuery());
            statement.execute(addForeignKeysWithOneRelation());
            statement.execute(getJoinTablesDefineQuery());
            statement.execute(addManyToManyForeignKeys());
        } catch (SQLException sql) {
            sql.printStackTrace();
        }
    }

    private String addForeignKeysWithOneRelation() {
        StringBuilder alterForeignKeysQuery = new StringBuilder();
        for (DBTable dbTable : tables) {
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
                    .append("(").append(foreignKey.getOtherTableKey().getName()).append(")")
                    .append(" REFERENCES ").append(foreignKey.getOtherTable().getName())
                    .append("(").append(foreignKey.getOtherTableKey().getName()).append(")").append(";");
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

    private StringBuilder prepareJoinTableQuery(DBTable dbTable) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE ").
                append(dbTable.getName()).
                append(" (\n");
        builder.append(getJoinTableColumnsDefinition(dbTable.getColumnSet()));
        return builder;
    }

    private StringBuilder getJoinTableColumnsDefinition(Set<DBColumn> dbColumns) {
        StringBuilder builder = new StringBuilder();
        for (DBColumn dbc : dbColumns) {
            builder.append(dbc.getName()).
                    append(" ");
            if (dbc.getType().getSqlType().equals(Type.STRING.getSqlType())) {
                builder.append("VARCHAR (").
                        append(dbc.getSize()).
                        append(") UNIQUE,\n");
            } else {
                builder.append(dbc.getType().getSqlType()).
                        append(" UNIQUE,\n");
            }
        }
        builder.delete(builder.length() - 2, builder.length());
        builder.append(");\n");
        return builder;
    }

    private String addManyToManyForeignKeys() {
        StringBuilder builder = new StringBuilder();
        for (DBTable dbc : tables) {
            for (ForeignKey fk : dbc.getForeignKeys()) {
                createCrossForeignKeys(builder, dbc, fk);
            }
        }
        return builder.toString();
    }

    private void createCrossForeignKeys(StringBuilder builder, DBTable dbc, ForeignKey fk) {
        if (fk.isHasRelationsTable()) {
            builder.append("ALTER TABLE " + dbc.getName() + "\n" +
                    " ADD FOREIGN KEY " + "(" + fk.getMyTableKey().getName() + ")" + " REFERENCES " +
                    fk.getOtherTable().getName() + "(" + fk.getOtherTableKey().getName() + ");" + "\n");

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

    private String getSelectAllColumnsQuery(DBTable dbTable) {

    //TODO Read processing section

    public Object readEntityById(Object id, Object entity) {
        Class<?> clazz = entity.getClass();
        isEntityCheck(clazz);
        DBTable table = getTableByClass(clazz);
        assert table != null;
        String preparedQuery =  createSelectAllByPrimaryKeyQuery(table);


        return entity;
    }

//    public Object readEntityByPartialInitializedInstance(Object object) throws IllegalAccessException, DBException {
//        Class<?> clazz = object.getClass();
//        isEntityCheck(clazz);
//        String tableName = getTableNameByClass(clazz);
//        DBTable table = getTableByClass(clazz);
//        Map<String, Object> columns = new HashMap<>();
//        for (Field f : clazz.getDeclaredFields()) {
//            if (f.isAnnotationPresent(Column.class)) {
//                f.setAccessible(true);
//                columns.put(getColumnName(f), f.get(object));
//            }
//        }
//
//        //TODO
//
//        return object;
//    }

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
    }


}
