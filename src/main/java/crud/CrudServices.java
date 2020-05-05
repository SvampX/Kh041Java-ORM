package crud;

import annotations.handlers.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

public class CrudServices {
    Set<DBTable> tables = EntityToTableMapper.getTables();

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
            statement.execute(getJoinTablesDefineQuery());
            statement.execute(addManyToManyForeignKeys());
        } catch (SQLException sql) {
            sql.printStackTrace();
        }
    }
    private String getJoinTablesDefineQuery(){
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
    private String addManyToManyForeignKeys(){
        StringBuilder builder = new StringBuilder();
        for (DBTable dbc : tables) {
            for(ForeignKey fk : dbc.getForeignKeys()){
                createCrossForeignnKeys(builder, dbc, fk);
            }
        }
        return builder.toString();
    }

    private void createCrossForeignnKeys(StringBuilder builder, DBTable dbc, ForeignKey fk) {
        if(fk.isHasRelationsTable()){
            builder.append("ALTER TABLE " + dbc.getName() + "\n" +
                    " ADD FOREIGN KEY " + "(" + fk.getMyTableKey().getName() + ")" + " REFERENCES " +
                    fk.getOtherTable().getName()+"(" + fk.getOtherTableKey().getName() + ");" + "\n");

            for( ForeignKey relationTableKey : fk.getOtherTable().getForeignKeys()){
                if(relationTableKey.getOtherTable() == dbc){
                    DBTable relationTable = fk.getOtherTable();
                    builder.append("ALTER TABLE " + relationTable.getName() + "\n" +
                            "ADD FOREIGN KEY" + "(" + relationTableKey.getMyTableKey().getName() + ")" + " REFERENCES " +
                            dbc.getName() +"(" + relationTableKey.getOtherTableKey().getName() + ");" + "\n");
                }
            }
        }
    }

    private String getSelectAllColumnsQuery(DBTable dbTable) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * FROM ").append(dbTable.getName()).
                append(" WHERE ").
                append(dbTable.getPrimaryKey().getName()).
                append(" = ? ;");
        return builder.toString();
    }


}
