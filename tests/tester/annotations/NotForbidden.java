package tester.annotations;

import java.lang.annotation.*;

/**
 * The inverse of {@link Forbidden}. Takes precedence over
 * Forbidden.
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotForbidden {
    String[] value();
    Forbidden.Type type() default Forbidden.Type.PREFIX;
}