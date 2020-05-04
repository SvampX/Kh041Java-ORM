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
    SequenceGenerator sequenceGenerator;

    public String createIdGenerator(DBColumn primaryKey, DBTable table) throws SQLException {
        String idScript = "";
        Field idField = primaryKey.getField();

        GeneratedValue generatedValue = idField.getAnnotation(GeneratedValue.class);
        if (generatedValue == null) {
            throw new DataObtainingFailureException(Messages.ERR_CANNOT_OBTAIN_GENERATED_VALUE);
        }

        GenerationType generationType = generatedValue.strategy();
        if (generationType == GenerationType.IDENTITY) {
            idScript = generateIdentityScript(idField);
        } else if (generationType == GenerationType.SEQUENCE) {
            idScript = generateIdSequenceScript(idField, generatedValue, table);
        } else if (generationType == GenerationType.AUTO) {
            idScript = generateAutoScript(idField, generatedValue, table);
        }
        return idScript;
    }

    private String generateAutoScript(Field idField, GeneratedValue generatedValue, DBTable table) throws SQLException {
        String dbDialect = ConnectionToDB.getInstance().getDialect();

        return null;
    }

    private String generateIdentityScript(Field field) {
        String type = getSqlIdType(field);

        return "ID " + type + " NOT NULL PRIMARY KEY AUTO_INCREMENT";
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

        return "ID " + type + " NOT NULL PRIMARY KEY " + "DEFAULT NEXTVAL('" +
                getSequenceName(generatedValue, sequenceGenerator, table) + "')";
    }

    private void insertSequenceToDB(String sequenceScript) throws DBException {
        Connection connection;
        Statement statement;
        boolean success;
        try {
            connection = ConnectionToDB.getInstance().getConnection();
            statement = connection.createStatement();
            success = statement.execute(sequenceScript);
            if (!success) {
                throw new DataObtainingFailureException(Messages.ERR_CANNOT_INSERT_SEQUENCE);
            }
        } catch (SQLException e) {
            throw new DBException(Messages.ERR_CANNOT_INSERT_SEQUENCE, e);
        }
    }

    private String createSequenceScript(GeneratedValue generatedValue, SequenceGenerator sequenceGenerator,
                                        DBTable table) {
        String sequenceScript = "";
        String sequenceName = getSequenceName(generatedValue, sequenceGenerator, table);
        if (hasGenerator(generatedValue)) {
            sequenceScript = "CREATE SEQUENCE " + getSequenceName(generatedValue, sequenceGenerator, table);
        } else {
            if (sequenceGenerator == null) {
                throw new DataObtainingFailureException(Messages.ERR_CANNOT_OBTAIN_SEQUENCE_GENERATOR_CLASS);
            }
            sequenceScript = "CREATE SEQUENCE " + sequenceName + " START WITH " + sequenceGenerator.initialValue() +
                    " INCREMENT BY " + sequenceGenerator.allocationSize();
        }

        return sequenceScript;
    }

    private boolean hasGenerator(GeneratedValue generatedValue) {
        return generatedValue.generator().isEmpty();
    }

    private String getSequenceName(GeneratedValue generatedValue, SequenceGenerator sequenceGenerator, DBTable table) {
        if (hasGenerator(generatedValue)) {
            return table.getName() + "_id_seq";
        }
        if (sequenceGenerator.sequenceName().isEmpty()) {
            return table.getName() + "_id_seq";
        }
        return sequenceGenerator.sequenceName();
    }

    private String getSqlIdType(Field field) {
        String type = "";
        String fieldType = field.getType().getSimpleName();

        if ("int".equalsIgnoreCase(fieldType) || "Integer".equalsIgnoreCase(fieldType)) {
            type = "INT";
        } else if ("long".equalsIgnoreCase(fieldType) || "Long".equalsIgnoreCase(fieldType)) {
            type = "BIGINT";
        } else {
            throw new DataObtainingFailureException(Messages.ERR_INAPPROPRIATE_ID_TYPE);
        }

        return type;
    }

    private SequenceGenerator getSequenceGenerator(Field field) {
        return field.getAnnotation(SequenceGenerator.class);
    }
}

