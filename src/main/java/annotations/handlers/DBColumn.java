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
}
