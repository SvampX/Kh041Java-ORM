package annotations.handlers;

import annotations.GeneratedValue;
import annotations.GenerationType;
import connections.ConnectionToDB;
import annotations.SequenceGenerator;
import exceptions.DBException;
import exceptions.DataObtainingFailureException;
import exceptions.Messages;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class GeneratedValueHandler {

    public static final String MYSQL_DIALECT = "mysql";
    public static final String POSTGRES_DIALECT = "postgresql";
    public static final String MYSQL_PRIMARY_KEY = " NOT NULL PRIMARY KEY AUTO_INCREMENT";
    public static final String PRIMARY_KEY_DEFAULT_NEXTVAL = " NOT NULL PRIMARY KEY DEFAULT NEXTVAL";
    public static final String OPEN_BRACKET = "('";
    public static final String CLOSE_BRACKET = "')";
    public static final String CREATE_SEQUENCE = "CREATE SEQUENCE ";
    public static final String START_WITH = " START WITH ";
    public static final String INCREMENT_BY = " INCREMENT BY ";
    public static final String END_SEQ_NAME = "_id_seq";

    public static Map<String, String> sequences = new HashMap<>();

    SequenceGenerator sequenceGenerator;
    String dbDialect;

    public String createIdGenerator(DBTable table) throws SQLException {
        Field idField = table.getPrimaryKey().getField();
        DBColumn primaryKey = table.getPrimaryKey();

        GeneratedValue generatedValue = idField.getAnnotation(GeneratedValue.class);
        if (generatedValue == null) {
            throw new DataObtainingFailureException(Messages.ERR_CANNOT_OBTAIN_GENERATED_VALUE);
        }

        GenerationType generationType = generatedValue.strategy();
        checkCompatibility(generationType);
        switch (generationType) {
            case IDENTITY:
                return generateIdentityScript(primaryKey);
            case SEQUENCE:
                return generateIdSequenceScript(primaryKey, generatedValue, table);
            case AUTO:
            default:
                return generateAutoScript(primaryKey, generatedValue, table);
        }
    }

    private String generateAutoScript(DBColumn primaryKey, GeneratedValue generatedValue, DBTable table) throws SQLException {
        switch (dbDialect) {
            case MYSQL_DIALECT:
                return generateIdentityScript(primaryKey);
            case POSTGRES_DIALECT:
                return generateIdSequenceScript(primaryKey, generatedValue, table);
            default:
                throw new IllegalArgumentException(Messages.ERR_DB_DIALECT_IS_NOT_SUPPORTED);
        }
    }

    private String generateIdentityScript(DBColumn primaryKey) {
        String type = getSqlIdType(primaryKey.getField());

        return primaryKey.getName() + " " + type + MYSQL_PRIMARY_KEY;
    }

    private String generateIdSequenceScript(DBColumn primaryKey, GeneratedValue generatedValue, DBTable table) {
        String type = getSqlIdType(primaryKey.getField());
        sequenceGenerator = getSequenceGenerator(primaryKey.getField());

        String sequenceScript = createSequenceScript(generatedValue, sequenceGenerator, table);
        try {
            insertSequenceToDB(sequenceScript);
        } catch (DBException e) {
            e.printStackTrace();
        }

        return primaryKey.getName() + " " + type + PRIMARY_KEY_DEFAULT_NEXTVAL + OPEN_BRACKET +
                getSequenceName(generatedValue, sequenceGenerator, table) + CLOSE_BRACKET;
    }

    private void insertSequenceToDB(String sequenceScript) throws DBException {
        Connection connection;
        Statement statement;
        try {
            connection = ConnectionToDB.getInstance().getConnection();
            statement = connection.createStatement();
            statement.execute(sequenceScript);
        } catch (SQLException e) {
            throw new DBException(Messages.ERR_CANNOT_INSERT_SEQUENCE + sequenceScript, e);
        }
    }

    private String createSequenceScript(GeneratedValue generatedValue, SequenceGenerator sequenceGenerator,
                                        DBTable table) {
        String sequenceScript;
        String sequenceName = getSequenceName(generatedValue, sequenceGenerator, table);
        if (hasGenerator(generatedValue)) {
            sequenceScript = CREATE_SEQUENCE + sequenceName;
        } else {
            if (sequenceGenerator == null) {
                throw new DataObtainingFailureException(Messages.ERR_CANNOT_OBTAIN_SEQUENCE_GENERATOR_CLASS);
            }
            sequenceScript = CREATE_SEQUENCE + sequenceName + START_WITH + sequenceGenerator.initialValue() +
                    INCREMENT_BY + sequenceGenerator.allocationSize();
        }

        sequences.put(table.getName(), sequenceName);
        return sequenceScript;
    }

    private boolean hasGenerator(GeneratedValue generatedValue) {
        return generatedValue.generator().isEmpty();
    }

    private String getSequenceName(GeneratedValue generatedValue, SequenceGenerator sequenceGenerator, DBTable table) {
        if (hasGenerator(generatedValue)) {
            return table.getName() + END_SEQ_NAME;
        }
        if (sequenceGenerator.sequenceName().isEmpty()) {
            return table.getName() + END_SEQ_NAME;
        }

        return sequenceGenerator.sequenceName();
    }

    private String getSqlIdType(Field field) {
        String fieldType = field.getType().getSimpleName();
        if ("int".equalsIgnoreCase(fieldType) || "Integer".equalsIgnoreCase(fieldType)) {
            return "INT";
        } else if ("long".equalsIgnoreCase(fieldType) || "Long".equalsIgnoreCase(fieldType)) {
            return "BIGINT";
        } else {
            throw new DataObtainingFailureException(Messages.ERR_INAPPROPRIATE_ID_TYPE);
        }
    }

    private void checkCompatibility(GenerationType generationType) {
        try {
            dbDialect = ConnectionToDB.getInstance().getDialect().toLowerCase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if ((dbDialect.equals(MYSQL_DIALECT) && generationType == GenerationType.SEQUENCE) ||
                (dbDialect.equals(POSTGRES_DIALECT) && generationType == GenerationType.IDENTITY)) {
            throw new IllegalArgumentException(Messages.ERR_GENERATION_TYPE_COMPATIBILITY);
        }
    }

    private SequenceGenerator getSequenceGenerator(Field field) {
        return field.getAnnotation(SequenceGenerator.class);
    }
}

