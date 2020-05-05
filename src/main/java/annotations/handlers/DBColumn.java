package annotations.handlers;

import java.lang.reflect.Field;

public class DBColumn {
    private Field field;
    private String name;
    private Type type;
    private int size;

    public DBColumn() {
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public int hashCode() {
        return field.hashCode() + name.hashCode();
    }

    @Override
    public String toString() {
        return "DBColumn{" +
                "field=" + field +
                ", name='" + name + '\'' +
                '}';
    }
}
