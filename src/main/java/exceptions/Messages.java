package exceptions;

public final class Messages {

    public static final String ERR_CANNOT_INSERT_SEQUENCE = "Cannot insert current sequence into database";

    public static final String ERR_CANNOT_DELETE_SEQUENCE = "Cannot delete current sequence into database";

    public static final String ERR_CANNOT_OBTAIN_GENERATED_VALUE =
            "Current field is not annotated with @GeneratedValue annotation";

    public static final String ERR_INAPPROPRIATE_ID_TYPE =
            "Inappropriate type for id field. Must be: int, Integer, long or Long";

    public static final String ERR_CANNOT_OBTAIN_SEQUENCE_GENERATOR_CLASS =
            "Cannot obtain SequenceGenerator class. Must use " +
                    "@SequenceGenerator annotation to determine generator for sequence";

    public static final String ERR_DB_DIALECT_IS_NOT_SUPPORTED = "This database dialect is not supported";
}
