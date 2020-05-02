package annotations.handlers;

import java.util.HashSet;
import java.util.Set;

public class DBTable {
    private String name;
    private Set<DBColumn> columnSet;
    private DBColumn primaryKey;
    private Class<?> myEntityClass;
    private Set<ForeignKey> foreignKeys = new HashSet<>();

    public DBTable() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<DBColumn> getColumnSet() {
        return columnSet;
    }

    public void setColumnSet(Set<DBColumn> columnSet) {
        this.columnSet = columnSet;
    }

    public DBColumn getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(DBColumn primaryKey) {
        this.primaryKey = primaryKey;
    }

    public Class<?> getMyEntityClass() {
        return myEntityClass;
    }

    public void setMyEntityClass(Class<?> myEntityClass) {
        this.myEntityClass = myEntityClass;
    }

    public Set<ForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    public void setForeignKeys(Set<ForeignKey> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }

    @Override
    public String toString() {
        return "DBTable{" +
                "name='" + name + '\'' +
                ",\n columnSet=" + columnSet +
                ",\n primaryKey=" + primaryKey +
                ",\n myEntityClass=" + myEntityClass.getName() +
                '}';
    }
}
