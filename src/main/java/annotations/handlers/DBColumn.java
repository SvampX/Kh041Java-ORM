package annotations.handlers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DBColumn {
    private Field field;
    private Method method;
    private String name;

    public DBColumn() {
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        if (field == null) {
            return method.hashCode() + name.hashCode();
        } else {
            if (method == null) {
                return field.hashCode() + name.hashCode();
            }
        }
        return field.hashCode() + method.hashCode() + name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        DBColumn other = (DBColumn) obj;
        if (method == null) {
            return this.name.equals(other.name) &&
                    this.field.equals(other.field);
        } else {
            if (field == null) {
                return this.name.equals(other.name) &&
                        this.method.equals(other.field);
            }
        }
        return this.name.equals(other.name) &&
                this.method.equals(other.method) &&
                this.field.equals(other.field);
    }
}
