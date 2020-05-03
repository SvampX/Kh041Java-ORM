package annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ManyToMany {
    String tableName() default "";

    String joinColumnsName() default "";

    String joinColumnsReferencedName() default "";

    String inverseJoinColumnsName() default "";

    String inverseJoinColumnsReferencedName() default "";
}