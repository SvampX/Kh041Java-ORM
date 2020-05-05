package annotations.handlers;

import annotations.GeneratedValue;
import annotations.GenerationType;
import annotations.SequenceGenerator;
import connections.ConnectionToDB;
import exceptions.DBException;
import exceptions.DataObtainingFailureException;
import exceptions.Messages;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class GeneratedValueHandler {

    public static final String MYSQL_DIALECT = "mysql";
    public static final String POSTGRES_DIALECT = "postgresql";
    public static final String ID = "ID ";
    public static final String MYSQL_PRIMARY_KEY = " NOT NULL PRIMARY KEY AUTO_INCREMENT";
    public static final String PRIMARY_KEY_DEFAULT_NEXTVAL = " NOT NULL PRIMARY KEY DEFAULT NEXTVAL";
    public static final String OPEN_BRACKET = "('";
    public static final String CLOSE_BRACKET = "')";
    public static final String CREATE_SEQUENCE = "CREATE SEQUENCE ";
    public static final String START_WITH = " START WITH ";
    public static final String INCREMENT_BY = " INCREMENT BY ";
    public static final String END_SEQ_NAME = "_id_seq";

    SequenceGenerator sequenceGenerator;

    public String createIdGenerator(DBTable table) throws SQLException {
        Field idField = table.getPrimaryKey().getField();

        GeneratedValue generatedValue = idField.getAnnotation(GeneratedValue.class);
        if (generatedValue == null) {
            throw new DataObtainingFailureException(Messages.ERR_CANNOT_OBTAIN_GENERATED_VALUE);
        }

        GenerationType generationType = generatedValue.strategy();
        switch (generationType) {
            case IDENTITY:
                return generateIdentityScript(idField);
            case SEQUENCE:
                return generateIdSequenceScript(idField, generatedValue, table);
            case AUTO:
            default:
                return generateAutoScript(idField, generatedValue, table);
        }
    }

    private String generateAutoScript(Field idField, GeneratedValue generatedValue, DBTable table) throws SQLException {
        String dbDialect = ConnectionToDB.getInstance().getDialect().toLowerCase();

        switch (dbDialect) {
            case MYSQL_DIALECT:
                return generateIdentityScript(idField);
            case POSTGRES_DIALECT:
                return generateIdSequenceScript(idField, generatedValue, table);
            default:
                throw new IllegalArgumentException(Messages.ERR_DB_DIALECT_IS_NOT_SUPPORTED);
        }
    }

    private String generateIdentityScript(Field field) {
        String type = getSqlIdType(field);

        return ID + type + MYSQL_PRIMARY_KEY;
    }

    private String generateIdSequenceScript(Field field, GeneratedValue generatedValue, DBTable table) {
        String type = getSqlIdType(field);
        sequenceGenerator = getSequenceGenerator(field);

        String sequenceScript = createSequenceScript(generatedValue, sequenceGenerator, table);
        try {
            insertSequenceToDB(sequenceScript);
        } catch (DBException e) {
            e.printStackTrace();
        }

        return ID + type + PRIMARY_KEY_DEFAULT_NEXTVAL + OPEN_BRACKET +
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
            throw new DBException(Messages.ERR_CANNOT_INSERT_SEQUENCE, e);
        }
    }

    private String createSequenceScript(GeneratedValue generatedValue, SequenceGenerator sequenceGenerator,
                                        DBTable table) {
        String sequenceScript;
        String sequenceName = getSequenceName(generatedValue, sequenceGenerator, table);
        if (hasGenerator(generatedValue)) {
            sequenceScript = CREATE_SEQUENCE + getSequenceName(generatedValue, sequenceGenerator, table);
        } else {
            if (sequenceGenerator == null) {
                throw new DataObtainingFailureException(Messages.ERR_CANNOT_OBTAIN_SEQUENCE_GENERATOR_CLASS);
            }
            sequenceScript = CREATE_SEQUENCE + sequenceName + START_WITH + sequenceGenerator.initialValue() +
                    INCREMENT_BY + sequenceGenerator.allocationSize();
        }

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

    private SequenceGenerator getSequenceGenerator(Field field) {
        return field.getAnnotation(SequenceGenerator.class);
    }
}

