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
    BOOLEAN("BOOLEAN"),
    TIME("TIME"),
    DATE("DATE"),
    TIMESTAMP("TIMESTAMP"),
    MYSQL_OTHER("BLOB"),
    POSTGRES_OTHER("BYTEA");

    private String sqlType;

    Type(String sqlType) {
        this.sqlType = sqlType;
    }

    public String getSqlType() {
        return sqlType;
    }
}
