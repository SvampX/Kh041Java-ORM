package annotations.handlers;

public enum Type {
    STRING("VARCHAR"),
    INTEGER("INTEGER"),
    LONG("BIGINT"),
    SHORT("SMALLINT"),
    FLOAT("FLOAT"),
    DOUBLE("DOUBLE"),
    BIGDECIMAL("NUMERIC"),
    CHARACTER("CHAR(1)"),
    BOOLEAN("BIT"),
    OTHER("VARBINARY");
    private String sqlType;

    Type(String sqlType) {
        this.sqlType = sqlType;
    }

    public String getSqlType(){
        return sqlType;
    }
    }
