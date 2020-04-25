package annotations.handlers;

import annotations.Column;
import annotations.Entity;
import annotations.GeneratedValue;
import annotations.Table;
import exceptions.DBException;
import exceptions.DataObtainingFailureException;
import exceptions.Messages;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class GeneratedValueHandler {

    public void setGeneratedIdValue(Object object) throws IllegalAccessException, DBException {
        Class<?> clazz = object.getClass();
        Field field = null;
        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new DataObtainingFailureException("Current class: " + clazz + Messages.ERR_CANNOT_OBTAIN_ENTITY_CLASS);
        }
        String tableName = getTableName(clazz);
        
        Map<String, Object> columns = new HashMap<>();
        for (Field f : clazz.getDeclaredFields()) {
            if (f.isAnnotationPresent(Column.class)) {
                f.setAccessible(true);
                columns.put(getColumnName(f), f.get(object));
            } else if (f.isAnnotationPresent(GeneratedValue.class)){
                field = f;
            }
        }

        if (field != null) {
            String sqlQuery = createQuery(columns, tableName);
            field.setAccessible(true);
            field.setInt(object, getIdFromTable(sqlQuery));
        }
    }

    private String getColumnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column == null) {
            throw new DataObtainingFailureException(Messages.ERR_CANNOT_OBTAIN_COLUMN_CLASS);
        }
        String name = field.getName();
        if (!column.name().isEmpty()) {
            name = column.name();
        }
        return name;
    }

    private String createQuery(Map<String, Object> columns, String table) {
        StringBuilder query = new StringBuilder();
        query.append("select id from ").append(table).append(" where ");
        for (Map.Entry<String, Object> entry : columns.entrySet()) {
            String value = entry.getValue().toString();
            if (entry.getValue() instanceof String) {
                value = "'" + entry.getValue() + "'";
            }
            query.append(entry.getKey()).append("=").append(value).append(" and ");
        }
        query.delete(query.length() - 5, query.length());
        return query.toString();
    }

    private String getTableName(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            Entity entity = clazz.getAnnotation(Entity.class);
            if (entity == null) {
                throw new DataObtainingFailureException("Current class: " + clazz +
                        Messages.ERR_CANNOT_OBTAIN_ENTITY_CLASS);
            }
        }

        return table.name();
    }

    private int getIdFromTable(String sqlQuery) throws DBException {
        int id = 0;
        //need to get connection here
        Connection connection = Test.connect();
        Statement statement;
        ResultSet resultSet;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sqlQuery);
            if (resultSet.next()) {
                id = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            throw new DBException(Messages.ERR_CANNOT_OBTAIN_ID, e);
        }
        return id;
    }

}
